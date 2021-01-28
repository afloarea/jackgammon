package com.github.afloarea.jackgammon.juliette.messages.server;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.github.afloarea.jackgammon.juliette.manager.GameToPlayerMessage;

import java.util.Objects;
import java.util.StringJoiner;

public final class InitGameMessage implements GameToPlayerMessage {
    private static final String BOARD_PLAYER_VALUE = "[\n" +
            "      {\"columnId\": \"A\", \"pieces\": 2},\n" +
            "      {\"columnId\": \"L\", \"pieces\": 5},\n" +
            "      {\"columnId\": \"R\", \"pieces\": 5},\n" +
            "      {\"columnId\": \"T\", \"pieces\": 3}\n" +
            "    ]";
    private static final String BOARD_OPPONENT_VALUE = "[\n" +
            "      {\"columnId\": \"F\", \"pieces\": 5},\n" +
            "      {\"columnId\": \"H\", \"pieces\": 3},\n" +
            "      {\"columnId\": \"M\", \"pieces\": 2},\n" +
            "      {\"columnId\": \"X\", \"pieces\": 5}\n" +
            "    ]";

    private final String playerName;
    private final String opponentName;
    private final String playerBoard;
    private final String opponentBoard;
    private final boolean startFirst;

    public InitGameMessage(String playerName, String opponentName, boolean firstPlayerStarts) {
        this.playerName = playerName;
        this.opponentName = opponentName;
        this.startFirst = firstPlayerStarts;
        if (firstPlayerStarts) {
            this.playerBoard = BOARD_PLAYER_VALUE;
            this.opponentBoard = BOARD_OPPONENT_VALUE;
        } else {
            this.playerBoard = BOARD_OPPONENT_VALUE;
            this.opponentBoard = BOARD_PLAYER_VALUE;
        }
    }

    public static InitGameMessage buildFirstPlayerMessage(String playerName, String opponentName) {
        return new InitGameMessage(playerName, opponentName, true);
    }

    public static InitGameMessage buildSecondPlayerMessage(String playerName, String opponentName) {
        return new InitGameMessage(playerName, opponentName, false);
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getOpponentName() {
        return opponentName;
    }

    public boolean isStartFirst() {
        return startFirst;
    }

    @JsonRawValue
    public String getBoard() {
        return "{\n" +
                "    \"player\": " + playerBoard +
                "    ,\n" +
                "    \"opponent\": " + opponentBoard +
                "    \n" +
                "  }";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InitGameMessage)) return false;
        InitGameMessage that = (InitGameMessage) o;
        return startFirst == that.startFirst
                && playerName.equals(that.playerName)
                && opponentName.equals(that.opponentName)
                && playerBoard.equals(that.playerBoard)
                && opponentBoard.equals(that.opponentBoard);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerName, opponentName, playerBoard, opponentBoard, startFirst);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", InitGameMessage.class.getSimpleName() + "[", "]")
                .add("playerName='" + playerName + "'")
                .add("opponentName='" + opponentName + "'")
                .add("playerBoard='" + playerBoard + "'")
                .add("opponentBoard='" + opponentBoard + "'")
                .add("startFirst=" + startFirst)
                .toString();
    }
}
