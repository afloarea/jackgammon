package com.github.afloarea.jackgammon.juliette.manager;

public class Player {

    private final String id;
    private final String name;
    private Game game;
    private Player opponent;

    public Player(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public GameToPlayersMessage executeMoveMessage(PlayerToGameMessage message) {
        return game.handle(id, opponent.id, message);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public Player getOpponent() {
        return opponent;
    }

    public void setOpponent(Player opponent) {
        this.opponent = opponent;
    }
}
