package com.github.afloarea.jackgammon.juliette.manager;

import com.github.afloarea.jackgammon.juliette.DiceResult;
import com.github.afloarea.jackgammon.juliette.board.Direction;
import com.github.afloarea.jackgammon.juliette.board.GameBoard;
import com.github.afloarea.jackgammon.juliette.messages.client.PlayerRollMessage;
import com.github.afloarea.jackgammon.juliette.messages.client.SelectMoveMessage;
import com.github.afloarea.jackgammon.juliette.messages.server.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.github.afloarea.jackgammon.juliette.board.Constants.COLORS_BY_DIRECTION;

public class DefaultGame implements Game {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultGame.class);

    private final Map<String, Direction> directionByPlayerId;
    private final GameBoard board;
    private final Map<String, String> playerNamesById;

    DefaultGame(String firstPlayerId, String secondPlayerId, Map<String, String> playerNamesById) {
        this.playerNamesById = playerNamesById;
        final var firstPlayerDirection = Direction.getRandom();
        directionByPlayerId = Map.of(
                firstPlayerId, firstPlayerDirection,
                secondPlayerId, firstPlayerDirection.reverse());
        board = GameBoard.buildNewBoard();
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
        final List<String> playerIds = new ArrayList<>(playerNamesById.keySet());
        final String firstPlayerId;
        final String secondPlayerId;
        if (directionByPlayerId.get(playerIds.get(0)) == Direction.FORWARD) {
            firstPlayerId = playerIds.get(0);
            secondPlayerId = playerIds.get(1);
        } else {
            firstPlayerId = playerIds.get(1);
            secondPlayerId = playerIds.get(0);
        }

        return GameToPlayersMessage.of(
                firstPlayerId, List.of(
                        InitGameMessage.buildFirstPlayerMessage(playerNamesById.get(firstPlayerId), playerNamesById.get(secondPlayerId)),
                        new PromptRollMessage()),
                secondPlayerId,List.of(
                        InitGameMessage.buildSecondPlayerMessage(playerNamesById.get(secondPlayerId), playerNamesById.get(firstPlayerId))));
    }

    private GameToPlayersMessage handleRoll(String playerId, String opponentId, PlayerRollMessage rollMessage) {
        final var playerDirection = directionByPlayerId.get(playerId);
        final var diceResult = DiceResult.generate();
        final var notification = new NotifyRollMessage(COLORS_BY_DIRECTION.get(playerDirection), diceResult);

        board.updateDiceForDirection(playerDirection, diceResult);

        return GameToPlayersMessage.of(
                playerId, generateCurrentPlayerMessages(notification),
                opponentId, generateOpponentMessages(notification));
    }

    private GameToPlayersMessage handleMove(String playerId, String opponentId, SelectMoveMessage selectMoveMessage) {
        final var move = selectMoveMessage.getSelectedMove();
        final var playerDirection = directionByPlayerId.get(playerId);
        final List<GameToPlayerMessage> playerMessages = board.executeMoveInDirection(playerDirection, move).stream()
                .map(executedMoved -> NotifyMoveMessage.of(COLORS_BY_DIRECTION.get(playerDirection), executedMoved))
                .collect(Collectors.toCollection(ArrayList::new));
        final List<GameToPlayerMessage> opponentMessages = new ArrayList<>(playerMessages);

        if (board.isGameComplete()) {
            final var endMessage = new NotifyGameEndedMessage(COLORS_BY_DIRECTION.get(board.getWinningDirection()));
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
}
