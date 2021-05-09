package com.github.afloarea.jackgammon.juliette.board.moves.executor;

import com.github.afloarea.jackgammon.juliette.board.BgMove;
import com.github.afloarea.jackgammon.juliette.board.Direction;
import com.github.afloarea.jackgammon.juliette.board.common.Move;

import java.util.List;

/**
 * Interface for executing game moves.
 */
public interface MoveExecutor {

    /**
     * Execute the provided move with the specified direction.
     * @param move the move to execute
     * @param direction the direction of the move
     * @return a list of performed simple moves (ex. suspend piece + move piece)
     */
    List<BgMove> executeMove(Move move, Direction direction);

}
