package com.github.afloarea.jackgammon.juliette;

public final class BoardUtils {

    private BoardUtils() {
    }

    public static String displayBoard(DefaultGameBoard gameBoard) {
        final var empty = " ";
        final var black = "B";
        final var white = "W";

        // first half
        var firstHalf = new StringBuilder("-".repeat(38) + System.lineSeparator());
        for (int index = 1; index < 15; index++) {
            firstHalf.append("|");
            for (int positionIndex = 1; positionIndex <= 12; positionIndex++) {
                var position = gameBoard.blackView[positionIndex];
                final boolean isEmpty = position.getPieceCount() < index;
                final String symbol;
                if (isEmpty) {
                    symbol = empty;
                } else {
                    symbol = position.getPieceColor() == Color.BLACK ? black : white;
                }
                firstHalf.append(" ").append(symbol).append(" ");
            }
            firstHalf.append("|").append(System.lineSeparator());
        }

        var secondHalf = new StringBuilder();
        for (int index = 14; index >= 1; index--) {
            secondHalf.append("|");
            for (int positionIndex = 1; positionIndex <= 12; positionIndex++) {
                var position = gameBoard.whiteView[positionIndex];
                final boolean isEmpty = position.getPieceCount() < index;
                final String symbol;
                if (isEmpty) {
                    symbol = empty;
                } else {
                    symbol = position.getPieceColor() == Color.BLACK ? black : white;
                }
                secondHalf.append(" ").append(symbol).append(" ");
            }
            secondHalf.append("|").append(System.lineSeparator());
        }
        secondHalf.append("-".repeat(38)).append(System.lineSeparator());

        return firstHalf.append(secondHalf).toString();
    }
}

