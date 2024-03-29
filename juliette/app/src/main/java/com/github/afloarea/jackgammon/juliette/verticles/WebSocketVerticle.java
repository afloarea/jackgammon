package com.github.afloarea.jackgammon.juliette.verticles;

import com.github.afloarea.jackgammon.juliette.messages.DisconnectEvent;
import com.github.afloarea.jackgammon.juliette.messages.SimpleEvents;
import com.github.afloarea.jackgammon.juliette.messages.client.ClientToServerEvent;
import com.github.afloarea.jackgammon.juliette.messages.server.ServerToClientEvent;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class WebSocketVerticle extends AbstractVerticle {
    private static final Logger LOG = LoggerFactory.getLogger(WebSocketVerticle.class);

    private static final Duration PING_INTERVAL = Duration.ofSeconds(30);

    private HttpServer httpServer;
    private final Map<String, ServerWebSocket> webSocketByClientId = new HashMap<>();

    @Override
    public void start(Promise<Void> startPromise) {
        final int port = Integer.parseInt(System.getenv().getOrDefault("JACKGAMMON_PORT", "8080"));
        LOG.info("Starting WebSocketVerticle on port {} ...", port);

        vertx.eventBus().<ServerToClientEvent>consumer(Endpoints.SEND_TO_PLAYER).handler(this::handleOutgoing);

        httpServer = vertx.createHttpServer();
        httpServer.webSocketHandler(this::handleIncoming)
                .listen(port)
                .<Void>mapEmpty()
                .onComplete(startPromise);

        // when behind a reverse proxy, the reverse proxy can reach its configured timeout
        // and if there is no traffic, then it will simply sever the connection
        vertx.setPeriodic(PING_INTERVAL.toMillis(), timerId ->
                webSocketByClientId.values().forEach(webSocket -> webSocket.writePing(Buffer.buffer("ping"))));
    }

    private void handleOutgoing(Message<ServerToClientEvent> eventMsg) {
        if (eventMsg.body() instanceof DisconnectEvent) {
            handlePlayerDisconnect(eventMsg);
            return;
        }
        handleServerToClientMessage(eventMsg);
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

    private void handlePlayerDisconnect(Message<?> msg) {
        final var clientId = msg.headers().get(Headers.CLIENT_ID);
        final var webSocket = webSocketByClientId.get(clientId);
        webSocket.close().onComplete(ar ->
                LOG.info("Disconnecting {} succeeded: {}", clientId, ar.succeeded()));
    }

    private void handleIncoming(ServerWebSocket webSocket) {
        final var clientId = UUID.randomUUID().toString();
        LOG.info("Received web socket connection. Assigning id: {}", clientId);
        webSocketByClientId.put(clientId, webSocket);

        webSocket.textMessageHandler(msg -> vertx.eventBus().send(
                Endpoints.HANDLE_INCOMING_MESSAGE,
                Json.decodeValue(msg, ClientToServerEvent.class),
                new DeliveryOptions().addHeader(Headers.CLIENT_ID, clientId)));

        webSocket.exceptionHandler(ex -> LOG.error("Exception on WebSocket", ex));
        webSocket.closeHandler(v -> {
            LOG.info("WebSocket closed for client {}", clientId);
            webSocketByClientId.remove(clientId);
            vertx.eventBus().send(
                    Endpoints.HANDLE_INCOMING_MESSAGE,
                    SimpleEvents.DISCONNECT,
                    new DeliveryOptions().addHeader(Headers.CLIENT_ID, clientId));
        });
    }

    @Override
    public void stop(Promise<Void> stopPromise) {
        LOG.info("Stopping WebSocketVerticle...");
        httpServer.close(stopPromise);
    }
}
