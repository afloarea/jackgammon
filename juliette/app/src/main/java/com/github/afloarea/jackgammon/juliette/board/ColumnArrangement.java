package com.github.afloarea.jackgammon.juliette.board;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.github.afloarea.jackgammon.juliette.board.Constants.*;

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
    public int getUncollectableCount(Direction direction) {
        final int sum = Arrays.stream(columnsByDirection.get(direction))
                .skip(HOME_START).limit(HOME_AND_COLLECT)
                .filter(column -> column.getMovingDirectionOfElements() == direction)
                .mapToInt(BoardColumn::getPieceCount)
                .sum();
        return Constants.PIECES_PER_PLAYER - sum;
    }

    @Override
    public int getColumnIndex(BoardColumn column, Direction direction) {
        return columnPositionByIdByDirection.get(direction).get(column.getId());
    }

    @Override
    public BoardColumn getColumnById(String columnId) {
        return columnsById.get(columnId);
    }

    @Override
    public BoardColumn getSuspendedColumn(Direction direction) {
        return getColumn(SUSPEND_INDEX, direction);
    }

    @Override
    public BoardColumn getCollectColumn(Direction direction) {
        return getColumn(COLLECT_INDEX, direction);
    }

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
