package com.github.afloarea.jackgammon.juliette.board.moves.generator;

import com.github.afloarea.jackgammon.juliette.board.Direction;
import com.github.afloarea.jackgammon.juliette.board.Move;

import java.util.List;
import java.util.stream.Stream;

/**
 * A service that computes the possible moves that can be made by a player (which is associated to a direction).
 */
public interface PossibleMovesProvider {

    /**
     * Compute the possible moves that can be made in the provided direction.
     * @param dice the available dice with which to perform moves (should not be empty)
     * @param direction the direction
     * @return a stream of the possible moves
     */
    Stream<Move> streamPossibleMoves(List<Integer> dice, Direction direction);

}
