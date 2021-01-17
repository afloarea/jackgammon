package com.github.afloarea.jackgammon.juliette.message.server;

import com.github.afloarea.jackgammon.juliette.Color;
import com.github.afloarea.jackgammon.juliette.DiceResult;
import com.github.afloarea.jackgammon.juliette.manager.GameToPlayerMessage;

import java.util.Objects;
import java.util.StringJoiner;

public final class NotifyRollMessage implements GameToPlayerMessage {
    private final Color playingColor;
    private final int dice1;
    private final int dice2;

    public NotifyRollMessage(Color playingColor, DiceResult diceResult) {
        this.playingColor = playingColor;
        this.dice1 = diceResult.getDice1();
        this.dice2 = diceResult.getDice2();
    }

    public Color getPlayingColor() {
        return playingColor;
    }

    public int getDice1() {
        return dice1;
    }

    public int getDice2() {
        return dice2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NotifyRollMessage)) return false;
        NotifyRollMessage that = (NotifyRollMessage) o;
        return dice1 == that.dice1 && dice2 == that.dice2 && playingColor == that.playingColor;
    }

    @Override
    public int hashCode() {
        return Objects.hash(playingColor, dice1, dice2);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", NotifyRollMessage.class.getSimpleName() + "[", "]")
                .add("playingColor=" + playingColor)
                .add("dice1=" + dice1)
                .add("dice2=" + dice2)
                .toString();
    }
}
