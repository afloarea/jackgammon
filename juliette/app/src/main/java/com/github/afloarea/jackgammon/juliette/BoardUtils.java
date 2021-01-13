package com.github.afloarea.jackgammon.juliette;

import java.util.Arrays;
import java.util.stream.Collectors;

public final class BoardUtils {
    private static final String BLACK_SYMBOL = "*";
    private static final String WHITE_SYMBOL = "+";
    private static final String EMPTY_SYMBOL = " ";

    private BoardUtils() {
    }

    public static String displayBoard(DefaultGameBoard gameBoard) {
        return Arrays.stream(constructBoardArray(gameBoard, 15))
                .map(row -> Arrays.stream(row).collect(Collectors.joining(EMPTY_SYMBOL, "|", "|")))
                .collect(Collectors.joining(System.lineSeparator()));
    }

    private static String[][] constructBoardArray(DefaultGameBoard gameBoard, int boardSize) {
        final String[][] board = new String[12][boardSize];
        final String[] bucket = new String[(boardSize - 1) / 2];

        Arrays.stream(board).forEach(row -> Arrays.fill(row, EMPTY_SYMBOL));

        for (int index = 0; index < 12; index++) {
            System.arraycopy(
                    stringify(gameBoard.blackView[index + 1], bucket), 0,
                    board[index], 0,
                    bucket.length);

            System.arraycopy(
                    reverse(stringify(gameBoard.whiteView[index + 1], bucket)), 0,
                    board[index], board[index].length - bucket.length,
                    bucket.length);
        }

        return board;
    }

    private static String[] stringify(BoardPosition position, String[] bucket) {
        final int pieces = position.getPieceCount();
        final int lastSlotIndex = bucket.length - 1;

        // fill colored
        final var color = position.getPieceColor() == Color.BLACK ? BLACK_SYMBOL : WHITE_SYMBOL;
        for (int index = 0; index < Math.min(pieces, lastSlotIndex); index++) {
            bucket[index] = color;
        }

        if (pieces < lastSlotIndex) {
            // fill the rest with empty
            Arrays.fill(bucket, pieces, bucket.length, EMPTY_SYMBOL);
            return bucket;
        }

        final int diff = pieces - lastSlotIndex;
        bucket[lastSlotIndex] = diff == 1 ? color : String.format("%+d", diff);
        return bucket;
    }

    private static <T> T[] reverse(T[] array) {
        for (int index = 0; index < array.length / 2; index++) {
            final int complement = array.length - index - 1;
            final T temp = array[index];
            array[index] = array[complement];
            array[complement] = temp;
        }
        return array;
    }
}

