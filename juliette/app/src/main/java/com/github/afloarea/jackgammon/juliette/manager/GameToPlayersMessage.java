package com.github.afloarea.jackgammon.juliette.manager;

import java.util.Collection;
import java.util.Map;

public class GameToPlayersMessage {
    private final Map<String, Collection<GameToPlayerMessage>> messagesByPlayer;

    private GameToPlayersMessage(Map<String, Collection<GameToPlayerMessage>> messagesByPlayer) {
        this.messagesByPlayer = messagesByPlayer;
    }

    public Collection<GameToPlayerMessage> getMessagesForPlayerId(String playerId) {
        return messagesByPlayer.get(playerId);
    }

    public static GameToPlayersMessage of(String playerId1, Collection<GameToPlayerMessage> player1Messages,
                                          String playerId2, Collection<GameToPlayerMessage> player2Messages) {
        return new GameToPlayersMessage(Map.of(playerId1, player1Messages, playerId2, player2Messages));
    }
}