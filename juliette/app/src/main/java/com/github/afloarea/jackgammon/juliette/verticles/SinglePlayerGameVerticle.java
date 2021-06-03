package com.github.afloarea.jackgammon.juliette.verticles;

import com.github.afloarea.jackgammon.juliette.manager.*;
import com.github.afloarea.jackgammon.juliette.messages.client.PlayerJoinMessage;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Random;
import java.util.Set;

public final class SinglePlayerGameVerticle extends AbstractVerticle {
    private static final Logger LOG = LoggerFactory.getLogger(SinglePlayerGameVerticle.class);

    private final Player player;
    private final Player computer;
    private final Game game;

    public SinglePlayerGameVerticle(Player player, PlayerJoinMessage.Mode gameMode) {
        final var computerName = gameMode == PlayerJoinMessage.Mode.RANDOM ? "RandomComputer" : "NeuralComputer";
        this.player = player;
        this.computer = new Player("computer-id", computerName);

        player.setOpponent(computer);
        computer.setOpponent(player);

        this.game = new SinglePlayerGame(
                player.getName(), computer.getName(), Map.of(player.getId(), player.getName(), computer.getId(), computerName));

        player.setGame(game);
        computer.setGame(game);
    }

    @Override
    public void start() {
        vertx.eventBus().consumer(Endpoints.HANDLE_DISCONNECT, this::handleDisconnect);
        vertx.eventBus().consumer(deploymentID(), (Message<PlayerToGameMessage> msg) -> {
            final var messages = player.executeMoveMessage(msg.body());
            sendMessagesToPlayers(messages);
        });

        vertx.eventBus().send(Endpoints.REGISTER, new RegistrationInfo(deploymentID(), Set.of(player.getId())));
        sendMessagesToPlayers(game.init());
    }

    private void sendMessagesToPlayers(GameToPlayersMessage messages) {
        final var deliveryOptions = new DeliveryOptions().addHeader(Headers.CLIENT_ID, player.getId());
        messages.getMessagesForPlayer(player.getId()).forEach(message ->
                vertx.eventBus().send(Endpoints.SEND_TO_PLAYER, message, deliveryOptions));
    }

    private void handleDisconnect(Message<String> msg) {
        final String playerId = msg.headers().get(Headers.CLIENT_ID);
        if (!playerId.equals(player.getId())) {
            return;
        }

        LOG.warn("Player {} disconnected.", player.getName());
        undeploy();
    }

    private void undeploy() {
        LOG.info("Ending game with players: {}", player.getId());
        if (game.isOver()) {
            vertx.eventBus().send(Endpoints.DISCONNECT_PLAYER,
                    "Game over",
                    new DeliveryOptions().addHeader(Headers.CLIENT_ID, player.getId()));
        }

        vertx.eventBus().send(Endpoints.UNREGISTER, new RegistrationInfo(deploymentID(), Set.of(player.getId())));
        vertx.undeploy(deploymentID());
    }

}
