package com.github.afloarea.jackgammon.juliette.messages.server;

import com.github.afloarea.jackgammon.juliette.GameMove;
import com.github.afloarea.jackgammon.juliette.manager.GameToPlayerMessage;

import java.util.Objects;
import java.util.StringJoiner;

public final class NotifyMoveMessage implements GameToPlayerMessage {
    private final GameMove move;

    public NotifyMoveMessage(GameMove move) {
        this.move = move;
    }

    public GameMove getMove() {
        return move;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NotifyMoveMessage)) return false;
        NotifyMoveMessage that = (NotifyMoveMessage) o;
        return Objects.equals(move, that.move);
    }

    @Override
    public int hashCode() {
        return Objects.hash(move);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", NotifyMoveMessage.class.getSimpleName() + "[", "]")
                .add("move=" + move)
                .toString();
    }
}
