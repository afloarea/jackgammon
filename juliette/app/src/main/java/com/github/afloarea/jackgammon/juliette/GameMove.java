package com.github.afloarea.jackgammon.juliette;

import java.util.Objects;

public final class GameMove {
    private final MoveType moveType;
    private final int from;
    private final int to;

    public GameMove(MoveType moveType, int from, int to) {
        this.moveType = moveType;
        this.from = from;
        this.to = to;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GameMove)) return false;
        GameMove gameMove = (GameMove) o;
        return from == gameMove.from && to == gameMove.to && moveType == gameMove.moveType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(moveType, from, to);
    }

    public int getFrom() {
        return from;
    }

    public int getTo() {
        return to;
    }
}
