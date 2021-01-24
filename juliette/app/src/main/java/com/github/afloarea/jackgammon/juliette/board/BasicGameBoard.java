package com.github.afloarea.jackgammon.juliette.board;

import com.github.afloarea.jackgammon.juliette.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

//TODO:
// Think about exposing suspended pieces
// the following case is not yet supported:
//      rolled (6 3) -> can do any, but if you do the 3 you can't do the 6 (you should be forced to do 6 and then 3)
// composite moves not supported yet

public final class BasicGameBoard implements GameBoard {
    private static final int MAX_PLAYER_PIECES  = 15;
    private static final int TOTAL_COLUMNS      = 24;

    private final BoardColumn[] blackViewBoardColumns = new BoardColumn[TOTAL_COLUMNS];
    private final BoardColumn[] whiteViewBoardColumns = new BoardColumn[TOTAL_COLUMNS];

    private final Map<String, BoardColumn> columnsById = new HashMap<>();

    private final Map<Color, BoardColumn> suspendedByColor = new EnumMap<>(Color.class);
    private final Map<Color, BoardColumn> collectedByColor = new EnumMap<>(Color.class);

    private Color currentPlayingColor = Color.NONE;

    private DiceResult currentDiceResult;
    private final List<Integer> remainingDiceValues = new ArrayList<>();

    // possible moves for current player
    private final Map<Integer, Set<GameMove>> possibleMovesByDiceValue = new HashMap<>();

    BasicGameBoard(BoardColumn[] boardColumns,
                   int suspendedBlack, int suspendedWhite, int collectedBlack, int collectedWhite) {

        if (boardColumns.length != TOTAL_COLUMNS) {
            throw new IllegalArgumentException("Incorrect number of columns provided");
        }

        System.arraycopy(boardColumns, 0, blackViewBoardColumns, 0, TOTAL_COLUMNS);
        for (int index = 0; index < TOTAL_COLUMNS; index++) {
            whiteViewBoardColumns[index] = blackViewBoardColumns[TOTAL_COLUMNS - index - 1];
        }
        suspendedByColor.put(Color.BLACK, new BoardColumn(suspendedBlack, Color.BLACK, "SB"));
        suspendedByColor.put(Color.WHITE, new BoardColumn(suspendedWhite, Color.WHITE, "SW"));
        collectedByColor.put(Color.BLACK, new BoardColumn(collectedBlack, Color.BLACK, "CB"));
        collectedByColor.put(Color.WHITE, new BoardColumn(collectedWhite, Color.WHITE, "CW"));

        final var grouped = Stream.concat(
                Arrays.stream(boardColumns),
                Stream.concat(suspendedByColor.values().stream(), collectedByColor.values().stream()))
                .collect(Collectors.toMap(BoardColumn::getId, Function.identity()));
        columnsById.putAll(grouped);

    }

    @Override
    public boolean currentPlayingColorFinishedTurn() {
        return remainingDiceValues.isEmpty();
    }

    @Override
    public Color getCurrentPlayingColor() {
        return currentPlayingColor;
    }

    @Override
    public Color getWinningColor() {
        return collectedByColor.values().stream()
                .filter(column -> column.getPieceCount() == MAX_PLAYER_PIECES)
                .map(BoardColumn::getPieceColor)
                .findFirst()
                .orElse(Color.NONE);
    }

    @Override
    public Color getLosingColor() {
        return getWinningColor().complement();
    }

    @Override
    public Set<GameMove> getPossibleMovesForCurrentPlayingColor() {
        return possibleMovesByDiceValue.values().stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public List<GameMove> executeMoveForPlayingColor(Color playingColor, GameMove move) {
        if (isGameComplete()) {
            throw new IllegalGameActionException("Game is complete. No more moves allowed");
        }

        if (playingColor != currentPlayingColor || playingColor == Color.NONE) {
            throw new IllegalGameActionException("Incorrect playing color provided");
        }

        final var optionalDiceValueUsed = possibleMovesByDiceValue.entrySet().stream()
                .filter(entry -> entry.getValue().contains(move))
                .map(Map.Entry::getKey)
                .findFirst();

        if (optionalDiceValueUsed.isEmpty()) {
            throw new IllegalGameActionException("Move cannot be made");
        }

        final var sourceColumn = columnsById.get(move.getFrom());
        final var targetColumn = columnsById.get(move.getTo());

        final var executedMoves = new ArrayList<GameMove>();
        final var opponentColor = currentPlayingColor.complement();

        if (targetColumn.getPieceColor() == opponentColor) {
            final var suspendedColumn = suspendedByColor.get(opponentColor);
            suspendedColumn.addPiece(opponentColor);
            targetColumn.removePiece();
            executedMoves.add(new GameMove(targetColumn.getId(), suspendedColumn.getId()));
        }

        targetColumn.addPiece(currentPlayingColor);
        sourceColumn.removePiece();
        executedMoves.add(move);

        remainingDiceValues.remove(optionalDiceValueUsed.get());
        updatePossibleMoves();
        return executedMoves;
    }

    private void updatePossibleMoves() {
        possibleMovesByDiceValue.clear();
        possibleMovesByDiceValue.putAll(computePossibleMovesForCurrentPlayer());
        if (possibleMovesByDiceValue.values().stream().allMatch(Set::isEmpty)) {
            remainingDiceValues.clear();
        }
    }

    @Override
    public void updateDiceForPlayingColor(Color playingColor, DiceResult dice) {
        performRollValidation(playingColor, dice);

        currentPlayingColor = playingColor;
        currentDiceResult = dice;
        dice.stream().forEach(remainingDiceValues::add);

        updatePossibleMoves();
    }

    private void performRollValidation(Color playingColor, DiceResult dice) {
        if (isGameComplete()) {
            throw new IllegalGameActionException("Unable to roll dice. Game is finished");
        }
        if (playingColor == Color.NONE) {
            throw new IllegalGameActionException("None color cannot update the dice");
        }
        if (!remainingDiceValues.isEmpty()) {
            throw new IllegalGameActionException("Cannot update dice. Turn is not yet over");
        }
        if (playingColor != currentPlayingColor.complement() && currentPlayingColor != Color.NONE) {
            throw new IllegalGameActionException("Wrong player color rolled dice.");
        }
    }

    private Map<Integer, Set<GameMove>> computePossibleMovesForCurrentPlayer() {
        return remainingDiceValues.stream()
                .distinct()
                .collect(Collectors.toMap(Function.identity(), this::computePossibleMovesForCurrentPlayer));
    }

    private Set<GameMove> computePossibleMovesForCurrentPlayer(int diceValue) {
        final var workingBoard = getBoardViewForCurrentPlayer();

        // enter-type move
        if (getSuspendedPiecesForCurrentPlayer() > 0) {
            final var column = workingBoard[diceValue - 1];
            return column.canAccept(currentPlayingColor)
                    ? Set.of(new GameMove(suspendedByColor.get(currentPlayingColor).getId(), column.getId()))
                    : Collections.emptySet();
        }

        // simple moves
        final Set<GameMove> viableMoves = generateSimpleMoves(diceValue);

        // collect-type move
        generateCollectMove(diceValue).ifPresent(viableMoves::add);

        return viableMoves;
    }

    private Set<GameMove> generateSimpleMoves(int distance) {
        final var workingBoard = getBoardViewForCurrentPlayer();
        return IntStream.range(0, workingBoard.length)
                .filter(index -> workingBoard[index].getPieceColor() == currentPlayingColor)
                .mapToObj(index -> generateSimpleMove(index, distance))
                .flatMap(Optional::stream)
                .collect(Collectors.toSet());
    }

    private Optional<GameMove> generateSimpleMove(int index, int distance) {
        final BoardColumn[] workingBoard = getBoardViewForCurrentPlayer();
        final int newIndex = index + distance;
        if (newIndex >= workingBoard.length || !workingBoard[newIndex].canAccept(currentPlayingColor)) {
            return Optional.empty();
        }
        return Optional.of(new GameMove(workingBoard[index].getId(), workingBoard[newIndex].getId()));
    }

    private Optional<GameMove> generateCollectMove(int diceValue) {
        final var homeColumns = Arrays.stream(getBoardViewForCurrentPlayer()).skip(18).collect(Collectors.toList());
        final var allPiecesHome = homeColumns.stream()
                .mapToInt(BoardColumn::getPieceCount)
                .sum() + getCollectedPiecesForCurrentPlayer() == MAX_PLAYER_PIECES;

        if (!allPiecesHome) {
            return Optional.empty();
        }

        final int complementDiceValue = 6 - diceValue;
        if (homeColumns.get(complementDiceValue).getPieceColor() == currentPlayingColor) {
            return Optional.of(new GameMove(
                    homeColumns.get(complementDiceValue).getId(),
                    collectedByColor.get(currentPlayingColor).getId()));
        }

        return IntStream.range(0, 6)
                .filter(index -> homeColumns.get(index).getPieceColor() == currentPlayingColor)
                .boxed()
                .findFirst()
                .filter(foundIndex -> foundIndex >= complementDiceValue)
                .map(index -> new GameMove(homeColumns.get(index).getId(), collectedByColor.get(currentPlayingColor).getId()));
    }

    private int getCollectedPiecesForCurrentPlayer() {
        return collectedByColor.get(currentPlayingColor).getPieceCount();
    }

    private int getSuspendedPiecesForCurrentPlayer() {
        return suspendedByColor.get(currentPlayingColor).getPieceCount();
    }

    private BoardColumn[] getBoardViewForCurrentPlayer() {
        return currentPlayingColor == Color.BLACK ? blackViewBoardColumns : whiteViewBoardColumns;
    }

    @Override
    public boolean isGameComplete() {
        return collectedByColor.values().stream()
                .mapToInt(BoardColumn::getPieceCount)
                .anyMatch(collected -> collected == MAX_PLAYER_PIECES);
    }

    @Override
    public String toString() {
        return BoardFactory.display(blackViewBoardColumns, whiteViewBoardColumns,
                suspendedByColor.get(Color.BLACK).getPieceCount(), suspendedByColor.get(Color.WHITE).getPieceCount(),
                collectedByColor.get(Color.BLACK).getPieceCount(), collectedByColor.get(Color.WHITE).getPieceCount());
    }
}
