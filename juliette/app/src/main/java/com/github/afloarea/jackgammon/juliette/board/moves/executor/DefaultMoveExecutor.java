package com.github.afloarea.jackgammon.juliette.board.moves.executor;

import com.github.afloarea.jackgammon.juliette.GameMove;
import com.github.afloarea.jackgammon.juliette.board.layout.ColumnSequence;
import com.github.afloarea.jackgammon.juliette.board.Direction;
import com.github.afloarea.jackgammon.juliette.board.Move;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Default implementation of the {@link MoveExecutor} that splits a complex move
 * into simple moves and executes each move.
 */
public final class DefaultMoveExecutor implements MoveExecutor {
    private final ColumnSequence columns;

    public DefaultMoveExecutor(ColumnSequence columns) {
        this.columns = columns;
    }

    @Override
    public List<GameMove> executeMove(Move move, Direction direction) {
        return split(move, direction)
                .flatMap(gameMove -> performBasicMove(gameMove, direction))
                .collect(Collectors.toList());
    }

    private Stream<GameMove> split(Move move, Direction direction) {
        final var splitMoves = new ArrayList<GameMove>();
        int fromIndex = columns.getColumnIndex(move.getSource(), direction);

        for (int distance : move.getDistances()) {
            final int newIndex = fromIndex + distance;
            splitMoves.add(new GameMove(
                    columns.getColumn(fromIndex, direction).getId(),
                    columns.getColumn(newIndex, direction).getId()));
            fromIndex = newIndex;
        }

        splitMoves.add(new GameMove(columns.getColumn(fromIndex, direction).getId(), move.getTarget().getId()));
        return splitMoves.stream();
    }

    private Stream<GameMove> performBasicMove(GameMove move, Direction direction) {
        final var sourceColumn = columns.getColumnById(move.getFrom());
        final var targetColumn = columns.getColumnById(move.getTo());

        final var executedMoves = new ArrayList<GameMove>();
        final var oppositeDirection = direction.reverse();

        if (targetColumn.getMovingDirectionOfElements() == oppositeDirection) {
            final var suspendColumn = columns.getSuspendedColumn(oppositeDirection);
            suspendColumn.addElement(oppositeDirection);
            targetColumn.removeElement();
            executedMoves.add(new GameMove(targetColumn.getId(), suspendColumn.getId()));
        }

        targetColumn.addElement(direction);
        sourceColumn.removeElement();
        executedMoves.add(move);

        return executedMoves.stream();
    }
}
