package com.github.afloarea.jackgammon.juliette.board;

import java.util.stream.Stream;

public final class ColumnArrangement implements ColumnSequence {

    @Override
    public BoardColumn getColumn(int index, Direction direction) {
        return null;
    }

    @Override
    public Stream<BoardColumn> stream(Direction direction) {
        return null;
    }

    @Override
    public int getUncollectableCount(Direction direction) {
        return 0;
    }

    @Override
    public boolean isSuspendColumn(BoardColumn column, Direction direction) {
        return false;
    }

    @Override
    public int getColumnIndex(BoardColumn column, Direction direction) {
        return 0;
    }
}
