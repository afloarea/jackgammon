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
        return build(values, 0, 0, 0, 0);
    }

    private static List<BoardColumn> translateToColumns(int[][] values) {
        final int[] upper = values[0];
        final int[] lower = reverse(values[1]);

        return Stream.concat(buildColumnStream(upper, BOARD_TEMPLATE[0]), buildColumnStream(lower, BOARD_TEMPLATE[1]))
                .collect(Collectors.toList());
    }

    private static Stream<BoardColumn> buildColumnStream(int[] rawValues, String[] template) {
        return IntStream.range(0, rawValues.length)
                .mapToObj(index ->
                        new BoardColumn(Math.abs(rawValues[index]), Direction.ofSign(rawValues[index]), template[index]));
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
