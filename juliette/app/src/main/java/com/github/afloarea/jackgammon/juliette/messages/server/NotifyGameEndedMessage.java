package com.github.afloarea.jackgammon.juliette.messages.server;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.github.afloarea.jackgammon.juliette.Color;
import com.github.afloarea.jackgammon.juliette.manager.GameToPlayerMessage;

import java.util.Objects;
import java.util.StringJoiner;

public final class NotifyGameEndedMessage implements GameToPlayerMessage {
    private final Color winningColor;

    public NotifyGameEndedMessage(Color winningColor) {
        this.winningColor = winningColor;
    }

    @JsonGetter("winner")
    public Color getWinningColor() {
        return winningColor;
    }

    @JsonGetter("loser")
    public Color getLosingColor() {
        return winningColor.complement();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NotifyGameEndedMessage)) return false;
        NotifyGameEndedMessage that = (NotifyGameEndedMessage) o;
        return winningColor == that.winningColor;
    }

    @Override
    public int hashCode() {
        return Objects.hash(winningColor);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", NotifyGameEndedMessage.class.getSimpleName() + "[", "]")
                .add("winningColor=" + winningColor)
                .toString();
    }
}
