package com.github.afloarea.jackgammon.juliette;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Random;

public enum Color {
    @JsonProperty("black") BLACK("B") {
        @Override
        public Color complement() {
            return Color.WHITE;
        }
    },
    @JsonProperty("white") WHITE("W") {
        @Override
        public Color complement() {
            return Color.BLACK;
        }
    },
    @JsonProperty("none") NONE(" ") {
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

    public static Color getRandom() {
        return Wrapper.RANDOM.nextBoolean() ? Color.BLACK : Color.WHITE;
    }

    private static final class Wrapper {
        private static final Random RANDOM = new Random();
    }
}
