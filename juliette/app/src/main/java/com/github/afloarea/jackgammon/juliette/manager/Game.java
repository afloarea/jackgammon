package com.github.afloarea.jackgammon.juliette.manager;

import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Handles interaction for players that are ready to play.
 */
public interface Game {

    GameToPlayersMessage handle(String playerId, String opponentId, PlayerToGameMessage message);

    GameToPlayersMessage init();

    boolean isOver();

    static Game setUpGame(Player player1, Player player2) {
        player1.setOpponent(player2);
        player2.setOpponent(player1);

        final var playerNameById = Stream.of(player1, player2).collect(Collectors.toMap(Player::getId, Player::getName));

        final var game = new MultiplayerGame(player1.getId(), player2.getId(), playerNameById);
        player1.setGame(game);
        player2.setGame(game);
        return game;
    }
}
