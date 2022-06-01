package com.github.afloarea.jackgammon.juliette.messages.server;

import com.github.afloarea.jackgammon.juliette.manager.GameToPlayerMessage;

public record NotifyGameEndedMessage(String winner) implements GameToPlayerMessage {
}
