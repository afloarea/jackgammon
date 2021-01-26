package com.github.afloarea.jackgammon.juliette.board;

import java.util.List;

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
}
