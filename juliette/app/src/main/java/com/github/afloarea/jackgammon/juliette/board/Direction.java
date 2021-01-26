package com.github.afloarea.jackgammon.juliette.board;

public enum Direction {
    FORWARD(1), BACKWARD(-1), NONE(0);

    private final int sign;

    Direction(int sign) {
        this.sign = sign;
    }

    public int getSign() {
        return sign;
    }
}
