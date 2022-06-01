package com.github.afloarea.jackgammon.juliette.verticles;

import com.github.afloarea.jackgammon.juliette.messages.DisconnectMessage;
import com.github.afloarea.jackgammon.juliette.messages.client.ClientToServerEvent;
import com.github.afloarea.jackgammon.juliette.messages.client.PlayerJoinMessage;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public final class DispatcherVerticle extends AbstractVerticle {
    private static final Logger LOG = LoggerFactory.getLogger(DispatcherVerticle.class);

    private final Map<String, String> addressesByClientId = new HashMap<>();

    @Override
    public void start() {
        vertx.eventBus().consumer(Endpoints.HANDLE_INCOMING_MESSAGE, this::handleIncomingMessage);

        vertx.eventBus().consumer(Endpoints.REGISTER, (Message<RegistrationInfo> msg) ->
                msg.body().clientIds().forEach(clientId -> addressesByClientId.put(clientId, msg.body().address())));

        vertx.eventBus().consumer(Endpoints.UNREGISTER, (Message<RegistrationInfo> msg) ->
                msg.body().clientIds().forEach(addressesByClientId::remove));
    }

    private void handleIncomingMessage(Message<ClientToServerEvent> msg) {
        if (msg.body() instanceof PlayerJoinMessage) {
            vertx.eventBus().send(
                    Endpoints.HANDLE_PLAYER_CONNECTION, msg.body(), new DeliveryOptions().setHeaders(msg.headers()));
            return;
        }
        if (msg.body() instanceof DisconnectMessage) {
            final var address = addressesByClientId.getOrDefault(
                    msg.headers().get(Headers.CLIENT_ID), Endpoints.HANDLE_PLAYER_CONNECTION);
            vertx.eventBus().send(address, msg.body(), new DeliveryOptions().setHeaders(msg.headers()));
            return;
        }

        // handle game message
        final var clientId = msg.headers().get(Headers.CLIENT_ID);
        final var address = addressesByClientId.get(clientId);
        if (address == null) {
            LOG.warn("No address to forward request with client id {}", clientId);
            return;
        }

        vertx.eventBus().send(address, msg.body(), new DeliveryOptions().setHeaders(msg.headers()));
    }
}
