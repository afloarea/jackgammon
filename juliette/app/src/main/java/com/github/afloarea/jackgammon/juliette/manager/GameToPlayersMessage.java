package com.github.afloarea.jackgammon.juliette.manager;

import java.util.Collection;
import java.util.Map;
import java.util.function.BiConsumer;

public final class GameToPlayersMessage {
    private final Map<String, Collection<GameToPlayerMessage>> messagesByPlayer;

    private GameToPlayersMessage(Map<String, Collection<GameToPlayerMessage>> messagesByPlayer) {
        this.messagesByPlayer = messagesByPlayer;
    }

    public static GameToPlayersMessage of(String playerId1, Collection<GameToPlayerMessage> player1Messages,
                                          String playerId2, Collection<GameToPlayerMessage> player2Messages) {
        return new GameToPlayersMessage(Map.of(playerId1, player1Messages, playerId2, player2Messages));
    }

    public void forEachPlayerMessages(BiConsumer<String, Collection<GameToPlayerMessage>> action) {
        messagesByPlayer.forEach(action);
    }

    public Collection<GameToPlayerMessage> getMessagesForPlayer(String playerId) {
        return messagesByPlayer.get(playerId);
    }
}
