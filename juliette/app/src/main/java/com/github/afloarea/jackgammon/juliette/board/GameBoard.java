package com.github.afloarea.jackgammon.juliette.board;

import com.github.afloarea.jackgammon.juliette.GameMove;

import java.util.List;
import java.util.Set;

public interface GameBoard {

    void updateDiceForDirection(Direction direction, DiceRoll dice);

    List<GameMove> executeMoveInDirection(Direction direction, GameMove move);

    //-------

    Set<GameMove> getCurrentDirectionPossibleMoves();

    Direction getCurrentTurnDirection();

    Direction getWinningDirection();

    default Direction getLosingDirection() {
        return getWinningDirection().reverse();
    }

    boolean isGameComplete();

    boolean isCurrentTurnDone();

    static GameBoard buildNewBoard() {
        return BoardFactory.buildDefaultBoard();
    }
}
