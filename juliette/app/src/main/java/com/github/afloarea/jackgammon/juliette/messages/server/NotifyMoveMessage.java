package com.github.afloarea.jackgammon.juliette.messages.server;

import com.github.afloarea.jackgammon.juliette.GameMove;
import com.github.afloarea.jackgammon.juliette.manager.GameToPlayerMessage;

public record NotifyMoveMessage(GameMove move) implements GameToPlayerMessage {
}
