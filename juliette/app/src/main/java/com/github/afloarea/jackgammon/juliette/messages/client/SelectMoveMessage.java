package com.github.afloarea.jackgammon.juliette.messages.client;

import com.github.afloarea.jackgammon.juliette.GameMove;
import com.github.afloarea.jackgammon.juliette.manager.PlayerToGameMessage;

public record SelectMoveMessage(GameMove selectedMove) implements PlayerToGameMessage {
}
