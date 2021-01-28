package com.github.afloarea.jackgammon.juliette.manager;

import com.github.afloarea.jackgammon.juliette.Color;
import com.github.afloarea.jackgammon.juliette.messages.client.ClientToServerEvent;

public interface PlayerToGameMessage extends ClientToServerEvent {

    Color getPlayingColor();
}
