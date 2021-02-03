package com.github.afloarea.jackgammon.juliette.verticles;

import com.github.afloarea.jackgammon.juliette.manager.Player;
import com.github.afloarea.jackgammon.juliette.messages.client.PlayerJoinMessage;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.Deque;

public final class MatchMakingVerticle extends AbstractVerticle {
    private static final Logger LOG = LoggerFactory.getLogger(MatchMakingVerticle.class);

    //private final Map<String, Deque<Player>> waitingPlayersByKeyword = new HashMap<>();
    private final Deque<Player> waitingPlayers = new ArrayDeque<>();

    @Override
    public void start() {
        vertx.eventBus().consumer(Endpoints.HANDLE_PLAYER_JOIN, this::handleJoin);
        vertx.eventBus().consumer(Endpoints.HANDLE_DISCONNECT, this::handleDisconnect);
    }

    private void handleJoin(Message<PlayerJoinMessage> joinMessage) {
        final String playerId = joinMessage.headers().get(Headers.CLIENT_ID);
        final String playerName = joinMessage.body().getPlayerName();
        final Player newPlayer = new Player(playerId, playerName);

        if (waitingPlayers.isEmpty()) {
            waitingPlayers.add(newPlayer);
            return;
        }

        final Player opponent = waitingPlayers.removeFirst();

        vertx.deployVerticle(new GameVerticle(opponent, newPlayer));
    }

    private void handleDisconnect(Message<String> msg) {
        final String playerId = msg.headers().get(Headers.CLIENT_ID);
        if (waitingPlayers.removeIf(player -> player.getId().equals(playerId))) {
            LOG.info("Disconnected waiting player with id {}", playerId);
        }
    }
}
