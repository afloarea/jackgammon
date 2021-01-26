package com.github.afloarea.jackgammon.juliette.board;

public final class BasicMoveCalculator extends AbstractMoveCalculator {

    public BasicMoveCalculator(ColumnSequence columnSequence) {
        super(columnSequence);
    }

    @Override
    protected boolean canMoveTo(int to, Direction direction) {
        return to < 25 && columnSequence.getColumn(to, direction).isClearForDirection(direction);
    }
}
