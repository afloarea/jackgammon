package com.github.afloarea.jackgammon.juliette.board.moves.generator;

import com.github.afloarea.jackgammon.juliette.board.layout.ColumnSequence;
import com.github.afloarea.jackgammon.juliette.board.Constants;
import com.github.afloarea.jackgammon.juliette.board.Direction;

/**
 * A calculator that checks that a piece can be moved to a column on the board (excludes collect-type moves).
 */
public final class BasicMoveCalculator extends AbstractMoveCalculator {

    public BasicMoveCalculator(ColumnSequence columnSequence) {
        super(columnSequence);
    }

    @Override
    protected boolean canMoveTo(int to, Direction direction) {
        return to < Constants.COLLECT_INDEX && columnSequence.getColumn(to, direction).isClearForDirection(direction);
    }
}
