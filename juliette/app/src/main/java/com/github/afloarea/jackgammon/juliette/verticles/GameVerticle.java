package com.github.afloarea.jackgammon.juliette.verticles;

import com.github.afloarea.jackgammon.juliette.manager.Game;
import com.github.afloarea.jackgammon.juliette.manager.GameToPlayersMessage;
import com.github.afloarea.jackgammon.juliette.manager.PlayerToGameMessage;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;

public final class GameVerticle extends AbstractVerticle {

    private final Game game;

    public GameVerticle(Game game) {
        this.game = game;
    }

    @Override
    public void start() throws Exception {
        vertx.eventBus().<PlayerToGameMessage>consumer("handlerPlayerMessage").handler(this::handlePlayerMessage);

        final var result = game.init();
        sendMessagesToPlayers(result);
    }

    private void handlePlayerMessage(Message<PlayerToGameMessage> message) {
        final var result = game.handle(message.body());
        sendMessagesToPlayers(result);
    }

    private void sendMessagesToPlayers(GameToPlayersMessage messages) {
        game.getPlayersIds().forEach(playerId -> {
            final var playerMessages = messages.getMessagesForPlayerId(playerId);
            final var deliveryOptions = new DeliveryOptions().addHeader(Headers.CLIENT_ID, playerId);
            playerMessages.forEach(message ->
                    vertx.eventBus().send(Endpoints.SEND_TO_PLAYER, message, deliveryOptions));
        });
    }
}
