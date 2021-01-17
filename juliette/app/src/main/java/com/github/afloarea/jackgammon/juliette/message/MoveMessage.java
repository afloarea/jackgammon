package com.github.afloarea.jackgammon.juliette.message;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.afloarea.jackgammon.juliette.Color;
import com.github.afloarea.jackgammon.juliette.GameMove;
import com.github.afloarea.jackgammon.juliette.manager.GameToPlayerMessage;
import com.github.afloarea.jackgammon.juliette.manager.PlayerToGameMessage;

import java.util.Objects;
import java.util.StringJoiner;

public final class MoveMessage implements PlayerToGameMessage, GameToPlayerMessage {
    private final Color playingColor;
    private final GameMove move;

    @JsonCreator
    public MoveMessage(@JsonProperty("playingColor") Color playingColor,
                       @JsonProperty("selectedMove") GameMove move) {
        this.playingColor = playingColor;
        this.move = move;
    }

    @Override
    public Color getPlayingColor() {
        return playingColor;
    }

    @JsonGetter("move")
    public GameMove getSelectedMove() {
        return move;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MoveMessage)) return false;
        MoveMessage that = (MoveMessage) o;
        return playingColor == that.playingColor && Objects.equals(move, that.move);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playingColor, move);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", MoveMessage.class.getSimpleName() + "[", "]")
                .add("playingColor=" + playingColor)
                .add("move=" + move)
                .toString();
    }
}
