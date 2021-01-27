package com.github.afloarea.jackgammon.juliette.messages.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.afloarea.jackgammon.juliette.Color;
import com.github.afloarea.jackgammon.juliette.GameMove;
import com.github.afloarea.jackgammon.juliette.manager.PlayerToGameMessage;

import java.util.Objects;
import java.util.StringJoiner;

public final class SelectMoveMessage implements PlayerToGameMessage {
    private final Color playingColor;
    private final GameMove selectedMove;

    public SelectMoveMessage(@JsonProperty("playingColor") Color playingColor,
                             @JsonProperty("selectedMove") GameMove selectedMove) {
        this.playingColor = playingColor;
        this.selectedMove = selectedMove;
    }

    @Override
    public Color getPlayingColor() {
        return playingColor;
    }

    public GameMove getSelectedMove() {
        return selectedMove;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SelectMoveMessage)) return false;
        SelectMoveMessage that = (SelectMoveMessage) o;
        return playingColor == that.playingColor && Objects.equals(selectedMove, that.selectedMove);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playingColor, selectedMove);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", SelectMoveMessage.class.getSimpleName() + "[", "]")
                .add("playingColor=" + playingColor)
                .add("selectedMove=" + selectedMove)
                .toString();
    }
}
