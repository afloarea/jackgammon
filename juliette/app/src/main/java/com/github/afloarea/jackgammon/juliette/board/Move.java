package com.github.afloarea.jackgammon.juliette.board;

import java.util.List;
import java.util.Objects;

public class Move {

    private final BoardColumn source;
    private final BoardColumn target;
    private final List<Integer> distances;

    public Move(BoardColumn source, BoardColumn target, List<Integer> distances) {
        this.source = source;
        this.target = target;
        this.distances = distances;
    }

    public BoardColumn getSource() {
        return source;
    }

    public BoardColumn getTarget() {
        return target;
    }

    public List<Integer> getDistances() {
        return distances;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Move)) return false;
        Move move = (Move) o;
        return source.getId().equals(move.source.getId()) && target.getId().equals(move.target.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(source.getId(), target.getId());
    }
}
