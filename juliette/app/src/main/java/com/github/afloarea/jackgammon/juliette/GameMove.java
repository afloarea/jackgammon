package com.github.afloarea.jackgammon.juliette;

import java.util.Objects;

public final class GameMove {
    private final Color color;
    private final int from;
    private final int distance;

    public GameMove(Color color, int from, int distance) {
        this.color = color;
        this.from = from;
        this.distance = distance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GameMove)) return false;
        GameMove gameMove = (GameMove) o;
        return from == gameMove.from && distance == gameMove.distance && color == gameMove.color;
    }

    @Override
    public int hashCode() {
        return Objects.hash(color, from, distance);
    }

    public Color getColor() {
        return color;
    }

    public int getFrom() {
        return from;
    }

    public int getDistance() {
        return distance;
    }
}
