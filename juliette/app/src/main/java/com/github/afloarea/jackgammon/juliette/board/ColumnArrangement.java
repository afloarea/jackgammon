package com.github.afloarea.jackgammon.juliette.board;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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
        return Arrays.stream(columnsByDirection.get(direction));
    }

    @Override
    public int getUncollectableCount(Direction direction) {
        final int sum = Arrays.stream(columnsByDirection.get(direction))
                .skip(19).limit(7)
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
        return getColumn(0, direction);
    }

    @Override
    public BoardColumn getCollectColumn(Direction direction) {
        return getColumn(25, direction);
    }

    public ColumnArrangement(int[][] values,
                             int forwardSuspended, int backwardSuspended,
                             int forwardCollected, int backwardCollected) {
        final var base = BoardFactory.translateToColumns(values);

        final var forward = new ArrayDeque<>(base);
        final var suspendForward = new BoardColumn(forwardSuspended, Direction.FORWARD, "SB");
        final var collectForward = new BoardColumn(forwardCollected, Direction.FORWARD, "CB");
        forward.addFirst(suspendForward);
        forward.addLast(collectForward);

        Collections.reverse(base);
        final var backward = new ArrayDeque<>(base);
        final var suspendBackwards = new BoardColumn(backwardSuspended, Direction.BACKWARD, "SW");
        final var collectBackwards = new BoardColumn(backwardCollected, Direction.BACKWARD, "CW");
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
