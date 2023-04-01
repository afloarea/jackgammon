package com.github.afloarea.jackgammon.juliette.messages.client;

import com.github.afloarea.jackgammon.juliette.GameMove;
import com.github.afloarea.jackgammon.juliette.manager.PlayerToGameEvent;

public record SelectMoveEvent(GameMove selectedMove) implements PlayerToGameEvent {
}
