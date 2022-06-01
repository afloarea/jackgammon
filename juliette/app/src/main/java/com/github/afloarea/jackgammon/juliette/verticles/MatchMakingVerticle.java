package com.github.afloarea.jackgammon.juliette.verticles;

import com.github.afloarea.jackgammon.juliette.manager.Player;
import com.github.afloarea.jackgammon.juliette.messages.DisconnectMessage;
import com.github.afloarea.jackgammon.juliette.messages.client.ClientToServerEvent;
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
        vertx.eventBus().consumer(Endpoints.HANDLE_PLAYER_CONNECTION, this::handleConnection);
    }

    private void handleConnection(Message<ClientToServerEvent> clientMsg) {
        final String clientId = clientMsg.headers().get(Headers.CLIENT_ID);
        if (clientMsg.body() instanceof PlayerJoinMessage joinMessage) {
            handleJoin(clientId, joinMessage);
            return;
        }
        if (clientMsg.body() instanceof DisconnectMessage) {
            handleDisconnect(clientId);
        }

        //FIXME
    }

    private void handleJoin(String playerId, PlayerJoinMessage msgBody) {
        final String playerName = msgBody.playerName();
        final Player newPlayer = new Player(playerId, playerName);

        final var mode = msgBody.mode();
        if (mode == PlayerJoinMessage.Mode.RANDOM || mode == PlayerJoinMessage.Mode.NEURAL) {
            LOG.info("Creating new single player game for player: {}", playerName);
            vertx.deployVerticle(new SinglePlayerGameVerticle(newPlayer, mode));
            return;
        }

        final String keyword = msgBody.keyword();
        final Deque<Player> waitingPlayers = waitingPlayersByKeyword.computeIfAbsent(keyword, k -> new ArrayDeque<>());

        if (waitingPlayers.isEmpty()) {
            waitingPlayers.add(newPlayer);
            return;
        }

        final Player opponent = waitingPlayers.removeFirst();

        LOG.info("Creating new game (keyword {}) for players: {}, {}", keyword, playerName, opponent.getName());
        vertx.deployVerticle(new MultiPlayerGameVerticle(opponent, newPlayer));
    }

    private void handleDisconnect(String playerId) {
        final boolean removed = waitingPlayersByKeyword.values().stream()
                .anyMatch(queue -> queue.removeIf(player -> player.getId().equals(playerId)));
        if (removed) {
            LOG.info("Disconnected waiting player with id {}", playerId);
        }
    }
}
