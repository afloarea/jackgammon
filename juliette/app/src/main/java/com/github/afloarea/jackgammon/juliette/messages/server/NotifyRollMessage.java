package com.github.afloarea.jackgammon.juliette.messages.server;

import com.github.afloarea.jackgammon.juliette.board.DiceResult;
import com.github.afloarea.jackgammon.juliette.manager.GameToPlayerMessage;

import java.util.Objects;
import java.util.StringJoiner;

public final class NotifyRollMessage implements GameToPlayerMessage {
    private final String playerName;
    private final int dice1;
    private final int dice2;

    public NotifyRollMessage(String playerName, DiceResult diceResult) {
        this.playerName = playerName;
        this.dice1 = diceResult.getDice1();
        this.dice2 = diceResult.getDice2();
    }

    public String getPlayerName() {
        return playerName;
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
        return dice1 == that.dice1 && dice2 == that.dice2 && playerName.equals(that.playerName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerName, dice1, dice2);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", NotifyRollMessage.class.getSimpleName() + "[", "]")
                .add("playerName=" + playerName)
                .add("dice1=" + dice1)
                .add("dice2=" + dice2)
                .toString();
    }
}
