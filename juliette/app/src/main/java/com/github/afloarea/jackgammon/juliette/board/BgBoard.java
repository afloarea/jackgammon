package com.github.afloarea.jackgammon.juliette.board;

import com.github.afloarea.jackgammon.juliette.board.exceptions.IllegalBgActionException;
import com.github.afloarea.jackgammon.juliette.board.impl.BoardFactory;

import java.util.List;
import java.util.Set;

/**
 * Backgammon board representation.
 */
public interface BgBoard {

    /**
     * Apply a dice roll result.
     * If this is the first turn, either direction can apply the dice roll.
     *
     * @param direction the direction (should be either clockwise or anticlockwise depending on whose turn is it)
     * @param dice      the dice roll result
     * @throws IllegalBgActionException if the roll cannot be applied
     */
    void applyDiceRoll(Direction direction, DiceRoll dice);

    /**
     * Execute the provided move in the given direction.
     *
     * @param direction the direction in which to execute move
     * @param move      the move to execute
     * @return the list of simple moves executed (each simple move corresponds to a single die value)
     * @throws IllegalBgActionException if the move cannot be executed
     */
    List<BgMove> execute(Direction direction, BgMove move);

    // read-only methods

    /**
     * Obtain the set of possible moves (both simple and composite).
     *
     * @return a set of possible moves
     */
    Set<BgMove> getPossibleMoves();

    /**
     * Get the current playing direction.
     * @return the direction
     */
    Direction getCurrentTurnDirection();

    /**
     * If the game is complete get the winning direction.
     * @return the winning direction
     */
    Direction getWinningDirection();

    /**
     * If the game is complete get the losing direction.
     * @return the losing direction
     */
    default Direction getLosingDirection() {
        return getWinningDirection().reverse();
    }

    /**
     * Check if the game is finished.
     * @return whether the game is complete or not.
     */
    boolean isGameComplete();

    /**
     * Check if the current direction has finished the turn.
     * @return true if the current direction has no moves left to make.
     */
    boolean isCurrentTurnDone();

    /**
     * Create a new board.
     * @return a new arranged board
     */
    static BgBoard build() {
        return BoardFactory.buildDefaultBoard();
    }
}
