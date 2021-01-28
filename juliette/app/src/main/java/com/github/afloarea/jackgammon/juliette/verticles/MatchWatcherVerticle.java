package com.github.afloarea.jackgammon.juliette.verticles;

import com.github.afloarea.jackgammon.juliette.manager.*;
import com.github.afloarea.jackgammon.juliette.messages.client.ClientToServerEvent;
import com.github.afloarea.jackgammon.juliette.messages.client.PlayerJoinMessage;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public final class MatchWatcherVerticle extends AbstractVerticle {
    private static final Logger LOG = LoggerFactory.getLogger(MatchWatcherVerticle.class);

    private final Deque<Player> waitingPlayers = new ArrayDeque<>();
    private final Map<String, Player> playersById = new HashMap<>();

    @Override
    public void start() {
        vertx.eventBus().consumer("handleDisconnect", this::handleDisconnect);
        vertx.eventBus().consumer("handlePlayerMessage", this::handlePlayerMessage);
    }

    private void handleDisconnect(Message<String> msg) {
        LOG.info("Received disconnect message {}", msg.body());
        final var playerId = msg.headers().get(Headers.CLIENT_ID);
        if (waitingPlayers.removeIf(player -> playerId.equals(player.getId()))) {
            return;
        }

        if (!playersById.containsKey(playerId)) {
            return;
        }

        final var playingPlayer = playersById.get(playerId);
        final var opponentId = playingPlayer.getOpponent().getId();

        playersById.remove(playerId);
        playersById.remove(opponentId);

        vertx.eventBus().send(Endpoints.DISCONNECT_PLAYER,
                "Opponent disconnected",
                new DeliveryOptions().addHeader(Headers.CLIENT_ID, opponentId));
    }

    private void handlePlayerMessage(Message<ClientToServerEvent> message) {
        LOG.info("Received message {}", message.body());

        final var body = message.body();
        final var playerId = message.headers().get(Headers.CLIENT_ID);
        if (body instanceof PlayerJoinMessage) {
            final var joinMessage = (PlayerJoinMessage) body;
            if (joinMessage.isPlayerReady()) {
                final var player = new Player(playerId, joinMessage.getPlayerName());
                if (waitingPlayers.isEmpty()) {
                    waitingPlayers.add(player);
                } else {
                    final var opponent = waitingPlayers.removeFirst();
                    final var game = Game.setUpGame(player, opponent);
                    playersById.put(player.getId(), player);
                    playersById.put(opponent.getId(), opponent);
                    initGame(game);
                }
            }
            return;
        }

        // handle disconnect?

        // normal message
        final var result = playersById.get(playerId).executeMoveMessage((PlayerToGameMessage) body);
        sendMessagesToPlayers(result);
    }

    private void initGame(Game game) {
        final var result = game.init();
        sendMessagesToPlayers(result);
    }

    private void sendMessagesToPlayers(GameToPlayersMessage messages) {
        messages.forEachPlayerMessages((playerId, playerMessages) -> {
            final var deliveryOptions = new DeliveryOptions().addHeader(Headers.CLIENT_ID, playerId);
            playerMessages.forEach(msg -> vertx.eventBus().send(Endpoints.SEND_TO_PLAYER, msg, deliveryOptions));
        });
    }
}
