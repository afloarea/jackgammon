package com.github.afloarea.jackgammon.juliette.message.server;

import com.github.afloarea.jackgammon.juliette.Color;
import com.github.afloarea.jackgammon.juliette.GameMove;
import com.github.afloarea.jackgammon.juliette.manager.GameToPlayerMessage;
import com.github.afloarea.jackgammon.juliette.message.client.SelectMoveMessage;

import java.util.Objects;
import java.util.StringJoiner;

public final class NotifyMoveMessage implements GameToPlayerMessage {
    private final Color playingColor;
    private final GameMove move;

    public NotifyMoveMessage(Color playingColor, GameMove move) {
        this.playingColor = playingColor;
        this.move = move;
    }

    public static NotifyMoveMessage from(SelectMoveMessage selectMessage) {
        return new NotifyMoveMessage(selectMessage.getPlayingColor(), selectMessage.getSelectedMove());
    }

    public Color getPlayingColor() {
        return playingColor;
    }

    public GameMove getMove() {
        return move;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NotifyMoveMessage)) return false;
        NotifyMoveMessage that = (NotifyMoveMessage) o;
        return playingColor == that.playingColor && Objects.equals(move, that.move);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playingColor, move);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", NotifyMoveMessage.class.getSimpleName() + "[", "]")
                .add("playingColor=" + playingColor)
                .add("move=" + move)
                .toString();
    }
}
