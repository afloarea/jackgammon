package com.github.afloarea.jackgammon.juliette;

public enum Color {
    BLACK {
        @Override
        public Color complement() {
            return Color.WHITE;
        }
    },
    WHITE {
        @Override
        public Color complement() {
            return Color.BLACK;
        }
    },
    NONE {
        @Override
        public Color complement() {
            return Color.NONE;
        }
    };

    public abstract Color complement();
}
