package com.github.afloarea.jackgammon.juliette.messages.server;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.github.afloarea.jackgammon.juliette.manager.GameToPlayerMessage;

public record InitGameMessage(
        String playerName,
        String opponentName,
        @JsonRawValue String board,
        boolean startFirst)
        implements GameToPlayerMessage {

    private static final String BOARD_PLAYER_VALUE = """
            [
              {"columnId": "A", "pieces": 2},
              {"columnId": "L", "pieces": 5},
              {"columnId": "R", "pieces": 5},
              {"columnId": "T", "pieces": 3}
            ]
            """;
    private static final String BOARD_OPPONENT_VALUE = """
            [
              {"columnId": "F", "pieces": 5},
              {"columnId": "H", "pieces": 3},
              {"columnId": "M", "pieces": 2},
              {"columnId": "X", "pieces": 5}
            ]
            """;

    public InitGameMessage(String playerName, String opponentName, boolean firstPlayerStarts) {
        this(playerName, opponentName, buildBoard(firstPlayerStarts), firstPlayerStarts);
    }

    public static InitGameMessage buildFirstPlayerMessage(String playerName, String opponentName) {
        return new InitGameMessage(playerName, opponentName, true);
    }

    public static InitGameMessage buildSecondPlayerMessage(String playerName, String opponentName) {
        return new InitGameMessage(playerName, opponentName, false);
    }

    private static String buildBoard(boolean firstPlayerStarts) {
        final String playerBoard;
        final String opponentBoard;
        if (firstPlayerStarts) {
            playerBoard = BOARD_PLAYER_VALUE;
            opponentBoard = BOARD_OPPONENT_VALUE;
        } else {
            playerBoard = BOARD_OPPONENT_VALUE;
            opponentBoard = BOARD_PLAYER_VALUE;
        }

        return """
                {
                    "player": %s,
                    "opponent": %s
                }
                """.formatted(playerBoard, opponentBoard);
    }
}
