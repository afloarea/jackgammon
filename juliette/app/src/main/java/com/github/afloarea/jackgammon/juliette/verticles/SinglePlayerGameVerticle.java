package com.github.afloarea.jackgammon.juliette.verticles;

import com.github.afloarea.jackgammon.juliette.manager.*;
import com.github.afloarea.jackgammon.juliette.messages.client.PlayerJoinEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

public final class SinglePlayerGameVerticle extends AbstractGameVerticle {
    private static final Logger LOG = LoggerFactory.getLogger(SinglePlayerGameVerticle.class);

    private final Player player;
    private final Game game;

    public SinglePlayerGameVerticle(Player player, PlayerJoinEvent.Mode gameMode) {
        final var computerName = gameMode == PlayerJoinEvent.Mode.RANDOM ? "RandomComputer" : "NeuralComputer";
        this.player = player;
        final Player computer = new Player("computer-id", computerName);

        player.setOpponent(computer);
        computer.setOpponent(player);

        this.game = new SinglePlayerGame(
                player.getId(), computer.getId(), Map.of(player.getId(), player.getName(), computer.getId(), computerName));

        player.setGame(game);
        computer.setGame(game);
    }

    @Override
    protected Set<String> playerIds() {
        return Set.of(player.getId());
    }

    @Override
    public void start() {
        super.start();
        sendMessagesToPlayers(game.init());
    }

    @Override
    protected void handlePlayerMessage(String playerId, PlayerToGameEvent playerMessage) {
        final var messages = player.executeMoveMessage(playerMessage);
        sendMessagesToPlayers(messages);
        if (game.isOver()) {
            LOG.info("Game with players {} ended.", playerIds());
            sendDisconnect(playerIds());
            undeploySelf();
        }
    }

    @Override
    protected void handleDisconnect(String playerId) {
        LOG.warn("Player {} disconnected.", player.getName());
        undeploySelf();
    }

}
