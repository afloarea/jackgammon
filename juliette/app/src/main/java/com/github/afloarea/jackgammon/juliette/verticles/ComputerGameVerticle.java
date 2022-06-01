package com.github.afloarea.jackgammon.juliette.verticles;

import com.github.afloarea.jackgammon.juliette.GameMove;
import com.github.afloarea.jackgammon.juliette.manager.*;
import com.github.afloarea.jackgammon.juliette.messages.DisconnectMessage;
import com.github.afloarea.jackgammon.juliette.messages.SimpleMessages;
import com.github.afloarea.jackgammon.juliette.messages.client.ClientToServerEvent;
import com.github.afloarea.jackgammon.juliette.messages.client.PlayerRollMessage;
import com.github.afloarea.jackgammon.juliette.messages.client.SelectMoveMessage;
import com.github.afloarea.jackgammon.juliette.messages.server.PromptMoveMessage;
import com.github.afloarea.jackgammon.juliette.messages.server.PromptRollMessage;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.Random;
import java.util.Set;

public final class ComputerGameVerticle extends AbstractVerticle {
    private static final Logger LOG = LoggerFactory.getLogger(ComputerGameVerticle.class);

    private static final Random RANDOM = new Random();

    private final Player player;
    private final Player computer = new Player("computer-id", "NeuralComputer");
    private final Game game;

    public ComputerGameVerticle(Player player) {
        this.player = player;
        this.game = Game.setUpGame(player, computer);
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
        Optional<GameToPlayersMessage> result = Optional.of(player.executeMoveMessage(msg));
        while (result.isPresent()) {
            sendMessagesToPlayers(result.get());
            if (game.isOver()) {
                undeploy();
                break;
            }
            result = generateResponse(result.get());
        }
    }

    private Optional<GameToPlayersMessage> generateResponse(GameToPlayersMessage original) {
        final Optional<GameToPlayerMessage> request = original.getMessagesForPlayer(computer.getId()).stream()
                .filter(msg -> msg instanceof PromptRollMessage
                        || msg instanceof PromptMoveMessage promptMsg && !promptMsg.getPossibleMoves().isEmpty())
                .findFirst();

        if (request.isEmpty()) {
            return Optional.empty();
        }

        final var message = request.get();
        if (message instanceof PromptRollMessage) {
            return Optional.of(computer.executeMoveMessage(new PlayerRollMessage()));
        } else {
            final PromptMoveMessage moveRequest = (PromptMoveMessage) message;
            return Optional.of(computer.executeMoveMessage(generateMove(moveRequest)));
        }
    }

    private PlayerToGameMessage generateMove(PromptMoveMessage moveRequest) {
        final int selectedSource = RANDOM.nextInt(moveRequest.getPossibleMoves().size());
        final var entry = moveRequest.getPossibleMoves().entrySet().stream()
                .skip(selectedSource)
                .limit(1)
                .findFirst().orElseThrow();

        final int selectedTarget = RANDOM.nextInt(entry.getValue().size());
        final String source = entry.getKey();
        final String target = entry.getValue().stream().skip(selectedTarget).limit(1).findFirst().orElseThrow();

        return new SelectMoveMessage(new GameMove(source, target));
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
