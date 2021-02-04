package com.github.afloarea.jackgammon.juliette;

import com.github.afloarea.jackgammon.juliette.messages.client.ClientToServerEvent;
import io.vertx.core.json.Json;
import org.junit.jupiter.api.Test;

public class TestJson {

    private static final String json = "{\n" +
            "  \"type\": \"join\",\n" +
            "  \"playerName\": \"Papagal\",\n" +
            "  \"options\": {\n" +
            "    \"mode\": \"private\"\n" +
            "  }\n" +
            "}";

    @Test
    void test() {
        final var msg = Json.decodeValue(json, ClientToServerEvent.class);
        System.out.println(msg);
    }

}
