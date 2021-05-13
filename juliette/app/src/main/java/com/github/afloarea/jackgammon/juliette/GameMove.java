package com.github.afloarea.jackgammon.juliette;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.afloarea.obge.BgMove;

import java.util.Objects;
import java.util.StringJoiner;

public final class GameMove {
    private final String from;
    private final String to;

    public GameMove(@JsonProperty("source") String from,
                    @JsonProperty("target") String to) {
        this.from = from;
        this.to = to;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GameMove)) return false;
        GameMove gameMove = (GameMove) o;
        return from.equals(gameMove.from) && to.equals(gameMove.to);
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to);
    }

    @JsonGetter("source")
    public String getFrom() {
        return from;
    }

    @JsonGetter("target")
    public String getTo() {
        return to;
    }

    @Override
    public String toString() {
        return new StringJoiner(",", "GM[", "]")
                .add("from=" + from)
                .add("to=" + to)
                .toString();
    }

    public BgMove toBgMove() {
        return BgMove.of(from, to);
    }

    public static GameMove fromBgMove(BgMove move) {
        return new GameMove(move.getSource(), move.getTarget());
    }
}
