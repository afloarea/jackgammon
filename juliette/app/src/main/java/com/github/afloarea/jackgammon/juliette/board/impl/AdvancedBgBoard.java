package com.github.afloarea.jackgammon.juliette.board.impl;

import com.github.afloarea.jackgammon.juliette.board.BgBoard;
import com.github.afloarea.jackgammon.juliette.board.BgMove;
import com.github.afloarea.jackgammon.juliette.board.DiceRoll;
import com.github.afloarea.jackgammon.juliette.board.Direction;
import com.github.afloarea.jackgammon.juliette.board.common.Move;
import com.github.afloarea.jackgammon.juliette.board.exceptions.IllegalBgActionException;
import com.github.afloarea.jackgammon.juliette.board.layout.ColumnSequence;
import com.github.afloarea.jackgammon.juliette.board.moves.executor.DefaultMoveExecutor;
import com.github.afloarea.jackgammon.juliette.board.moves.executor.MoveExecutor;
import com.github.afloarea.jackgammon.juliette.board.moves.generator.DefaultMoveProvider;
import com.github.afloarea.jackgammon.juliette.board.moves.generator.PossibleMovesProvider;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.afloarea.jackgammon.juliette.board.common.Constants.PIECES_PER_PLAYER;

public final class AdvancedBgBoard implements BgBoard {

    private final Map<BgMove, Move> movesMap = new HashMap<>();
    private Direction currentDirection = Direction.NONE;

    private final List<Integer> remainingDiceValues = new ArrayList<>();

    private final ColumnSequence columns;
    private final PossibleMovesProvider defaultMoveProvider;
    private final MoveExecutor moveExecutor;

    public AdvancedBgBoard(ColumnSequence columns) {
        this.columns = columns;
        this.defaultMoveProvider = new DefaultMoveProvider(columns);
        this.moveExecutor = new DefaultMoveExecutor(columns);
    }

    @Override
    public void applyDiceRoll(Direction direction, DiceRoll dice) {
        validateDirection(direction);

        currentDirection = direction;
        dice.stream().boxed().forEach(remainingDiceValues::add);

        updatePossibleMoves();
    }

    private void validateDirection(Direction direction) {
        if (isGameComplete()) {
            throw new IllegalBgActionException("Unable to roll dice. Game is finished");
        }
        if (!remainingDiceValues.isEmpty()) {
            throw new IllegalBgActionException("Cannot update dice. Turn is not yet over");
        }
        if (direction == null || direction == Direction.NONE) {
            throw new IllegalBgActionException("Invalid direction provided");
        }
        if (direction != currentDirection.reverse() && currentDirection != Direction.NONE) {
            throw new IllegalBgActionException("Wrong player color rolled dice.");
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
                                move -> BgMove.of(move.getSource().getId(), move.getTarget().getId()),
                                Function.identity()));

        movesMap.clear();
        movesMap.putAll(computedMoves);

        if (movesMap.isEmpty()) {
            remainingDiceValues.clear();
        }
    }

    @Override
    public boolean isCurrentTurnDone() {
        return remainingDiceValues.isEmpty();
    }

    @Override
    public Set<BgMove> getPossibleMoves() {
        return Set.copyOf(movesMap.keySet());
    }

    @Override
    public List<BgMove> execute(Direction direction, BgMove move) {
        final Move selectedMove = getSelectedMove(direction, move);
        selectedMove.getDistances().forEach(remainingDiceValues::remove);
        final var executedMoves = moveExecutor.executeMove(selectedMove, currentDirection);
        updatePossibleMoves(); // update after executing the moves
        return executedMoves;
    }

    private Move getSelectedMove(Direction direction, BgMove move) {
        if (isGameComplete()) {
            throw new IllegalBgActionException("Game is complete. No more moves allowed");
        }

        if (direction != currentDirection || direction == Direction.NONE) {
            throw new IllegalBgActionException("Incorrect direction provided");
        }

        final var selectedMove = movesMap.get(move);
        if (selectedMove == null) {
            throw new IllegalBgActionException("Invalid move provided");
        }
        return selectedMove;
    }

    @Override
    public boolean isGameComplete() {
        return Stream.of(Direction.CLOCKWISE, Direction.ANTICLOCKWISE)
                .map(columns::getCollectColumn)
                .anyMatch(column -> column.getPieceCount() == PIECES_PER_PLAYER);
    }

    @Override
    public Direction getCurrentTurnDirection() {
        return currentDirection;
    }

    @Override
    public Direction getWinningDirection() {
        return Stream.of(Direction.CLOCKWISE, Direction.ANTICLOCKWISE)
                .filter(direction -> columns.getCollectColumn(direction).getPieceCount() == PIECES_PER_PLAYER)
                .findAny()
                .orElse(Direction.NONE);
    }
}
