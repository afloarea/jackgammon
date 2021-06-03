package com.github.afloarea.jackgammon.juliette.verticles;

import com.github.afloarea.jackgammon.juliette.GameMove;
import com.github.afloarea.jackgammon.juliette.manager.*;
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
        vertx.eventBus().consumer(Endpoints.HANDLE_DISCONNECT, this::handleDisconnect);
        vertx.eventBus().consumer(deploymentID(), (Message<PlayerToGameMessage> msg) -> {
            Optional<GameToPlayersMessage> result = Optional.of(player.executeMoveMessage(msg.body()));
            while (result.isPresent()) {
                sendMessagesToPlayers(result.get());
                if (game.isOver()) {
                    undeploy();
                    break;
                }
                result = generateResponse(result.get());
            }
        });

        vertx.eventBus().send(Endpoints.REGISTER, new RegistrationInfo(deploymentID(), Set.of(player.getId())));
        sendMessagesToPlayers(game.init());
    }

    private Optional<GameToPlayersMessage> generateResponse(GameToPlayersMessage original) {
        final Optional<GameToPlayerMessage> request = original.getMessagesForPlayer(computer.getId()).stream()
                .filter(msg -> msg instanceof PromptRollMessage
                        || msg instanceof PromptMoveMessage && !((PromptMoveMessage) msg).getPossibleMoves().isEmpty())
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
                .findFirst().get();

        final int selectedTarget = RANDOM.nextInt(entry.getValue().size());
        final String source = entry.getKey();
        final String target = entry.getValue().stream().skip(selectedTarget).limit(1).findFirst().get();

        return new SelectMoveMessage(new GameMove(source, target));
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
