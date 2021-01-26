package com.github.afloarea.jackgammon.juliette.board;

import java.util.List;
import java.util.stream.Stream;

public final class PermissiveCollectMoveCalculator extends AbstractMoveCalculator {

    private boolean checkedOnce = false;

    public PermissiveCollectMoveCalculator(ColumnSequence columnSequence) {
        super(columnSequence);
    }

    @Override
    public final Stream<Move> computeMovesFromStart(int startIndex, List<Integer> availableHops, Direction direction) {
        checkedOnce = false;
        return super.computeMovesFromStart(startIndex, availableHops, direction);
    }

    @Override
    protected boolean canMoveTo(int to, Direction direction) {
        if (to < 25) {
            return columnSequence.getColumn(to, direction).isClearForDirection(direction);
        }
        if (checkedOnce) {
            return false;
        }
        checkedOnce = true;
        return true;
    }
}
