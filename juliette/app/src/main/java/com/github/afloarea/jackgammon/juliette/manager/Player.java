package com.github.afloarea.jackgammon.juliette.manager;

public class Player {

    private final String id;
    private final String name;
    private Game game;

    public Player(String id, String name) {
        this.id = id;
        this.name = name;
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

}
