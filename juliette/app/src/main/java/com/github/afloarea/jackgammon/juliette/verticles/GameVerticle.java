package com.github.afloarea.jackgammon.juliette.verticles;

import com.github.afloarea.jackgammon.juliette.manager.Game;
import com.github.afloarea.jackgammon.juliette.manager.GameToPlayersMessage;
import com.github.afloarea.jackgammon.juliette.manager.Player;
import com.github.afloarea.jackgammon.juliette.manager.PlayerToGameMessage;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class GameVerticle extends AbstractVerticle {
    private static final Logger LOG = LoggerFactory.getLogger(GameVerticle.class);

    private final Map<String, Player> playersById;
    private final Game game;

    public GameVerticle(Player firstPlayer, Player secondPlayer) {
        this.playersById = Stream.of(firstPlayer, secondPlayer)
                .collect(Collectors.toMap(Player::getId, Function.identity()));
        this.game = Game.setUpGame(firstPlayer, secondPlayer);
    }

    @Override
    public void start() {
        vertx.eventBus().consumer(Endpoints.HANDLE_DISCONNECT, this::handleDisconnect);
        vertx.eventBus().consumer(deploymentID(), (Message<PlayerToGameMessage> msg) -> {
            final String playerId = msg.headers().get(Headers.CLIENT_ID);
            final GameToPlayersMessage result = playersById.get(playerId).executeMoveMessage(msg.body());
            sendMessagesToPlayers(result);
            if (game.isOver()) {
                undeploy(playersById.keySet());
            }
        });

        vertx.eventBus().send(Endpoints.REGISTER, new RegistrationInfo(deploymentID(), playersById.keySet()));
        sendMessagesToPlayers(game.init());
    }

    private void sendMessagesToPlayers(GameToPlayersMessage messages) {
        messages.forEachPlayerMessages((playerId, playerMessages) -> {
            final var deliveryOptions = new DeliveryOptions().addHeader(Headers.CLIENT_ID, playerId);
            playerMessages.forEach(msg -> vertx.eventBus().send(Endpoints.SEND_TO_PLAYER, msg, deliveryOptions));
        });
    }

    private void handleDisconnect(Message<String> msg) {
        final String playerId = msg.headers().get(Headers.CLIENT_ID);
        if (!playersById.containsKey(playerId)) {
            return;
        }

        final Player disconnectedPlayer = playersById.get(playerId);
        LOG.warn("Player {} disconnected.", disconnectedPlayer.getName());
        undeploy(Set.of(disconnectedPlayer.getOpponent().getId()));
    }

    private void undeploy(Set<String> playersToDisconnect) {
        final String message = playersToDisconnect.size() == 1 ? "Opponent disconnected" : "Game over";
        playersToDisconnect.forEach(playerId ->
                vertx.eventBus().send(Endpoints.DISCONNECT_PLAYER,
                        message,
                        new DeliveryOptions().addHeader(Headers.CLIENT_ID, playerId)));

        vertx.eventBus().send(Endpoints.UNREGISTER, new RegistrationInfo(deploymentID(), playersById.keySet()));
        vertx.undeploy(deploymentID());
    }
}
