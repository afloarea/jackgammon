package com.github.afloarea.jackgammon.juliette.board;

import com.github.afloarea.jackgammon.juliette.DiceResult;
import com.github.afloarea.jackgammon.juliette.GameMove;

import java.util.List;
import java.util.Set;

public interface GameBoard {

    void updateDiceForDirection(Direction direction, DiceResult dice);

    List<GameMove> executeMoveInDirection(Direction direction, GameMove move);

    //-------

    Set<GameMove> getCurrentDirectionPossibleMoves();

    Direction getCurrentDirection();

    Direction getWinningDirection();

    Direction getLosingDirection();

    boolean isGameComplete();

    boolean currentDirectionMovementIsComplete();

    static GameBoard buildNewBoard() {
        return BoardFactory.buildDefaultBoard();
    }
}
