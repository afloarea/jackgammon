package com.github.afloarea.jackgammon.juliette.board.moves.executor;

import com.github.afloarea.jackgammon.juliette.board.BgMove;
import com.github.afloarea.jackgammon.juliette.board.common.Constants;
import com.github.afloarea.jackgammon.juliette.board.Direction;
import com.github.afloarea.jackgammon.juliette.board.common.Move;
import com.github.afloarea.jackgammon.juliette.board.layout.ColumnSequence;

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
    public List<BgMove> executeMove(Move move, Direction direction) {
        return split(move, direction)
                .flatMap(gameMove -> performBasicMove(gameMove, direction))
                .collect(Collectors.toList());
    }

    private Stream<BgMove> split(Move move, Direction direction) {
        final var splitMoves = new ArrayList<BgMove>();
        int fromIndex = columns.getColumnIndex(move.getSource(), direction);

        for (int distance : move.getDistances()) {
            final int newIndex = Math.min(fromIndex + distance, Constants.COLLECT_INDEX);
            splitMoves.add(BgMove.of(
                    columns.getColumn(fromIndex, direction).getId(),
                    columns.getColumn(newIndex, direction).getId()));
            fromIndex = newIndex;
        }

        return splitMoves.stream();
    }

    private Stream<BgMove> performBasicMove(BgMove move, Direction direction) {
        final var sourceColumn = columns.getColumnById(move.getSource());
        final var targetColumn = columns.getColumnById(move.getTarget());

        final var executedMoves = new ArrayList<BgMove>();
        final var oppositeDirection = direction.reverse();

        if (targetColumn.getMovingDirectionOfElements() == oppositeDirection) {
            final var suspendColumn = columns.getSuspendedColumn(oppositeDirection);
            suspendColumn.addElement(oppositeDirection);
            targetColumn.removeElement();
            executedMoves.add(BgMove.of(targetColumn.getId(), suspendColumn.getId()));
        }

        targetColumn.addElement(direction);
        sourceColumn.removeElement();
        executedMoves.add(move);

        return executedMoves.stream();
    }
}
