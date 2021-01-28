package com.github.afloarea.jackgammon.juliette.board;

import com.github.afloarea.jackgammon.juliette.board.layout.ColumnArrangement;
import com.github.afloarea.jackgammon.juliette.board.layout.ColumnSequence;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public final class BoardFactory {
    private static final String[][] BOARD_TEMPLATE = {
            "ABCDEFGHIJKL".split(""),
            new StringBuilder("MNOPQRSTUVWX").reverse().toString().split("")
    };

    private BoardFactory() {}

    public static AdvancedGameBoard buildDefaultBoard() {
        return build(new int[][] {
                {-2, 0, 0, 0, 0, +5,   0, +3, 0, 0, 0, -5},
                {+2, 0, 0, 0, 0, -5,   0, -3, 0, 0, 0, +5}
        });
    }

    public static AdvancedGameBoard build(int[][] values,
                                          int suspendedForward, int suspendedBackwards, int collectedForward, int collectedBackwards) {

        final ColumnSequence columnSequence = new ColumnArrangement(translateToColumns(values),
                suspendedForward, suspendedBackwards, collectedForward, collectedBackwards);

        return new AdvancedGameBoard(columnSequence);
    }

    public static AdvancedGameBoard build(int[][] values) {

        final ColumnSequence columnSequence = new ColumnArrangement(translateToColumns(values),
                0, 0, 0, 0);

        return new AdvancedGameBoard(columnSequence);
    }

    private static List<BoardColumn> translateToColumns(int[][] values) {
        final int[] upper = values[0];
        final int[] lower = reverse(values[1]);

        final Stream<BoardColumn> upperStream = IntStream.range(0, upper.length)
                .mapToObj(index ->
                        new BoardColumn(Math.abs(upper[index]), Direction.ofSign(upper[index]), BOARD_TEMPLATE[0][index]));

        final Stream<BoardColumn> lowerStream = IntStream.range(0, lower.length)
                .mapToObj(index ->
                        new BoardColumn(Math.abs(lower[index]), Direction.ofSign(lower[index]), BOARD_TEMPLATE[1][index]));

        return Stream.concat(upperStream, lowerStream)
                .collect(Collectors.toList());
    }

    private static int[] reverse(int[] array) {
        final int[] result = new int[array.length];
        for (int index = 0; index < array.length; index++) {
            int complement = array.length - 1 - index;
            result[index] = array[complement];
        }
        return result;
    }
}
