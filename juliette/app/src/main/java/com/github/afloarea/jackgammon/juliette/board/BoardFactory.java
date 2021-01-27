package com.github.afloarea.jackgammon.juliette.board;

import com.github.afloarea.jackgammon.juliette.Color;
import com.github.afloarea.jackgammon.juliette.board.layout.ColumnArrangement;
import com.github.afloarea.jackgammon.juliette.board.layout.ColumnSequence;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public final class BoardFactory {
    private static final String[][] BOARD_TEMPLATE = {
            "ABCDEFGHIJKL".split(""),
            new StringBuilder("MNOPQRSTUVWX").reverse().toString().split("")
    };
    public static final Map<Integer, String> IDS_BY_POSITION;

    static {
        final String identifiers = "ABCDEFGHIJKLXWVUTSRQPONM";
        IDS_BY_POSITION = IntStream.range(0, identifiers.length()).boxed()
                .collect(Collectors.toUnmodifiableMap(
                        Function.identity(), index -> String.valueOf(identifiers.charAt(index))));
    }

    private BoardFactory() {}

    public static AdvancedGameBoard buildDefaultBoard() {
        return build(new int[][] {
                {-2, 0, 0, 0, 0, +5,   0, +3, 0, 0, 0, -5},
                {+2, 0, 0, 0, 0, -5,   0, -3, 0, 0, 0, +5}
        });
    }

    public static BasicGameBoard build(int[] upper, int[] lower) {
        return build(upper, lower, 0, 0, 0, 0);
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

    public static BasicGameBoard build(int[] upper, int[] lower,
                                       int suspendedBlack, int suspendedWhite, int collectedBlack, int collectedWhite) {

        final int[] columnValues = new int[upper.length + lower.length];
        System.arraycopy(upper, 0, columnValues, 0, upper.length);
        for (int index = 0; index < lower.length; index++) {
            columnValues[upper.length + index] = lower[lower.length - index - 1];
        }

        final var columns = IntStream.range(0, columnValues.length)
                .mapToObj(index -> buildColumnFrom(columnValues[index], index))
                .toArray(BoardColumn[]::new);

        return new BasicGameBoard(columns, suspendedBlack, suspendedWhite, collectedBlack, collectedWhite);
    }

    private static BoardColumn buildColumnFrom(int value, int position) {
        if (value == 0) {
            return new BoardColumn(IDS_BY_POSITION.get(position));
        }
        return new BoardColumn(Math.abs(value), value < 0 ? Color.BLACK : Color.WHITE, IDS_BY_POSITION.get(position));
    }

    static String display(BoardColumn[] blackViewColumns, BoardColumn[] whiteViewColumns,
                          int suspendedBlack, int suspendedWhite, int collectedBlack, int collectedWhite) {

        final String blackStats = String.format("%s: suspended - %2d\t collected - %2d", Color.BLACK, suspendedBlack, collectedBlack);
        final String whiteStats = String.format("%s: suspended - %2d\t collected - %2d", Color.WHITE, suspendedWhite, collectedWhite);

        final String firstHalf = Arrays.stream(blackViewColumns).limit(blackViewColumns.length / 2)
                .map(BoardFactory::mapColumnToString).collect(Collectors.joining(",", "[", "]"));
        final String secondHalf = Arrays.stream(whiteViewColumns).limit(whiteViewColumns.length / 2)
                .map(BoardFactory::mapColumnToString).collect(Collectors.joining(",", "[", "]"));

        return String.format("%s | %s%s%s | %s%s",
                firstHalf, blackStats, System.lineSeparator(),
                secondHalf, whiteStats, System.lineSeparator());
    }

    private static String mapColumnToString(BoardColumn column) {
        return String.format("%3s", column.getPieceCount() + column.getPieceColor().getSymbol());
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
