package com.github.afloarea.jackgammon.juliette.neural;

import com.github.afloarea.obge.Direction;
import com.github.afloarea.obge.board.BoardSnapshot;
import com.github.afloarea.obge.board.ColumnSnapshot;

import java.util.stream.DoubleStream;

public final class TdMapper {

    public static double[] mapToNeuralInputs(Direction direction, BoardSnapshot board) {
        final var specialColumnsStream = DoubleStream.builder()
                .add(mapSuspendCountToInput(board.getSuspended(direction)))
                .add(mapSuspendCountToInput(board.getSuspended(direction.reverse())))
                .add(mapCollectCountToInput(board.getCollected(direction)))
                .add(mapCollectCountToInput(board.getCollected(direction.reverse())))
                .build();

        final var normalColumnsStream = board.stream(direction)
                .flatMapToDouble(column -> mapNormalColumnToStream(direction, column));

        final var inputStream = DoubleStream.concat(specialColumnsStream, normalColumnsStream);
        return inputStream.toArray(); //TODO: maybe reuse array to reduce memory footprint
    }

    private static DoubleStream mapNormalColumnToStream(Direction playingDirection, ColumnSnapshot column) {
        final int pieceCount = column.pieceCount();
        if (pieceCount == 0) {
            return DoubleStream.generate(() -> 0).limit(8);
        }
        double first = 1;
        double second = 0;
        double third = 0;
        double fourth = 0;

        if (pieceCount >= 2) {
            second = 1;
        }
        if (pieceCount >= 3) {
            third = 1;
        }
        if (pieceCount > 3) {
            fourth = (pieceCount - 3) / 2D;
        }

        final var currentDirection = DoubleStream.of(first, second, third, fourth);
        final var oppositeDirection = DoubleStream.of(0, 0, 0, 0);

        return column.elementsDirection() == playingDirection
                ? DoubleStream.concat(currentDirection, oppositeDirection)
                : DoubleStream.concat(oppositeDirection, currentDirection);
    }

    private static double mapSuspendCountToInput(int suspendCount) {
        return suspendCount / 2D;
    }

    private static double mapCollectCountToInput(int collectCount) {
        return collectCount / 15D;
    }

    private TdMapper() {
    }
}