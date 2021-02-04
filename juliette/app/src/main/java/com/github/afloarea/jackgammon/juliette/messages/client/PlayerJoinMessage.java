package com.github.afloarea.jackgammon.juliette.messages.client;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;
import java.util.StringJoiner;

public final class PlayerJoinMessage implements ClientToServerEvent {

    private final String playerName;
    private final Options options;

    @JsonCreator
    public PlayerJoinMessage(@JsonProperty("playerName") String playerName,
                             @JsonProperty("options") Options options) {
        this.playerName = playerName;
        this.options = options;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlayerJoinMessage)) return false;
        PlayerJoinMessage that = (PlayerJoinMessage) o;
        return Objects.equals(options, that.options) && Objects.equals(playerName, that.playerName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerName, options);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", PlayerJoinMessage.class.getSimpleName() + "[", "]")
                .add("playerName='" + playerName + "'")
                .add("options=" + options)
                .toString();
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getKeyword() {
        return options.keyword;
    }

    public Mode getMode() {
        return options.mode;
    }

    public static class Options {
        private final Mode mode;
        private final String keyword;

        @JsonCreator
        public Options(@JsonProperty(value = "mode", defaultValue = "singleplayer") Mode mode,
                       @JsonProperty(value = "keyword", defaultValue = "all") String keyword) {
            this.mode = mode;
            this.keyword = keyword == null ? "all" : keyword;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Options options = (Options) o;
            return mode == options.mode && Objects.equals(keyword, options.keyword);
        }

        @Override
        public int hashCode() {
            return Objects.hash(mode, keyword);
        }

        @Override
        public String toString() {
            return "Options{" +
                    "mode=" + mode +
                    ", keyword='" + keyword + '\'' +
                    '}';
        }
    }

    public enum Mode {
        @JsonProperty("multiplayer") MULTIPLAYER,
        @JsonProperty("singleplayer") SINGLEPLAYER,
        @JsonProperty("private") PRIVATE,
    }

}
