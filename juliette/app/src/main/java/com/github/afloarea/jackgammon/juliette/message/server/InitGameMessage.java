package com.github.afloarea.jackgammon.juliette.message.server;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.github.afloarea.jackgammon.juliette.Color;
import com.github.afloarea.jackgammon.juliette.manager.GameToPlayerMessage;

import java.util.Objects;
import java.util.StringJoiner;

public final class InitGameMessage implements GameToPlayerMessage {
    private static final String BOARD_VALUE = "{\n" +
            "    \"black\": {\n" +
            "      \"0\": 2,\n" +
            "      \"11\": 5,\n" +
            "      \"16\": 3,\n" +
            "      \"18\": 5\n" +
            "    },\n" +
            "    \"white\": {\n" +
            "      \"5\": 5,\n" +
            "      \"7\": 3,\n" +
            "      \"12\": 5,\n" +
            "      \"23\": 2\n" +
            "    }\n" +
            "  }";

    private final Color playingColor;

    public InitGameMessage(Color playingColor) {
        this.playingColor = playingColor;
    }

    public Color getPlayingColor() {
        return playingColor;
    }

    @JsonRawValue
    public String getBoard() {
        return BOARD_VALUE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InitGameMessage)) return false;
        InitGameMessage that = (InitGameMessage) o;
        return playingColor == that.playingColor;
    }

    @Override
    public int hashCode() {
        return Objects.hash(playingColor);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", InitGameMessage.class.getSimpleName() + "[", "]")
                .add("playingColor=" + playingColor)
                .toString();
    }
}
