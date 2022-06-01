package com.github.afloarea.jackgammon.juliette.messages.client;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PlayerJoinMessage(String playerName, Options options) implements ClientToServerEvent {

    public String keyword() {
        return options.keyword;
    }

    public Mode mode() {
        return options.mode;
    }

    public record Options(Mode mode, String keyword) {
        public Options {
            if (keyword == null) {
                keyword = "all";
            }
        }
    }

    public enum Mode {
        @JsonProperty("multiplayer") MULTIPLAYER,
        @JsonProperty("random") RANDOM,
        @JsonProperty("neural") NEURAL,
        @JsonProperty("private") PRIVATE,
    }

}
