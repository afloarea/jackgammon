package com.github.afloarea.jackgammon.juliette;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.afloarea.obge.moves.ObgMove;
import com.github.afloarea.obge.moves.ObgTransition;

import java.util.StringJoiner;

public record GameMove(@JsonProperty("source") String from,
                       @JsonProperty("target") String to) {
    @Override
    public String toString() {
        return new StringJoiner(",", "GM[", "]")
                .add("from=" + from)
                .add("to=" + to)
                .toString();
    }

    public static GameMove fromBgMove(ObgMove move) {
        return new GameMove(move.source(), move.target());
    }

    public static GameMove fromSuspendedPart(ObgTransition transition) {
        return new GameMove(transition.target(), transition.suspended());
    }

    /**
     * Map a transition to a game move. The game move will IGNORE the suspending column.
     * @param transition the transition
     * @return the game move
     */
    public static GameMove fromSimplePart(ObgTransition transition) {
        return new GameMove(transition.source(), transition.target());
    }
}
