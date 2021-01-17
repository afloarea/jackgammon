package com.github.afloarea.jackgammon.juliette.message.server;

import com.github.afloarea.jackgammon.juliette.GameMove;
import com.github.afloarea.jackgammon.juliette.manager.GameToPlayerMessage;

import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;

public final class PromptMoveMessage implements GameToPlayerMessage {

    private final Set<GameMove> possibleMoves;

    public PromptMoveMessage(Set<GameMove> possibleMoves) {
        this.possibleMoves = possibleMoves;
    }

    public Set<GameMove> getPossibleMoves() {
        return possibleMoves;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PromptMoveMessage)) return false;
        PromptMoveMessage that = (PromptMoveMessage) o;
        return Objects.equals(possibleMoves, that.possibleMoves);
    }

    @Override
    public int hashCode() {
        return Objects.hash(possibleMoves);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", PromptMoveMessage.class.getSimpleName() + "[", "]")
                .add("possibleMoves=" + possibleMoves)
                .toString();
    }
}
