package com.github.afloarea.jackgammon.juliette.message.server;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.github.afloarea.jackgammon.juliette.Color;
import com.github.afloarea.jackgammon.juliette.manager.GameToPlayerMessage;

import java.util.Objects;
import java.util.StringJoiner;

public final class InitGameMessage implements GameToPlayerMessage {
    private static final String BOARD_VALUE = "[\n" +
            "    {\"columnId\": \"A\", \"pieces\": 2, \"color\": \"black\"},\n" +
            "    {\"columnId\": \"F\", \"pieces\": 5, \"color\": \"white\"},\n" +
            "    {\"columnId\": \"H\", \"pieces\": 3, \"color\": \"white\"},\n" +
            "    {\"columnId\": \"L\", \"pieces\": 5, \"color\": \"black\"},\n" +
            "    {\"columnId\": \"M\", \"pieces\": 2, \"color\": \"white\"},\n" +
            "    {\"columnId\": \"R\", \"pieces\": 5, \"color\": \"black\"},\n" +
            "    {\"columnId\": \"T\", \"pieces\": 3, \"color\": \"black\"},\n" +
            "    {\"columnId\": \"X\", \"pieces\": 5, \"color\": \"white\"}\n" +
            "  ]";

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
