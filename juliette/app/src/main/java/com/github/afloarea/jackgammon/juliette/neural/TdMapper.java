package com.github.afloarea.jackgammon.juliette.neural;

import com.github.afloarea.obge.Direction;
import com.github.afloarea.obge.board.ColumnInfo;
import com.github.afloarea.obge.board.ObgBoard;

import java.util.stream.DoubleStream;

public final class TdMapper {

    public static double[] mapToNeuralInputs(Direction direction, ObgBoard board) {

        final var specialColumnsStream = DoubleStream.builder()
                .add(mapSuspendColumnToInput(board.getSuspendColumn(direction)))
                .add(mapSuspendColumnToInput(board.getSuspendColumn(direction.reverse())))
                .add(mapCollectColumnToInput(board.getCollectColumn(direction)))
                .add(mapCollectColumnToInput(board.getCollectColumn(direction.reverse())))
                .build();

        final var normalColumnsStream = board.getNormalColumns(direction).stream()
                .flatMapToDouble(column -> mapNormalColumnToStream(direction, column));

        final var inputStream = DoubleStream.concat(specialColumnsStream, normalColumnsStream);
        return inputStream.toArray(); //TODO: maybe reuse array to reduce memory footprint
    }

    private static DoubleStream mapNormalColumnToStream(Direction playingDirection, ColumnInfo column) {
        final int pieceCount = column.getPieceCount();
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

        return column.getMovingDirectionOfElements() == playingDirection
                ? DoubleStream.concat(currentDirection, oppositeDirection)
                : DoubleStream.concat(oppositeDirection, currentDirection);
    }

    private static double mapSuspendColumnToInput(ColumnInfo suspendColumn) {
        return suspendColumn.getPieceCount() / 2D;
    }

    private static double mapCollectColumnToInput(ColumnInfo collectColumn) {
        return collectColumn.getPieceCount();
    }

    private TdMapper() {}
}