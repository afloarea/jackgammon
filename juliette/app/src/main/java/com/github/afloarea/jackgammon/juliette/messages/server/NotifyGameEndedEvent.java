package com.github.afloarea.jackgammon.juliette.messages.server;

import com.github.afloarea.jackgammon.juliette.manager.GameToPlayerEvent;

public record NotifyGameEndedEvent(String winner) implements GameToPlayerEvent {
}
