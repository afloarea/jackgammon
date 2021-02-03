package com.github.afloarea.jackgammon.juliette.manager;

import com.github.afloarea.jackgammon.juliette.DiceResult;
import com.github.afloarea.jackgammon.juliette.board.Direction;
import com.github.afloarea.jackgammon.juliette.board.GameBoard;
import com.github.afloarea.jackgammon.juliette.messages.client.PlayerRollMessage;
import com.github.afloarea.jackgammon.juliette.messages.client.SelectMoveMessage;
import com.github.afloarea.jackgammon.juliette.messages.server.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class DefaultGame implements Game {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultGame.class);

    private final Map<String, Direction> directionByPlayerId;
    private final GameBoard board;
    private final PlayerInfo firstPlayer;
    private final PlayerInfo secondPlayer;

    DefaultGame(String firstPlayerId, String secondPlayerId, Map<String, String> playerNamesById) {
        this.firstPlayer = new PlayerInfo(firstPlayerId, playerNamesById.get(firstPlayerId), Direction.FORWARD);
        this.secondPlayer = new PlayerInfo(secondPlayerId, playerNamesById.get(secondPlayerId), Direction.BACKWARD);
        this.directionByPlayerId = Map.of(firstPlayerId, Direction.FORWARD, secondPlayerId, Direction.BACKWARD);

        board = GameBoard.buildNewBoard();
    }

    @Override
    public boolean isOver() {
        return board.isGameComplete();
    }

    @Override
    public GameToPlayersMessage handle(String playerId, String opponentId, PlayerToGameMessage message) {
        if (message instanceof PlayerRollMessage) {
            return handleRoll(playerId, opponentId, (PlayerRollMessage) message);
        }
        if (message instanceof SelectMoveMessage) {
            return handleMove(playerId, opponentId, (SelectMoveMessage) message);
        }
        throw new UnsupportedOperationException("Unable to handle message of type " + message.getClass().getSimpleName());
    }

    @Override
    public GameToPlayersMessage init() {
        return GameToPlayersMessage.of(
                firstPlayer.getId(), List.of(new InitGameMessage(
                        firstPlayer.getName(), secondPlayer.getName(), true), new PromptRollMessage()),
                secondPlayer.getId(), List.of(new InitGameMessage(
                        firstPlayer.getName(), secondPlayer.getName(), false)));
    }

    private GameToPlayersMessage handleRoll(String playerId, String opponentId, PlayerRollMessage rollMessage) {
        final var playerDirection = directionByPlayerId.get(playerId);
        final var diceResult = DiceResult.generate();
        final var notification = new NotifyRollMessage(
                firstPlayer.getId().equals(playerId) ? firstPlayer.getName() : secondPlayer.getName(),
                diceResult);

        board.updateDiceForDirection(playerDirection, diceResult);

        return GameToPlayersMessage.of(
                playerId, generateCurrentPlayerMessages(notification),
                opponentId, generateOpponentMessages(notification));
    }

    private GameToPlayersMessage handleMove(String playerId, String opponentId, SelectMoveMessage selectMoveMessage) {
        final var move = selectMoveMessage.getSelectedMove();
        final var playerDirection = directionByPlayerId.get(playerId);
        final List<GameToPlayerMessage> playerMessages = board.executeMoveInDirection(playerDirection, move).stream()
                .map(NotifyMoveMessage::new)
                .collect(Collectors.toCollection(ArrayList::new));
        final List<GameToPlayerMessage> opponentMessages = new ArrayList<>(playerMessages);

        if (board.isGameComplete()) {
            final var endMessage = new NotifyGameEndedMessage(
                    firstPlayer.getId().equals(playerId) ? firstPlayer.getName() : secondPlayer.getName());
            playerMessages.add(endMessage);
            opponentMessages.add(endMessage);
        } else {
            playerMessages.add(new PromptMoveMessage(board.getCurrentDirectionPossibleMoves()));
            if (board.currentDirectionMovementIsComplete()) {
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
        if (board.currentDirectionMovementIsComplete()) {
            opponentMessages.add(new PromptRollMessage());
        }
        return Collections.unmodifiableList(opponentMessages);
    }

    private List<GameToPlayerMessage> generateCurrentPlayerMessages(GameToPlayerMessage notification) {
        return List.of(notification, new PromptMoveMessage(board.getCurrentDirectionPossibleMoves()));
    }

    private static final class PlayerInfo {
        private final String id;
        private final String name;
        private final Direction direction;

        public PlayerInfo(String id, String name, Direction direction) {
            this.id = id;
            this.name = name;
            this.direction = direction;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public Direction getDirection() {
            return direction;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PlayerInfo)) return false;
            PlayerInfo that = (PlayerInfo) o;
            return id.equals(that.id) && name.equals(that.name) && direction == that.direction;
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, name, direction);
        }
    }
}
