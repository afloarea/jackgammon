package com.github.afloarea.jackgammon.juliette.board.layout;

import com.github.afloarea.jackgammon.juliette.board.BoardColumn;
import com.github.afloarea.jackgammon.juliette.board.Constants;
import com.github.afloarea.jackgammon.juliette.board.Direction;

import java.util.stream.Stream;

/**
 * A sequence of columns that can be queried to obtain information about the layout, such as
 * col
 */
public interface ColumnSequence {

    /**
     * Retrieve a column by index by traversing the columns in the specified direction.
     * @param index the index
     * @param direction the direction
     * @return the column
     */
    BoardColumn getColumn(int index, Direction direction);

    /**
     * Stream the columns in a given direction starting with suspend column, continuing with regular columns
     * and ending with (but not including) the collect column.
     * @param direction the direction in which to stream
     * @return the stream of columns
     */
    Stream<BoardColumn> stream(Direction direction);

    /**
     * Count the number of pieces up to (excluding) the column with the provided index.
     * This also counts suspended pieces (at index 0).
     * @param columnIndex the exclusive limit of the count
     * @param direction the direction
     * @return the number of pieces up to the provided index
     */
    int countPiecesUpToIndex(int columnIndex, Direction direction);

    /**
     * Retrieve the index of a column based on the direction.
     * @param column the column for which to get the index
     * @param direction the direction in which to search
     * @return the index of the column
     */
    int getColumnIndex(BoardColumn column, Direction direction);

    /**
     * Retrieve a column based on it's id.
     * @param columnId the column id
     * @return the column with the columnId
     */
    BoardColumn getColumnById(String columnId);

    /**
     * Retrieve the suspended column based on the given direction.
     * @param direction the direction
     * @return the suspend column
     */
    default BoardColumn getSuspendedColumn(Direction direction) {
        return getColumn(Constants.SUSPEND_INDEX, direction);
    }

    /**
     * Retrieve the collect column based on the given direction.
     * @param direction the direction
     * @return the suspend column
     */
    default BoardColumn getCollectColumn(Direction direction) {
        return getColumn(Constants.COLLECT_INDEX, direction);
    }
}
