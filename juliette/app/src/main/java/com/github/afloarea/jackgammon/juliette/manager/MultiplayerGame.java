package com.github.afloarea.jackgammon.juliette.manager;

import com.github.afloarea.jackgammon.juliette.GameMove;
import com.github.afloarea.jackgammon.juliette.messages.client.PlayerRollMessage;
import com.github.afloarea.jackgammon.juliette.messages.client.SelectMoveMessage;
import com.github.afloarea.jackgammon.juliette.messages.server.*;
import com.github.afloarea.obge.Direction;
import com.github.afloarea.obge.InteractiveObgEngine;
import com.github.afloarea.obge.dice.DiceRoll;
import com.github.afloarea.obge.factory.BoardTemplate;
import com.github.afloarea.obge.factory.ObgEngines;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public final class MultiplayerGame implements Game {
    private static final Logger LOG = LoggerFactory.getLogger(MultiplayerGame.class);

    private final Map<String, Direction> directionByPlayerId;
    private final InteractiveObgEngine engine;
    private final PlayerInfo firstPlayer;
    private final PlayerInfo secondPlayer;

    MultiplayerGame(String firstPlayerId, String secondPlayerId, Map<String, String> playerNamesById) {
        this.firstPlayer = new PlayerInfo(firstPlayerId, playerNamesById.get(firstPlayerId), Direction.CLOCKWISE);
        this.secondPlayer = new PlayerInfo(secondPlayerId, playerNamesById.get(secondPlayerId),
                Direction.ANTICLOCKWISE);
        this.directionByPlayerId = Map.of(firstPlayerId, Direction.CLOCKWISE, secondPlayerId, Direction.ANTICLOCKWISE);

        engine = ObgEngines.create(InteractiveObgEngine.class, BoardTemplate.getDefault());
    }

    @Override
    public boolean isOver() {
        return engine.isGameComplete();
    }

    @Override
    public GameToPlayersMessage handle(String playerId, String opponentId, PlayerToGameMessage message) {
        if (message instanceof PlayerRollMessage rollMessage) {
            return handleRoll(playerId, opponentId, rollMessage);
        }
        if (message instanceof SelectMoveMessage selectMessage) {
            return handleMove(playerId, opponentId, selectMessage);
        }
        throw new UnsupportedOperationException("Unable to handle message of type " + message.getClass().getSimpleName());
    }

    @Override
    public GameToPlayersMessage init() {
        return GameToPlayersMessage.of(
                firstPlayer.id(), List.of(new InitGameMessage(
                        firstPlayer.name(), secondPlayer.name(), true), new PromptRollMessage()),
                secondPlayer.id(), List.of(new InitGameMessage(
                        secondPlayer.name(), firstPlayer.name(), false)));
    }

    private GameToPlayersMessage handleRoll(String playerId, String opponentId, PlayerRollMessage rollMessage) {
        final var playerDirection = directionByPlayerId.get(playerId);
        final var diceResult = DiceRoll.generate();
        final var notification = new NotifyRollMessage(
                firstPlayer.id().equals(playerId) ? firstPlayer.name() : secondPlayer.name(),
                diceResult);

        engine.applyDiceRoll(playerDirection, diceResult);

        return GameToPlayersMessage.of(
                playerId, generateCurrentPlayerMessages(notification),
                opponentId, generateOpponentMessages(notification));
    }

    private GameToPlayersMessage handleMove(String playerId, String opponentId, SelectMoveMessage selectMoveMessage) {
        final var move = selectMoveMessage.selectedMove();
        final var playerDirection = directionByPlayerId.get(playerId);
        final List<GameToPlayerMessage> playerMessages =
                engine.execute(playerDirection, move.from(), move.to()).stream()
                        .<GameMove>mapMulti((transition, consumer) -> {
                            if (transition.isSuspending()) {
                                consumer.accept(GameMove.fromSuspendedPart(transition));
                            }
                            consumer.accept(GameMove.fromSimplePart(transition));
                        })
                        .map(NotifyMoveMessage::new)
                        .collect(Collectors.toCollection(ArrayList::new));
        final List<GameToPlayerMessage> opponentMessages = new ArrayList<>(playerMessages);

        if (engine.isGameComplete()) {
            final var endMessage = new NotifyGameEndedMessage(
                    firstPlayer.id().equals(playerId) ? firstPlayer.name() : secondPlayer.name());
            playerMessages.add(endMessage);
            opponentMessages.add(endMessage);
        } else {
            playerMessages.add(new PromptMoveMessage(engine.getPossibleMoves()));
            if (engine.isCurrentTurnDone()) {
                opponentMessages.add(new PromptRollMessage());
            }
        }

        return GameToPlayersMessage.of(
                playerId, Collections.unmodifiableList(playerMessages),
                opponentId, Collections.unmodifiableList(opponentMessages));
    }

    private List<GameToPlayerMessage> generateOpponentMessages(GameToPlayerMessage notification) {
        final var opponentMessages = new ArrayList<GameToPlayerMessage>();
        opponentMessages.add(notification);
        if (engine.isCurrentTurnDone()) {
            opponentMessages.add(new PromptRollMessage());
        }
        return Collections.unmodifiableList(opponentMessages);
    }

    private List<GameToPlayerMessage> generateCurrentPlayerMessages(GameToPlayerMessage notification) {
        return List.of(notification, new PromptMoveMessage(engine.getPossibleMoves()));
    }

    private record PlayerInfo(String id, String name, Direction direction) {
    }
}
