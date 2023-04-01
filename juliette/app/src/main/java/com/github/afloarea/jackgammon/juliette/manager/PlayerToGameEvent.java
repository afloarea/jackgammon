package com.github.afloarea.jackgammon.juliette.manager;

import com.github.afloarea.jackgammon.juliette.messages.client.ClientToServerEvent;
import com.github.afloarea.jackgammon.juliette.messages.client.PlayerRollEvent;
import com.github.afloarea.jackgammon.juliette.messages.client.SelectMoveEvent;

public sealed interface PlayerToGameEvent extends ClientToServerEvent
        permits PlayerRollEvent, SelectMoveEvent {}
