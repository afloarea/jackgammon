package com.github.afloarea.jackgammon.juliette.manager;

import java.util.Collection;
import java.util.Map;
import java.util.function.BiConsumer;

public final class GameToPlayersMessages {
    private final Map<String, Collection<GameToPlayerEvent>> messagesByPlayer;

    private GameToPlayersMessages(Map<String, Collection<GameToPlayerEvent>> messagesByPlayer) {
        this.messagesByPlayer = messagesByPlayer;
    }

    public static GameToPlayersMessages of(String playerId1, Collection<GameToPlayerEvent> player1Messages,
                                           String playerId2, Collection<GameToPlayerEvent> player2Messages) {
        return new GameToPlayersMessages(Map.of(playerId1, player1Messages, playerId2, player2Messages));
    }

    public void forEachPlayerMessages(BiConsumer<String, Collection<GameToPlayerEvent>> action) {
        messagesByPlayer.forEach(action);
    }

    public Collection<GameToPlayerEvent> getMessagesForPlayer(String playerId) {
        return messagesByPlayer.get(playerId);
    }
}
