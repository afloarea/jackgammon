package com.github.afloarea.jackgammon.juliette;

public enum Color {
    BLACK("B") {
        @Override
        public Color complement() {
            return Color.WHITE;
        }
    },
    WHITE("W") {
        @Override
        public Color complement() {
            return Color.BLACK;
        }
    },
    NONE(" ") {
        @Override
        public Color complement() {
            return Color.NONE;
        }
    };

    private final String symbol;

    Color(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }

    public abstract Color complement();

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
