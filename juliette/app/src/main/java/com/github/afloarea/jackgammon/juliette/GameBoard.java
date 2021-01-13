package com.github.afloarea.jackgammon.juliette;

import java.util.Set;

public interface GameBoard {

    void updateDiceForPlayingColor(Color playingColor, DiceResult dice);

    void executeMoveForPlayingColor(Color playingColor, GameMove move);

    //-------

    Set<GameMove> getPossibleMovesForPlayingColor(Color playingColor);

    Color getCurrentPlayingColor();

    Color getWinningColor();

    Color getLosingColor();

    boolean isGameComplete();

    boolean currentPlayingColorPlayedFull();
}
