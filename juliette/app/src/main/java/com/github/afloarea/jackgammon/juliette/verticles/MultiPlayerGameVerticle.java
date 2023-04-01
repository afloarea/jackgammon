package com.github.afloarea.jackgammon.juliette.verticles;

import com.github.afloarea.jackgammon.juliette.manager.Game;
import com.github.afloarea.jackgammon.juliette.manager.GameToPlayersMessages;
import com.github.afloarea.jackgammon.juliette.manager.Player;
import com.github.afloarea.jackgammon.juliette.manager.PlayerToGameEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class MultiPlayerGameVerticle extends AbstractGameVerticle {
    private static final Logger LOG = LoggerFactory.getLogger(MultiPlayerGameVerticle.class);

    private final Map<String, Player> playersById;
    private final Game game;

    public MultiPlayerGameVerticle(Player firstPlayer, Player secondPlayer) {
        this.playersById = Stream.of(firstPlayer, secondPlayer)
                .collect(Collectors.toMap(Player::getId, Function.identity()));
        this.game = Game.setUpGame(firstPlayer, secondPlayer);
    }

    @Override
    protected Set<String> playerIds() {
        return playersById.keySet();
    }

    @Override
    public void start() {
        super.start();
        sendMessagesToPlayers(game.init());
    }

    @Override
    protected void handlePlayerMessage(String playerId, PlayerToGameEvent msg) {
        final GameToPlayersMessages result = playersById.get(playerId).executeMoveMessage(msg);
        sendMessagesToPlayers(result);
        if (game.isOver()) {
            LOG.info("Game with players {} ended.", playerIds());
            sendDisconnect(playerIds());
            undeploySelf();
        }
    }

    @Override
    protected void handleDisconnect(String playerId) {
        final Player disconnectedPlayer = playersById.get(playerId);
        LOG.warn("Player {} disconnected.", disconnectedPlayer.getName());
        sendDisconnect(Set.of(disconnectedPlayer.getOpponent().getId()));
        undeploySelf();
    }
}
