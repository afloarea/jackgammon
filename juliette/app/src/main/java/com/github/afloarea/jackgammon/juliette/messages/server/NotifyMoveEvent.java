package com.github.afloarea.jackgammon.juliette.messages.server;

import com.github.afloarea.jackgammon.juliette.GameMove;
import com.github.afloarea.jackgammon.juliette.manager.GameToPlayerEvent;

public record NotifyMoveEvent(GameMove move) implements GameToPlayerEvent {
}
