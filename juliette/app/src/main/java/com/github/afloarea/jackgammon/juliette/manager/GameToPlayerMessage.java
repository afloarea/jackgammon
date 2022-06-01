package com.github.afloarea.jackgammon.juliette.manager;

import com.github.afloarea.jackgammon.juliette.messages.server.*;

public sealed interface GameToPlayerMessage extends ServerToClientEvent permits
        InitGameMessage, NotifyGameEndedMessage, NotifyMoveMessage,
        NotifyRollMessage, PromptMoveMessage, PromptRollMessage {
}
