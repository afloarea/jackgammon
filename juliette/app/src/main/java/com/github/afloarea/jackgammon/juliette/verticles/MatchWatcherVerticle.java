package com.github.afloarea.jackgammon.juliette.verticles;

import com.github.afloarea.jackgammon.juliette.manager.*;
import com.github.afloarea.jackgammon.juliette.message.client.ClientToServerEvent;
import com.github.afloarea.jackgammon.juliette.message.client.PlayerJoinMessage;
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
        vertx.eventBus().consumer("handlePlayerMessage", this::handlePlayerMessage);
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
                    final var game = new DefaultGame(opponent.getId(), player.getId());
                    opponent.setGame(game);
                    player.setGame(game);
                    playersById.put(player.getId(), player);
                    playersById.put(opponent.getId(), opponent);
                    initGame(game);
                }
            }
            return;
        }

        // handle disconnect?

        // normal message
        final var game = playersById.get(playerId).getGame();
        final var result = game.handle((PlayerToGameMessage) body);
        sendMessagesToPlayers(result, game);
    }

    private void initGame(Game game) {
        final var result = game.init();
        sendMessagesToPlayers(result, game);
    }

    private void sendMessagesToPlayers(GameToPlayersMessage messages, Game game) {
        game.getPlayersIds().forEach(playerId -> {
            final var playerMessages = messages.getMessagesForPlayerId(playerId);
            final var deliveryOptions = new DeliveryOptions().addHeader(Headers.CLIENT_ID, playerId);
            playerMessages.forEach(message ->
                    vertx.eventBus().send(Endpoints.SEND_TO_PLAYER, message, deliveryOptions));
        });
    }
}
