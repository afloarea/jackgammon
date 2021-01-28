package com.github.afloarea.jackgammon.juliette.messages.server;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.github.afloarea.jackgammon.juliette.manager.GameToPlayerMessage;

import java.util.Objects;

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

    private InitGameMessage(String playerName, String opponentName, String playerBoard, String opponentBoard) {
        this.playerName = playerName;
        this.opponentName = opponentName;
        this.playerBoard = playerBoard;
        this.opponentBoard = opponentBoard;
    }

    public static InitGameMessage buildFirstPlayerMessage(String playerName, String opponentName) {
        return new InitGameMessage(playerName, opponentName, BOARD_PLAYER_VALUE, BOARD_OPPONENT_VALUE);
    }

    public static InitGameMessage buildSecondPlayerMessage(String playerName, String opponentName) {
        return new InitGameMessage(playerName, opponentName, BOARD_OPPONENT_VALUE, BOARD_PLAYER_VALUE);
    }

    public String getPlayerName() {
        return playerName;
    }

    public String getOpponentName() {
        return opponentName;
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
        if (o == null || getClass() != o.getClass()) return false;
        InitGameMessage that = (InitGameMessage) o;
        return playerName.equals(that.playerName)
                && opponentName.equals(that.opponentName)
                && playerBoard.equals(that.playerBoard)
                && opponentBoard.equals(that.opponentBoard);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerName, opponentName, playerBoard, opponentBoard);
    }

    @Override
    public String toString() {
        return "InitGameMessage{" +
                "playerName='" + playerName + '\'' +
                ", opponentName='" + opponentName + '\'' +
                ", playerBoard='" + playerBoard + '\'' +
                ", opponentBoard='" + opponentBoard + '\'' +
                '}';
    }
}
