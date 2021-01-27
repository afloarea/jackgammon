package com.github.afloarea.jackgammon.juliette.board.layout;

import com.github.afloarea.jackgammon.juliette.board.BoardColumn;
import com.github.afloarea.jackgammon.juliette.board.Direction;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.github.afloarea.jackgammon.juliette.board.Constants.COLLECT_INDEX;

/**
 * A symbolic representation of the column within a backgammon game.
 * Each column has an alphabetic id, starting with A from the upper left corner,
 * ending with L in the upper right, and then continuing with M from lower left to X to the lower right.
 *
 * The columns for suspended pieces have an index of 0 and an id starting with S.
 * The columns for collected pieces have an index of 25 and an id starting with C.
 * The rest of the column have an index in the [1, 24] inclusive.
 *
 * This implementation provides constant time for most of it's operations.
 */
public final class ColumnArrangement implements ColumnSequence {

    private final Map<String, BoardColumn> columnsById;
    private final Map<Direction, BoardColumn[]> columnsByDirection;
    private final Map<Direction, Map<String, Integer>> columnPositionByIdByDirection = new EnumMap<>(Direction.class);

    @Override
    public BoardColumn getColumn(int index, Direction direction) {
        return columnsByDirection.get(direction)[index];
    }

    @Override
    public Stream<BoardColumn> stream(Direction direction) {
        return Arrays.stream(columnsByDirection.get(direction)).limit(COLLECT_INDEX);
    }

    @Override
    public int countPiecesUpToIndex(int index, Direction direction) {
        return Arrays.stream(columnsByDirection.get(direction))
                .limit(index)
                .filter(column -> column.getMovingDirectionOfElements() == direction)
                .mapToInt(BoardColumn::getPieceCount)
                .sum();
    }

    @Override
    public int getColumnIndex(BoardColumn column, Direction direction) {
        return columnPositionByIdByDirection.get(direction).get(column.getId());
    }

    @Override
    public BoardColumn getColumnById(String columnId) {
        return columnsById.get(columnId);
    }

    /**
     * Construct a column arrangement.
     * @param columnLayout the columnLayout containing the normal columns (with ids from A to X)
     * @param forwardSuspended the number of suspended pieces for the forward direction
     * @param backwardsSuspended the number of suspended pieces for the backwards direction
     * @param forwardCollected the number of collected pieces for the forward direction
     * @param backwardsCollected the number of colllected pieces for the backwards direction
     */
    public ColumnArrangement(List<BoardColumn> columnLayout,
                             int forwardSuspended, int backwardsSuspended,
                             int forwardCollected, int backwardsCollected) {
        final var base = new ArrayList<>(columnLayout);

        final var forward = new ArrayDeque<>(base);
        final var suspendForward = new BoardColumn(forwardSuspended, Direction.FORWARD, "SB");
        final var collectForward = new BoardColumn(forwardCollected, Direction.FORWARD, "CB");
        forward.addFirst(suspendForward);
        forward.addLast(collectForward);

        Collections.reverse(base);
        final var backward = new ArrayDeque<>(base);
        final var suspendBackwards = new BoardColumn(backwardsSuspended, Direction.BACKWARD, "SW");
        final var collectBackwards = new BoardColumn(backwardsCollected, Direction.BACKWARD, "CW");
        backward.addFirst(suspendBackwards);
        backward.addLast(collectBackwards);

        columnsByDirection = Map.of(
                Direction.FORWARD, forward.toArray(BoardColumn[]::new),
                Direction.BACKWARD, backward.toArray(BoardColumn[]::new));

        Stream.of(Direction.FORWARD, Direction.BACKWARD).forEach(direction -> {
            final Map<String, Integer> columnIdByIndex = new HashMap<>();

            final var columns = columnsByDirection.get(direction);
            IntStream.range(0, columns.length).forEach(index -> columnIdByIndex.put(columns[index].getId(), index));

            columnPositionByIdByDirection.put(direction, columnIdByIndex);
        });

        columnsById = Stream.concat(
                base.stream(), Stream.of(suspendBackwards, suspendForward, collectBackwards, collectForward))
                .collect(Collectors.toMap(BoardColumn::getId, Function.identity()));

    }
}
