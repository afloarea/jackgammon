package com.github.afloarea.jackgammon.juliette;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class DefaultGameBoard implements GameBoard {
    BoardPosition[] whiteView;
    BoardPosition[] blackView;

    public void init() {
        // set up the board
        blackView = new BoardPosition[]{
                BoardPosition.emptyFromBlackView(0), // special case for black pieces out of play
                BoardPosition.ofColorFromBlackView(2, Color.BLACK, 1),
                BoardPosition.emptyFromBlackView(2),
                BoardPosition.emptyFromBlackView(3),
                BoardPosition.emptyFromBlackView(4),
                BoardPosition.emptyFromBlackView(5),
                BoardPosition.ofColorFromBlackView(5, Color.WHITE, 6),
                BoardPosition.emptyFromBlackView(7),
                BoardPosition.ofColorFromBlackView(3, Color.WHITE, 8),
                BoardPosition.emptyFromBlackView(9),
                BoardPosition.emptyFromBlackView(10),
                BoardPosition.emptyFromBlackView(11),
                BoardPosition.ofColorFromBlackView(5, Color.BLACK, 12),
                BoardPosition.ofColorFromBlackView(5, Color.WHITE, 13),
                BoardPosition.emptyFromBlackView(14),
                BoardPosition.emptyFromBlackView(15),
                BoardPosition.emptyFromBlackView(16),
                BoardPosition.ofColorFromBlackView(3, Color.BLACK, 17),
                BoardPosition.emptyFromBlackView(18),
                BoardPosition.ofColorFromBlackView(5, Color.BLACK, 19),
                BoardPosition.emptyFromBlackView(20),
                BoardPosition.emptyFromBlackView(21),
                BoardPosition.emptyFromBlackView(22),
                BoardPosition.emptyFromBlackView(23),
                BoardPosition.ofColorFromBlackView(2, Color.WHITE, 24),
                BoardPosition.emptyFromBlackView(25)  // special case for white pieces out of play
        };

        final var whites = new ArrayList<>(Arrays.asList(blackView));
        Collections.reverse(whites);
        whiteView = whites.toArray(BoardPosition[]::new);
    }

    @Override
    public Result executeMove(GameMove move) {
        return null;
    }

    @Override
    public String toString() {
        return BoardUtils.displayBoard(this);
    }
}
