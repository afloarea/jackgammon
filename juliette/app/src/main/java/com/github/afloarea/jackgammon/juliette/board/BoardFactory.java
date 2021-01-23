package com.github.afloarea.jackgammon.juliette.board;

import com.github.afloarea.jackgammon.juliette.Color;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class BoardFactory {
    public static final Map<Integer, String> IDS_BY_POSITION;

    static {
        final String identifiers = "ABCDEFGHIJKLXWVUTSRQPONM";
        IDS_BY_POSITION = IntStream.range(0, identifiers.length()).boxed()
                .collect(Collectors.toUnmodifiableMap(
                        Function.identity(), index -> String.valueOf(identifiers.charAt(index))));
    }

    private BoardFactory() {}

    public static BasicGameBoard buildDefaultBoard() {
        return build(
                new int[] {-2, 0, 0, 0, 0, +5,   0, +3, 0, 0, 0, -5},
                new int[] {+2, 0, 0, 0, 0, -5,   0, -3, 0, 0, 0, +5}
        );
    }

    public static BasicGameBoard build(int[] upper, int[] lower) {
        return build(upper, lower, 0, 0, 0, 0);
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
}
