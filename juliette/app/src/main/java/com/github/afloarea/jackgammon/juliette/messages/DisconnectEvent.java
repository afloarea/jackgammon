package com.github.afloarea.jackgammon.juliette.messages;

import com.github.afloarea.jackgammon.juliette.messages.client.ClientToServerEvent;
import com.github.afloarea.jackgammon.juliette.messages.server.ServerToClientEvent;

public record DisconnectEvent() implements ClientToServerEvent, ServerToClientEvent {
}
