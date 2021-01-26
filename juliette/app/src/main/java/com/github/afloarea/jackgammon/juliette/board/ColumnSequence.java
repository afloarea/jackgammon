package com.github.afloarea.jackgammon.juliette.board;

import java.util.stream.Stream;

public interface ColumnSequence {

    BoardColumn getColumn(int index, Direction direction);

    Stream<BoardColumn> stream(Direction direction);

    int getUncollectableCount(Direction direction);

    boolean isSuspendColumn(BoardColumn column, Direction direction);

    int getColumnIndex(BoardColumn column, Direction direction);

    BoardColumn getColumnById(String columnId);
}
