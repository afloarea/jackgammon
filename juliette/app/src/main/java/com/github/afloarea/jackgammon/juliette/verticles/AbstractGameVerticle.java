package com.github.afloarea.jackgammon.juliette.verticles;

import com.github.afloarea.jackgammon.juliette.manager.GameToPlayersMessages;
import com.github.afloarea.jackgammon.juliette.manager.PlayerToGameEvent;
import com.github.afloarea.jackgammon.juliette.messages.DisconnectEvent;
import com.github.afloarea.jackgammon.juliette.messages.SimpleEvents;
import com.github.afloarea.jackgammon.juliette.messages.client.ChatMessageEvent;
import com.github.afloarea.jackgammon.juliette.messages.client.ClientToServerEvent;
import com.github.afloarea.jackgammon.juliette.messages.server.DisplayChatMessageEvent;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.DeliveryOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public abstract class AbstractGameVerticle extends AbstractVerticle {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractGameVerticle.class);

    protected abstract void handlePlayerMessage(String playerId, PlayerToGameEvent playerMessage);

    protected abstract void handleDisconnect(String playerId);

    protected abstract Set<String> playerIds();

    @Override
    public void start() {
        vertx.eventBus().<ClientToServerEvent>consumer(deploymentID()).handler(msg -> {
            final String playerId = msg.headers().get(Headers.CLIENT_ID);
            if (msg.body() instanceof PlayerToGameEvent playerMessage) {
                handlePlayerMessage(playerId, playerMessage);
                return;
            }
            if (msg.body() instanceof DisconnectEvent) {
                handleDisconnect(playerId);
                return;
            }
            if (msg.body() instanceof ChatMessageEvent chatMessageEvent) {
                handleChatMessage(chatMessageEvent);
                return;
            }
            LOG.warn("Unexpected message: {}", msg.body());
        });

        vertx.eventBus().send(Endpoints.REGISTER, new RegistrationInfo(deploymentID(), playerIds()));
    }

    private void handleChatMessage(ChatMessageEvent event) {
        final var displayMessageEvent = new DisplayChatMessageEvent(event.author(), event.message());
        playerIds().stream()
                .map(id -> new DeliveryOptions().addHeader(Headers.CLIENT_ID, id))
                .forEach(deliveryOptions ->
                        vertx.eventBus().send(Endpoints.SEND_TO_PLAYER, displayMessageEvent, deliveryOptions));
    }

    protected final void sendMessagesToPlayers(GameToPlayersMessages messages) {
        messages.forEachPlayerMessages((playerId, playerMessages) -> {
            final var deliveryOptions = new DeliveryOptions().addHeader(Headers.CLIENT_ID, playerId);
            playerMessages.forEach(msg -> vertx.eventBus().send(Endpoints.SEND_TO_PLAYER, msg, deliveryOptions));
        });
    }

    protected void sendDisconnect(Set<String> playerIds) {
        playerIds.forEach(playerId ->
                vertx.eventBus().send(Endpoints.SEND_TO_PLAYER,
                        SimpleEvents.DISCONNECT,
                        new DeliveryOptions().addHeader(Headers.CLIENT_ID, playerId)));
    }

    protected final void undeploySelf() {
        vertx.eventBus().send(Endpoints.UNREGISTER, new RegistrationInfo(deploymentID(), playerIds()));
        vertx.undeploy(deploymentID());
    }
}
