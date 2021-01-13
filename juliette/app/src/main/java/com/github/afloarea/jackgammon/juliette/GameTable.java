package com.github.afloarea.jackgammon.juliette;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class GameTable implements GameBoard {
    private static final int MAX_PLAYER_PIECES = 15;

    private BoardColumn[] blackViewBoardColumns = new BoardColumn[24];
    private BoardColumn[] whiteViewBoardColumns = new BoardColumn[24];
    private int suspendedBlackPieces = 0;
    private int suspendedWhitePieces = 0;
    private int collectedBlackPieces = 0;
    private int collectedWhitePieces = 0;

    private Color currentPlayingColor = Color.NONE;
    private DiceResult currentDiceResult;
    private List<Integer> remainingDiceValues = new ArrayList<>();

    // possible moves for current player
    private Set<GameMove> possibleMoves = new HashSet<>();

    private void initBoard() {
        // initialize the board
    }

    @Override
    public void updateDiceForPlayingColor(Color playingColor, DiceResult dice) {
        if (playingColor == Color.NONE) {
            throw new IllegalGameActionException("None color cannot update the dice");
        }
        if (!remainingDiceValues.isEmpty()) {
            throw new IllegalGameActionException("Cannot update dice. Turn is not yet over");
        }
        if (playingColor != currentPlayingColor.complement()) {
            throw new IllegalGameActionException("Wrong player color rolled dice.");
        }
        // + other validations

        currentPlayingColor = playingColor;
        currentDiceResult = dice;
        dice.stream().forEach(remainingDiceValues::add);

        // update possible moves

    }

    private Set<GameMove> computePossibleMovesForCurrentPlayer() {
        return remainingDiceValues.stream()
                .distinct()
                .map(this::computePossibleMovesForCurrentPlayer)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }

    private Set<GameMove> computePossibleMovesForCurrentPlayer(int diceValue) {
        final var workingBoard = getBoardViewForCurrentPlayer();

        // enter-type move
        if (getSuspendedPiecesForCurrentPlayer() > 0) {
            final var column = workingBoard[diceValue - 1];
            return column.canAccept(currentPlayingColor)
                    ? Set.of(new GameMove(MoveType.ENTER, -1, column.getPosition()))
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
                .filter(index -> workingBoard[index].pieceColor == currentPlayingColor)
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
        return Optional.of(new GameMove(
                MoveType.SIMPLE,
                workingBoard[index].getPosition(),
                workingBoard[newIndex].getPosition()));
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
        return IntStream.range(0, 6)
                .filter(index -> homeColumns.get(index).pieceColor == currentPlayingColor)
                .boxed()
                .findFirst()
                .filter(foundIndex -> foundIndex >= complementDiceValue)
                .map(index -> new GameMove(MoveType.COLLECT, homeColumns.get(index).getPosition(), -1));
    }

    private int getCollectedPiecesForCurrentPlayer() {
        return currentPlayingColor == Color.BLACK ? collectedBlackPieces : collectedWhitePieces;
    }

    private int getSuspendedPiecesForCurrentPlayer() {
        return currentPlayingColor == Color.BLACK ? suspendedBlackPieces : suspendedWhitePieces;
    }

    private BoardColumn[] getBoardViewForCurrentPlayer() {
        return currentPlayingColor == Color.BLACK ? blackViewBoardColumns : whiteViewBoardColumns;
    }

    @Override
    public boolean isGameComplete() {
        return collectedBlackPieces == MAX_PLAYER_PIECES || collectedWhitePieces == MAX_PLAYER_PIECES;
    }


}
