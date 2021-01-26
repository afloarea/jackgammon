package com.github.afloarea.jackgammon.juliette.board;

import com.github.afloarea.jackgammon.juliette.Color;
import com.github.afloarea.jackgammon.juliette.DiceResult;
import com.github.afloarea.jackgammon.juliette.GameMove;
import com.github.afloarea.jackgammon.juliette.IllegalGameActionException;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AdvancedGameBoard implements GameBoard {
    private static final Map<Color, Direction> DIRECTIONS_BY_COLOR = Map.of(
            Color.BLACK, Direction.FORWARD,
            Color.WHITE, Direction.BACKWARD,
            Color.NONE, Direction.NONE);

    private final Map<GameMove, Move> movesMap = new HashMap<>();
    private Color currentPlayingColor = Color.NONE;

    private final List<Integer> remainingDiceValues = new ArrayList<>();

    private final TurnLogic turnLogic;
    private final ColumnSequence columns;

    public AdvancedGameBoard(ColumnSequence columns) {
        this.columns = columns;
        this.turnLogic = new TurnLogic(columns);
    }

    @Override
    public void updateDiceForPlayingColor(Color playingColor, DiceResult dice) {
        performRollValidation(playingColor, dice);

        currentPlayingColor = playingColor;
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

    private void updatePossibleMoves() {
        final var computedMoves =
                turnLogic.streamPossibleMoves(remainingDiceValues, DIRECTIONS_BY_COLOR.get(currentPlayingColor))
                        .collect(Collectors.toMap(
                                move -> new GameMove(move.getSource().getId(), move.getTarget().getId()),
                                Function.identity()));

        movesMap.clear();
        movesMap.putAll(computedMoves);

        if (movesMap.isEmpty()) {
            remainingDiceValues.clear();
        }
    }

    @Override
    public boolean currentPlayingColorFinishedTurn() {
        return remainingDiceValues.isEmpty();
    }

    @Override
    public Set<GameMove> getPossibleMovesForCurrentPlayingColor() {
        return Set.copyOf(movesMap.keySet());
    }

    @Override
    public List<GameMove> executeMoveForPlayingColor(Color playingColor, GameMove move) {
        if (isGameComplete()) {
            throw new IllegalGameActionException("Game is complete. No more moves allowed");
        }

        if (playingColor != currentPlayingColor || playingColor == Color.NONE) {
            throw new IllegalGameActionException("Incorrect playing color provided");
        }

        final Move selectedMove = movesMap.get(move);
        remainingDiceValues.removeAll(selectedMove.getDistances());
        final var executedMoves = split(selectedMove)
                .flatMap(this::performBasicMove)
                .collect(Collectors.toList());
        updatePossibleMoves(); // update after executing the moves
        return executedMoves;
    }

    Stream<GameMove> split(Move move) {
        final var currentDirection = DIRECTIONS_BY_COLOR.get(currentPlayingColor);
        final var splitMoves = new ArrayList<GameMove>();
        int fromIndex = columns.getColumnIndex(move.getSource(), currentDirection);

        for (int index = 0; index < move.getDistances().size() - 1; index++) {
            final int newIndex = fromIndex + currentDirection.getSign() * move.getDistances().get(index);
            splitMoves.add(new GameMove(
                    columns.getColumn(fromIndex, currentDirection).getId(),
                    columns.getColumn(newIndex, currentDirection).getId()));
            fromIndex = newIndex;
        }

        splitMoves.add(new GameMove(columns.getColumn(fromIndex, currentDirection).getId(), move.getTarget().getId()));
        return splitMoves.stream();
    }

    Stream<GameMove> performBasicMove(GameMove move) {
        final var sourceColumn = columns.getColumnById(move.getFrom());
        final var targetColumn = columns.getColumnById(move.getTo());

        final var executedMoves = new ArrayList<GameMove>();
        final var oppositeDirection = DIRECTIONS_BY_COLOR.get(currentPlayingColor.complement());
        final var currentDirection = DIRECTIONS_BY_COLOR.get(currentPlayingColor);

        if (targetColumn.getMovingDirectionOfElements() == oppositeDirection) {
            final var suspendColumn = columns.getSuspendedColumn(oppositeDirection);
            suspendColumn.addElement(oppositeDirection);
            targetColumn.removeElement();
            executedMoves.add(new GameMove(targetColumn.getId(), suspendColumn.getId()));
        }

        targetColumn.addElement(currentDirection);
        sourceColumn.removeElement();
        executedMoves.add(move);

        return executedMoves.stream();
    }

    @Override
    public boolean isGameComplete() {
        return Stream.of(Direction.FORWARD, Direction.BACKWARD)
                .map(columns::getCollectColumn)
                .anyMatch(column -> column.getPieceCount() == 15);
    }

    @Override
    public Color getCurrentPlayingColor() {
        return currentPlayingColor;
    }

    @Override
    public Color getWinningColor() {
        return DIRECTIONS_BY_COLOR.entrySet().stream()
                .filter(entry -> columns.getCollectColumn(entry.getValue()).getPieceCount() == 15)
                .map(Map.Entry::getKey)
                .findAny()
                .orElse(Color.NONE);
    }

    @Override
    public Color getLosingColor() {
        return getWinningColor().complement();
    }
}
