package com.github.afloarea.jackgammon.juliette.board;

public final class StrictCollectMoveCalculator extends AbstractMoveCalculator {

    public StrictCollectMoveCalculator(ColumnSequence columnSequence) {
        super(columnSequence);
    }

    @Override
    protected boolean canMoveTo(int to, Direction direction) {
        if (to > 25) {
            return false;
        }
        if (to == 25) {
            return true;
        }
        return columnSequence.getColumn(to, direction).isClearForDirection(direction);
    }
}
