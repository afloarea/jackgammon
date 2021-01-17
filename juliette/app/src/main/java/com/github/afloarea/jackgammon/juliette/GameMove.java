package com.github.afloarea.jackgammon.juliette;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;
import java.util.StringJoiner;

public final class GameMove {
    private final MoveType type;
    private final int from;
    private final int to;

    public GameMove(@JsonProperty("type") MoveType type,
                    @JsonProperty("source") int from,
                    @JsonProperty("target") int to) {
        this.type = type;
        this.from = from;
        this.to = to;
    }

    public static GameMove move(int from, int to) {
        return new GameMove(MoveType.SIMPLE, from, to);
    }

    public static GameMove enter(int to) {
        return new GameMove(MoveType.ENTER, -1, to);
    }

    public static GameMove collect(int from) {
        return new GameMove(MoveType.COLLECT, from, -1);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GameMove)) return false;
        GameMove gameMove = (GameMove) o;
        return from == gameMove.from && to == gameMove.to && type == gameMove.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, from, to);
    }

    @JsonGetter("source")
    public int getFrom() {
        return from;
    }

    @JsonGetter("target")
    public int getTo() {
        return to;
    }

    public MoveType getType() {
        return type;
    }

    @Override
    public String toString() {
        return new StringJoiner(",", "GM[", "]")
                .add(String.valueOf(type))
                .add("from=" + from)
                .add("to=" + to)
                .toString();
    }
}
