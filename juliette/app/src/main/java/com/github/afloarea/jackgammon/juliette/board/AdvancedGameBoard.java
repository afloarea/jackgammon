package com.github.afloarea.jackgammon.juliette.board;

import com.github.afloarea.jackgammon.juliette.DiceResult;
import com.github.afloarea.jackgammon.juliette.GameMove;
import com.github.afloarea.jackgammon.juliette.IllegalGameActionException;
import com.github.afloarea.jackgammon.juliette.board.layout.ColumnSequence;
import com.github.afloarea.jackgammon.juliette.board.moves.executor.DefaultMoveExecutor;
import com.github.afloarea.jackgammon.juliette.board.moves.executor.MoveExecutor;
import com.github.afloarea.jackgammon.juliette.board.moves.generator.DefaultMoveProvider;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.afloarea.jackgammon.juliette.board.Constants.PIECES_PER_PLAYER;

public final class AdvancedGameBoard implements GameBoard {

    private final Map<GameMove, Move> movesMap = new HashMap<>();
    private Direction currentDirection = Direction.NONE;

    private final List<Integer> remainingDiceValues = new ArrayList<>();

    private final ColumnSequence columns;
    private final DefaultMoveProvider defaultMoveProvider;
    private final MoveExecutor moveExecutor;

    public AdvancedGameBoard(ColumnSequence columns) {
        this.columns = columns;
        this.defaultMoveProvider = new DefaultMoveProvider(columns);
        this.moveExecutor = new DefaultMoveExecutor(columns);
    }

    @Override
    public void updateDiceForDirection(Direction direction, DiceResult dice) {
        performRollValidation(direction, dice);

        currentDirection = direction;
        dice.stream().boxed().forEach(remainingDiceValues::add);

        updatePossibleMoves();
    }

    private void performRollValidation(Direction direction, DiceResult dice) {
        if (isGameComplete()) {
            throw new IllegalGameActionException("Unable to roll dice. Game is finished");
        }
        if (direction == Direction.NONE) {
            throw new IllegalGameActionException("None color cannot update the dice");
        }
        if (!remainingDiceValues.isEmpty()) {
            throw new IllegalGameActionException("Cannot update dice. Turn is not yet over");
        }
        if (direction != currentDirection.reverse() && currentDirection != Direction.NONE) {
            throw new IllegalGameActionException("Wrong player color rolled dice.");
        }
    }

    private void updatePossibleMoves() {
        if (remainingDiceValues.isEmpty() || isGameComplete()) {
            movesMap.clear();
            return;
        }
        final var computedMoves =
                defaultMoveProvider.streamPossibleMoves(remainingDiceValues, currentDirection)
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
    public boolean currentDirectionMovementIsComplete() {
        return remainingDiceValues.isEmpty();
    }

    @Override
    public Set<GameMove> getCurrentDirectionPossibleMoves() {
        return Set.copyOf(movesMap.keySet());
    }

    @Override
    public List<GameMove> executeMoveInDirection(Direction direction, GameMove move) {
        if (isGameComplete()) {
            throw new IllegalGameActionException("Game is complete. No more moves allowed");
        }

        if (direction != currentDirection || direction == Direction.NONE) {
            throw new IllegalGameActionException("Incorrect playing color provided");
        }

        final Move selectedMove = movesMap.get(move);
        selectedMove.getDistances().forEach(remainingDiceValues::remove);
        final var executedMoves = moveExecutor.executeMove(selectedMove, currentDirection);
        updatePossibleMoves(); // update after executing the moves
        return executedMoves;
    }

    @Override
    public boolean isGameComplete() {
        return Stream.of(Direction.FORWARD, Direction.BACKWARD)
                .map(columns::getCollectColumn)
                .anyMatch(column -> column.getPieceCount() == PIECES_PER_PLAYER);
    }

    @Override
    public Direction getCurrentDirection() {
        return currentDirection;
    }

    @Override
    public Direction getWinningDirection() {
        return Stream.of(Direction.FORWARD, Direction.BACKWARD)
                .filter(direction -> columns.getCollectColumn(direction).getPieceCount() == PIECES_PER_PLAYER)
                .findAny()
                .orElse(Direction.NONE);
    }

    @Override
    public Direction getLosingDirection() {
        return getWinningDirection().reverse();
    }
}
