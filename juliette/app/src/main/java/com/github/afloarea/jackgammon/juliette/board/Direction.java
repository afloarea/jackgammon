package com.github.afloarea.jackgammon.juliette.board;

public enum Direction {
    FORWARD(1) {
        @Override
        public Direction reverse() {
            return BACKWARD;
        }
    }, BACKWARD(-1) {
        @Override
        public Direction reverse() {
            return FORWARD;
        }
    }, NONE(0) {
        @Override
        public Direction reverse() {
            return NONE;
        }
    };

    private final int sign;

    Direction(int sign) {
        this.sign = sign;
    }

    public int getSign() {
        return sign;
    }

    public abstract Direction reverse();

    public static Direction ofSign(int sign) {
        if (sign == 0) {
            return NONE;
        }
        return sign < 0 ? Direction.FORWARD : Direction.BACKWARD;
    }
}
