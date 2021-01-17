package com.github.afloarea.jackgammon.juliette.message.client;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.afloarea.jackgammon.juliette.Color;
import com.github.afloarea.jackgammon.juliette.manager.PlayerToGameMessage;

import java.util.Objects;
import java.util.StringJoiner;

public final class PlayerRollMessage implements PlayerToGameMessage {

    private final Color playingColor;

    @JsonCreator
    public PlayerRollMessage(@JsonProperty("playingColor") Color playingColor) {
        this.playingColor = playingColor;
    }

    @Override
    public Color getPlayingColor() {
        return playingColor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlayerRollMessage)) return false;
        PlayerRollMessage that = (PlayerRollMessage) o;
        return playingColor == that.playingColor;
    }

    @Override
    public int hashCode() {
        return Objects.hash(playingColor);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", PlayerRollMessage.class.getSimpleName() + "[", "]")
                .add("playingColor=" + playingColor)
                .toString();
    }
}
