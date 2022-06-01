package com.github.afloarea.jackgammon.juliette.verticles;

import com.github.afloarea.jackgammon.juliette.manager.*;
import com.github.afloarea.jackgammon.juliette.messages.DisconnectMessage;
import com.github.afloarea.jackgammon.juliette.messages.SimpleMessages;
import com.github.afloarea.jackgammon.juliette.messages.client.ClientToServerEvent;
import com.github.afloarea.jackgammon.juliette.messages.client.PlayerJoinMessage;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

public final class SinglePlayerGameVerticle extends AbstractVerticle {
    private static final Logger LOG = LoggerFactory.getLogger(SinglePlayerGameVerticle.class);

    private final Player player;
    private final Game game;

    public SinglePlayerGameVerticle(Player player, PlayerJoinMessage.Mode gameMode) {
        final var computerName = gameMode == PlayerJoinMessage.Mode.RANDOM ? "RandomComputer" : "NeuralComputer";
        this.player = player;
        final Player computer = new Player("computer-id", computerName);

        player.setOpponent(computer);
        computer.setOpponent(player);

        this.game = new SinglePlayerGame(
                player.getId(), computer.getId(), Map.of(player.getId(), player.getName(), computer.getId(), computerName));

        player.setGame(game);
        computer.setGame(game);
    }

    @Override
    public void start() {
        vertx.eventBus().consumer(deploymentID(), (Message<ClientToServerEvent> msg) -> {
            if (msg.body() instanceof PlayerToGameMessage playerMessage) {
                handlePlayerMessage(playerMessage);
            } else if (msg.body() instanceof DisconnectMessage) {
                handleDisconnect();
            }
        });

        vertx.eventBus().send(Endpoints.REGISTER, new RegistrationInfo(deploymentID(), Set.of(player.getId())));
        sendMessagesToPlayers(game.init());
    }

    private void handlePlayerMessage(PlayerToGameMessage msg) {
        final var messages = player.executeMoveMessage(msg);
        sendMessagesToPlayers(messages);
    }

    private void sendMessagesToPlayers(GameToPlayersMessage messages) {
        final var deliveryOptions = new DeliveryOptions().addHeader(Headers.CLIENT_ID, player.getId());
        messages.getMessagesForPlayer(player.getId()).forEach(message ->
                vertx.eventBus().send(Endpoints.SEND_TO_PLAYER, message, deliveryOptions));
    }

    private void handleDisconnect() {
        LOG.warn("Player {} disconnected.", player.getName());
        undeploy();
    }

    private void undeploy() {
        LOG.info("Ending game with players: {}", player.getId());
        if (game.isOver()) {
            vertx.eventBus().send(Endpoints.SEND_TO_PLAYER,
                    SimpleMessages.DISCONNECT_MESSAGE,
                    new DeliveryOptions().addHeader(Headers.CLIENT_ID, player.getId()));
        }

        vertx.eventBus().send(Endpoints.UNREGISTER, new RegistrationInfo(deploymentID(), Set.of(player.getId())));
        vertx.undeploy(deploymentID());
    }

}
