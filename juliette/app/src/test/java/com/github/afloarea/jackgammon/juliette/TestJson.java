package com.github.afloarea.jackgammon.juliette;

import com.github.afloarea.jackgammon.juliette.messages.client.ClientToServerEvent;
import io.vertx.core.json.Json;
import org.junit.jupiter.api.Test;

class TestJson {

    private static final String JSON = """
            {
                "type": "join",
                "playerName": "Papagal",
                "options": {
                    "mode": "private"
                }
            }
            """;

    @Test
    void test() {
        final var msg = Json.decodeValue(JSON, ClientToServerEvent.class);
        System.out.println(msg);
    }

}
