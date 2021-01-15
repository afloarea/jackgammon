package com.github.afloarea.jackgammon.juliette.board;

import com.github.afloarea.jackgammon.juliette.Color;
import com.github.afloarea.jackgammon.juliette.DiceResult;
import com.github.afloarea.jackgammon.juliette.GameMove;

import java.util.Set;

public interface GameBoard {

    void updateDiceForPlayingColor(Color playingColor, DiceResult dice);

    void executeMoveForPlayingColor(Color playingColor, GameMove move);

    //-------

    Set<GameMove> getPossibleMovesForCurrentPlayingColor();

    Color getCurrentPlayingColor();

    Color getWinningColor();

    Color getLosingColor();

    boolean isGameComplete();

    boolean currentPlayingColorFinishedTurn();

    static GameBoard buildNewBoard() {
        return BoardFactory.buildDefaultBoard();
    }
}
