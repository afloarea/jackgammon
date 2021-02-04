package com.github.afloarea.jackgammon.juliette.verticles;

import com.github.afloarea.jackgammon.juliette.manager.Player;
import com.github.afloarea.jackgammon.juliette.messages.client.PlayerJoinMessage;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public final class MatchMakingVerticle extends AbstractVerticle {
    private static final Logger LOG = LoggerFactory.getLogger(MatchMakingVerticle.class);

    private final Map<String, Deque<Player>> waitingPlayersByKeyword = new HashMap<>();

    @Override
    public void start() {
        vertx.eventBus().consumer(Endpoints.HANDLE_PLAYER_JOIN, this::handleJoin);
        vertx.eventBus().consumer(Endpoints.HANDLE_DISCONNECT, this::handleDisconnect);
    }

    private void handleJoin(Message<PlayerJoinMessage> joinMessage) {
        final String playerId = joinMessage.headers().get(Headers.CLIENT_ID);
        final String playerName = joinMessage.body().getPlayerName();
        final Player newPlayer = new Player(playerId, playerName);

        if (joinMessage.body().getMode() == PlayerJoinMessage.Mode.SINGLEPLAYER) {
            LOG.info("Creating new single player game for player: {}", playerName);
            vertx.deployVerticle(new SinglePlayerGameVerticle(newPlayer));
            return;
        }

        final String keyword = joinMessage.body().getKeyword();
        final Deque<Player> waitingPlayers = waitingPlayersByKeyword.computeIfAbsent(keyword, k -> new ArrayDeque<>());

        if (waitingPlayers.isEmpty()) {
            waitingPlayers.add(newPlayer);
            return;
        }

        final Player opponent = waitingPlayers.removeFirst();

        LOG.info("Creating new game (keyword {}) for players: {}, {}", keyword, playerName, opponent.getName());
        vertx.deployVerticle(new GameVerticle(opponent, newPlayer));
    }

    private void handleDisconnect(Message<String> msg) {
        final String playerId = msg.headers().get(Headers.CLIENT_ID);
        final boolean removed = waitingPlayersByKeyword.values().stream()
                .anyMatch(queue -> queue.removeIf(player -> player.getId().equals(playerId)));
        if (removed) {
            LOG.info("Disconnected waiting player with id {}", playerId);
        }
    }
}
