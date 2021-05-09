package com.github.afloarea.jackgammon.juliette.board.layout;

import com.github.afloarea.jackgammon.juliette.board.Direction;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.github.afloarea.jackgammon.juliette.board.common.Constants.COLLECT_INDEX;

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
     * @param clockwiseSuspended the number of suspended pieces for the clockwise direction
     * @param anticlockwiseSuspended the number of suspended pieces for the anticlockwise direction
     * @param clockwiseCollected the number of collected pieces for the clockwise direction
     * @param anticlockwiseCollected the number of collected pieces for the anticlockwise direction
     */
    public ColumnArrangement(List<BoardColumn> columnLayout,
                             int clockwiseSuspended, int anticlockwiseSuspended,
                             int clockwiseCollected, int anticlockwiseCollected) {
        final var base = new ArrayList<>(columnLayout);

        final var clockwise = new ArrayDeque<>(base);
        final var suspendClockwise = new BoardColumn(clockwiseSuspended, Direction.CLOCKWISE, "SB");
        final var collectClockwise = new BoardColumn(clockwiseCollected, Direction.CLOCKWISE, "CB");
        clockwise.addFirst(suspendClockwise);
        clockwise.addLast(collectClockwise);

        Collections.reverse(base);
        final var anticlockwise = new ArrayDeque<>(base);
        final var suspendAnticlockwise = new BoardColumn(anticlockwiseSuspended, Direction.ANTICLOCKWISE, "SW");
        final var collectAnticlockwise = new BoardColumn(anticlockwiseCollected, Direction.ANTICLOCKWISE, "CW");
        anticlockwise.addFirst(suspendAnticlockwise);
        anticlockwise.addLast(collectAnticlockwise);

        columnsByDirection = Map.of(
                Direction.CLOCKWISE, clockwise.toArray(BoardColumn[]::new),
                Direction.ANTICLOCKWISE, anticlockwise.toArray(BoardColumn[]::new));

        Stream.of(Direction.CLOCKWISE, Direction.ANTICLOCKWISE).forEach(direction -> {
            final Map<String, Integer> columnIdByIndex = new HashMap<>();

            final var columns = columnsByDirection.get(direction);
            IntStream.range(0, columns.length).forEach(index -> columnIdByIndex.put(columns[index].getId(), index));

            columnPositionByIdByDirection.put(direction, columnIdByIndex);
        });

        columnsById = Stream.concat(
                base.stream(), Stream.of(suspendAnticlockwise, suspendClockwise, collectAnticlockwise, collectClockwise))
                .collect(Collectors.toMap(BoardColumn::getId, Function.identity()));

    }
}
