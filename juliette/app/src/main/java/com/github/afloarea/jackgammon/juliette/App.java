package com.github.afloarea.jackgammon.juliette;

import com.github.afloarea.jackgammon.juliette.verticles.DispatcherVerticle;
import com.github.afloarea.jackgammon.juliette.verticles.MatchMakingVerticle;
import com.github.afloarea.jackgammon.juliette.verticles.WebSocketVerticle;
import io.vertx.core.Future;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;

public class App {
    private static final Logger LOG = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        final var vertx = Vertx.vertx();
        setUpCodecs(vertx);

        deploySequential(vertx, List.of(MatchMakingVerticle.class, DispatcherVerticle.class, WebSocketVerticle.class))
                .onSuccess(v -> LOG.info("Deployment complete"));
    }

    private static Future<Void> deploySequential(Vertx vertx, Collection<Class<? extends Verticle>> verticles) {
        var future = Future.<String>succeededFuture();
        for (var verticleClass : verticles) {
            future = future.compose(id -> vertx.deployVerticle(verticleClass.getName()));
        }
        return future.mapEmpty();
    }

    private static void setUpCodecs(Vertx vertx) {
        final var immutableCodec = new ImmutableLocalCodec<>();
        vertx.eventBus().registerCodec(immutableCodec);
        vertx.eventBus().codecSelector(obj -> immutableCodec.name());
    }

    private static final class ImmutableLocalCodec<T> implements MessageCodec<T, T> {
        @Override
        public void encodeToWire(Buffer buffer, T t) {
            throw new UnsupportedOperationException("Encoding to wire not supported");
        }

        @Override
        public T decodeFromWire(int pos, Buffer buffer) {
            throw new UnsupportedOperationException("Decoding from wire not supported");
        }

        @Override
        public T transform(T t) {
            return t;
        }

        @Override
        public String name() {
            return "immutable-codec";
        }

        @Override
        public byte systemCodecID() {
            return -1; // custom user codec must return -1
        }
    }
}
