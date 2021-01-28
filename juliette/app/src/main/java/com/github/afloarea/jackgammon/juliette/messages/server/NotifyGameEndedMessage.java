package com.github.afloarea.jackgammon.juliette.messages.server;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.github.afloarea.jackgammon.juliette.manager.GameToPlayerMessage;

import java.util.Objects;
import java.util.StringJoiner;

public final class NotifyGameEndedMessage implements GameToPlayerMessage {
    private final String winner;

    public NotifyGameEndedMessage(String winner) {
        this.winner = winner;
    }

    @JsonGetter("winner")
    public String getWinner() {
        return winner;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NotifyGameEndedMessage)) return false;
        NotifyGameEndedMessage that = (NotifyGameEndedMessage) o;
        return winner.equals(that.winner);
    }

    @Override
    public int hashCode() {
        return Objects.hash(winner);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", NotifyGameEndedMessage.class.getSimpleName() + "[", "]")
                .add("winner=" + winner)
                .toString();
    }
}
