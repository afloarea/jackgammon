package com.github.afloarea.jackgammon.juliette.messages.server;

import com.github.afloarea.jackgammon.juliette.manager.GameToPlayerMessage;
import com.github.afloarea.obge.dice.DiceRoll;

public record NotifyRollMessage(String playerName, int dice1, int dice2) implements GameToPlayerMessage {

    public NotifyRollMessage(String playerName, DiceRoll diceRoll) {
        this(playerName, diceRoll.dice1(), diceRoll.dice2());
    }

}
