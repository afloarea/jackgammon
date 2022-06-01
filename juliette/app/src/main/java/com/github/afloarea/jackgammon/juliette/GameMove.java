package com.github.afloarea.jackgammon.juliette;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.afloarea.obge.moves.ObgMove;

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
        return new GameMove(move.getSource(), move.getTarget());
    }
}
