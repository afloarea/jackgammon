package com.github.afloarea.jackgammon.juliette.messages.server;

import com.github.afloarea.jackgammon.juliette.manager.GameToPlayerEvent;
import com.github.afloarea.obge.dice.DiceRoll;

public record NotifyRollEvent(String playerName, int dice1, int dice2) implements GameToPlayerEvent {

    public NotifyRollEvent(String playerName, DiceRoll diceRoll) {
        this(playerName, diceRoll.dice1(), diceRoll.dice2());
    }

}
