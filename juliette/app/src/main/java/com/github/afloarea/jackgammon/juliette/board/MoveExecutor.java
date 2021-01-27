package com.github.afloarea.jackgammon.juliette.board;

import com.github.afloarea.jackgammon.juliette.GameMove;

import java.util.List;

public interface MoveExecutor {

    List<GameMove> executeMove(Move move, Direction direction);

}
