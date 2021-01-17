package com.github.afloarea.jackgammon.juliette.message.client;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;
import java.util.StringJoiner;

public final class PlayerJoinMessage implements ClientToServerEvent {

    private final String playerName;
    private final boolean playerReady;

    @JsonCreator
    public PlayerJoinMessage(@JsonProperty("playerName") String playerName,
                             @JsonProperty("ready") boolean playerReady) {
        this.playerName = playerName;
        this.playerReady = playerReady;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlayerJoinMessage)) return false;
        PlayerJoinMessage that = (PlayerJoinMessage) o;
        return playerReady == that.playerReady && Objects.equals(playerName, that.playerName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerName, playerReady);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", PlayerJoinMessage.class.getSimpleName() + "[", "]")
                .add("playerName='" + playerName + "'")
                .add("playerReady=" + playerReady)
                .toString();
    }

    public String getPlayerName() {
        return playerName;
    }

    public boolean isPlayerReady() {
        return playerReady;
    }

}
