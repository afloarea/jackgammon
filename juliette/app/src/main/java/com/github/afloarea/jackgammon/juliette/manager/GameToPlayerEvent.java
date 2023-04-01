package com.github.afloarea.jackgammon.juliette.manager;

import com.github.afloarea.jackgammon.juliette.messages.server.*;

public sealed interface GameToPlayerEvent extends ServerToClientEvent permits
        InitGameEvent, NotifyGameEndedEvent, NotifyMoveEvent,
        NotifyRollEvent, PromptMoveEvent, PromptRollEvent {
}
