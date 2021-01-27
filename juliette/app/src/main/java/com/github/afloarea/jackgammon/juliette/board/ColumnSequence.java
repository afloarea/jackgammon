package com.github.afloarea.jackgammon.juliette.board;

import java.util.stream.Stream;

public interface ColumnSequence {

    /**
     * Retrieve a column by index by traversing the columns in the specified direction.
     * @param index the index
     * @param direction the direction
     * @return the column
     */
    BoardColumn getColumn(int index, Direction direction);

    Stream<BoardColumn> stream(Direction direction);

    int getUncollectableCount(Direction direction);

    int getColumnIndex(BoardColumn column, Direction direction);

    BoardColumn getColumnById(String columnId);

    BoardColumn getSuspendedColumn(Direction direction);

    BoardColumn getCollectColumn(Direction direction);
}
