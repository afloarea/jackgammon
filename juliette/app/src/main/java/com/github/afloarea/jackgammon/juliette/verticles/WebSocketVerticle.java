package com.github.afloarea.jackgammon.juliette.verticles;

import com.github.afloarea.jackgammon.juliette.messages.client.ClientToServerEvent;
import com.github.afloarea.jackgammon.juliette.messages.server.ServerToClientEvent;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class WebSocketVerticle extends AbstractVerticle {
    private static final Logger LOG = LoggerFactory.getLogger(WebSocketVerticle.class);

    private HttpServer httpServer;
    private final Map<String, ServerWebSocket> webSocketByClientId = new HashMap<>();

    @Override
    public void start(Promise<Void> startPromise) {
        LOG.info("Starting WebSocketVerticle...");

        vertx.eventBus().<ServerToClientEvent>consumer(Endpoints.SEND_TO_PLAYER).handler(this::handleServerToClientMessage);
        vertx.eventBus().<String>consumer(Endpoints.DISCONNECT_PLAYER).handler(this::handlePlayerDisconnect);

        vertx.createHttpServer()
                .webSocketHandler(this::handleWebSocket)
                .listen(8080)
                .onSuccess(startedServer -> httpServer = startedServer)
                .<Void>mapEmpty()
                .onComplete(startPromise);
    }

    private void handleServerToClientMessage(Message<ServerToClientEvent> msg) {
        final var clientId = msg.headers().get(Headers.CLIENT_ID);
        LOG.info("Sending message to client {}: {}", clientId, msg.body());
        final var webSocket = webSocketByClientId.get(clientId);
        if (!webSocket.isClosed()) {
            LOG.info("Sending message to {}: {}", clientId, msg.body());
            webSocket.writeTextMessage(Json.encode(msg.body()));
        } else {
            LOG.warn("Client WebSocket closed");
        }
    }

    private void handlePlayerDisconnect(Message<String> msg) {
        final var clientId = msg.headers().get(Headers.CLIENT_ID);
        LOG.info("Disconnecting client {}", clientId);
        final var webSocket = webSocketByClientId.remove(clientId);
        webSocket.close((short) 1001, msg.body()).onComplete(ar ->
                LOG.info("Disconnecting {} succeeded: {}", clientId, ar.succeeded()));
    }

    private void handleWebSocket(ServerWebSocket webSocket) {
        final var clientId = UUID.randomUUID().toString();
        LOG.info("Received web socket connection. Assigning id: {}", clientId);
        webSocketByClientId.put(clientId, webSocket);

        webSocket.textMessageHandler(msg -> vertx.eventBus().send(
                Endpoints.HANDLE_INCOMING_MESSAGE,
                Json.decodeValue(msg, ClientToServerEvent.class),
                new DeliveryOptions().addHeader(Headers.CLIENT_ID, clientId)));

        webSocket.exceptionHandler(ex -> LOG.error("Exception on WebSocket", ex));
        webSocket.closeHandler(v -> LOG.info("WebSocket closed"));
        webSocket.endHandler(v -> {
            LOG.info("WebSocket ended");
            vertx.eventBus().publish(
                    Endpoints.HANDLE_DISCONNECT,
                    "Client disconnected",
                    new DeliveryOptions().addHeader(Headers.CLIENT_ID, clientId));
        });
    }

    @Override
    public void stop(Promise<Void> stopPromise){
        LOG.info("Stopping WebSocketVerticle...");
        httpServer.close(stopPromise);
    }
}
