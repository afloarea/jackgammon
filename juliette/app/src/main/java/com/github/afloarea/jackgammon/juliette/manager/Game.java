package com.github.afloarea.jackgammon.juliette.manager;

import java.util.Collection;

// handles interaction for players that are ready to play
public interface Game {

    GameToPlayersMessage handle(PlayerToGameMessage message);

    GameToPlayersMessage init();

    Collection<String> getPlayersIds();
}
