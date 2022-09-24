package com.github.afloarea.jackgammon.juliette;

import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;

public class StaticServer {

    public static void main(String[] args) {
        final var vertx = Vertx.vertx();

        final Router router = Router.router(vertx);

        router.route().handler(StaticHandler.create("../jimmy"));

        vertx.createHttpServer()
                .requestHandler(router)
                .listen(8082)
                .onComplete(server -> System.out.println("Server ready"));

        Runtime.getRuntime().addShutdownHook(new Thread(vertx::close));
    }

}
