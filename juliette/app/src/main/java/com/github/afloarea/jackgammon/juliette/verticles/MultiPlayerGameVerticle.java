package com.github.afloarea.jackgammon.juliette.verticles;

import com.github.afloarea.jackgammon.juliette.manager.Game;
import com.github.afloarea.jackgammon.juliette.manager.GameToPlayersMessage;
import com.github.afloarea.jackgammon.juliette.manager.Player;
import com.github.afloarea.jackgammon.juliette.manager.PlayerToGameMessage;
import com.github.afloarea.jackgammon.juliette.messages.DisconnectMessage;
import com.github.afloarea.jackgammon.juliette.messages.SimpleMessages;
import com.github.afloarea.jackgammon.juliette.messages.client.ClientToServerEvent;
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

public final class MultiPlayerGameVerticle extends AbstractVerticle {
    private static final Logger LOG = LoggerFactory.getLogger(MultiPlayerGameVerticle.class);

    private final Map<String, Player> playersById;
    private final Game game;

    public MultiPlayerGameVerticle(Player firstPlayer, Player secondPlayer) {
        this.playersById = Stream.of(firstPlayer, secondPlayer)
                .collect(Collectors.toMap(Player::getId, Function.identity()));
        this.game = Game.setUpGame(firstPlayer, secondPlayer);
    }

    @Override
    public void start() {
        vertx.eventBus().consumer(deploymentID(), (Message<ClientToServerEvent> msg) -> {
            final String playerId = msg.headers().get(Headers.CLIENT_ID);
            if (msg.body() instanceof PlayerToGameMessage playerMessage) {
                handlePlayerMessage(playerId, playerMessage);
            } else if (msg.body() instanceof DisconnectMessage) {
                handleDisconnect(playerId);
            }
        });

        vertx.eventBus().send(Endpoints.REGISTER, new RegistrationInfo(deploymentID(), playersById.keySet()));
        sendMessagesToPlayers(game.init());
    }

    private void handlePlayerMessage(String playerId, PlayerToGameMessage msg) {
        final GameToPlayersMessage result = playersById.get(playerId).executeMoveMessage(msg);
        sendMessagesToPlayers(result);
        if (game.isOver()) {
            undeploy(playersById.keySet());
        }
    }

    private void sendMessagesToPlayers(GameToPlayersMessage messages) {
        messages.forEachPlayerMessages((playerId, playerMessages) -> {
            final var deliveryOptions = new DeliveryOptions().addHeader(Headers.CLIENT_ID, playerId);
            playerMessages.forEach(msg -> vertx.eventBus().send(Endpoints.SEND_TO_PLAYER, msg, deliveryOptions));
        });
    }

    private void handleDisconnect(String playerId) {
        final Player disconnectedPlayer = playersById.get(playerId);
        LOG.warn("Player {} disconnected.", disconnectedPlayer.getName());
        undeploy(Set.of(disconnectedPlayer.getOpponent().getId()));
    }

    private void undeploy(Set<String> playersToDisconnect) {
        LOG.info("Ending game with players: {}", playersById.keySet());
        playersToDisconnect.forEach(playerId ->
                vertx.eventBus().send(Endpoints.SEND_TO_PLAYER,
                        SimpleMessages.DISCONNECT_MESSAGE,
                        new DeliveryOptions().addHeader(Headers.CLIENT_ID, playerId)));

        vertx.eventBus().send(Endpoints.UNREGISTER, new RegistrationInfo(deploymentID(), playersById.keySet()));
        vertx.undeploy(deploymentID());
    }
}
