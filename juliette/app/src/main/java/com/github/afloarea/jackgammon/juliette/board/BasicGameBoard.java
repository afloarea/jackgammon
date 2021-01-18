package com.github.afloarea.jackgammon.juliette.board;

import com.github.afloarea.jackgammon.juliette.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

    private final Map<Color, Integer> suspendedPieces = new EnumMap<>(Map.of(Color.BLACK, 0, Color.WHITE, 0));
    private final Map<Color, Integer> collectedPieces = new EnumMap<>(Map.of(Color.BLACK, 0, Color.WHITE, 0));

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
        suspendedPieces.put(Color.BLACK, suspendedBlack);
        suspendedPieces.put(Color.WHITE, suspendedWhite);
        collectedPieces.put(Color.BLACK, collectedBlack);
        collectedPieces.put(Color.WHITE, collectedWhite);
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
        return collectedPieces.entrySet().stream()
                .filter(entry -> entry.getValue() == MAX_PLAYER_PIECES)
                .map(Map.Entry::getKey)
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
    public void executeMoveForPlayingColor(Color playingColor, GameMove move) {
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

        final int usedDiceValue = optionalDiceValueUsed.get();
        if (move.getType() == MoveType.COLLECT) {
            blackViewBoardColumns[move.getFrom()].removePiece();
            collectPiece();
            remainingDiceValues.remove((Integer) usedDiceValue);
            updatePossibleMoves();
            return;
        }

        if (move.getType() == MoveType.ENTER) {
            removeSuspendedPiece();
        } else { // simple move
            blackViewBoardColumns[move.getFrom()].removePiece();
        }

        // add piece to new position, suspending opponent piece if necessary
        final var targetColumn = blackViewBoardColumns[move.getTo()];
        if (targetColumn.getPieceColor() == currentPlayingColor.complement()) {
            targetColumn.removePiece();
            suspendPiece();
        }
        targetColumn.addPiece(currentPlayingColor);

        remainingDiceValues.remove((Integer) usedDiceValue);
        updatePossibleMoves();
    }

    private void suspendPiece() {
        suspendedPieces.compute(currentPlayingColor.complement(), (k, v) -> v + 1);
    }

    private void removeSuspendedPiece() {
        suspendedPieces.compute(currentPlayingColor, (k, v) -> v - 1);
    }

    private void collectPiece() {
        collectedPieces.compute(currentPlayingColor, (k, v) -> v + 1);
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
                    ? Set.of(GameMove.enter(column.getPosition()))
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
        return Optional.of(
                GameMove.move(workingBoard[index].getPosition(), workingBoard[newIndex].getPosition()));
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
            return Optional.of(GameMove.collect(homeColumns.get(complementDiceValue).getPosition()));
        }

        return IntStream.range(0, 6)
                .filter(index -> homeColumns.get(index).getPieceColor() == currentPlayingColor)
                .boxed()
                .findFirst()
                .filter(foundIndex -> foundIndex >= complementDiceValue)
                .map(index -> GameMove.collect(homeColumns.get(index).getPosition()));
    }

    private int getCollectedPiecesForCurrentPlayer() {
        return collectedPieces.get(currentPlayingColor);
    }

    private int getSuspendedPiecesForCurrentPlayer() {
        return suspendedPieces.get(currentPlayingColor);
    }

    private BoardColumn[] getBoardViewForCurrentPlayer() {
        return currentPlayingColor == Color.BLACK ? blackViewBoardColumns : whiteViewBoardColumns;
    }

    @Override
    public boolean isGameComplete() {
        return collectedPieces.values().stream().anyMatch(collected -> collected == MAX_PLAYER_PIECES);
    }

    @Override
    public String toString() {
        return BoardFactory.display(blackViewBoardColumns, whiteViewBoardColumns,
                suspendedPieces.get(Color.BLACK), suspendedPieces.get(Color.WHITE),
                collectedPieces.get(Color.BLACK), collectedPieces.get(Color.WHITE));
    }
}
