package com.github.afloarea.jackgammon.juliette.manager;

import com.github.afloarea.jackgammon.juliette.Color;
import com.github.afloarea.jackgammon.juliette.DiceResult;
import com.github.afloarea.jackgammon.juliette.board.GameBoard;
import com.github.afloarea.jackgammon.juliette.message.NotifyMoveMessage;
import com.github.afloarea.jackgammon.juliette.message.client.PlayerRollMessage;
import com.github.afloarea.jackgammon.juliette.message.client.SelectMoveMessage;
import com.github.afloarea.jackgammon.juliette.message.server.InitGameMessage;
import com.github.afloarea.jackgammon.juliette.message.server.NotifyRollMessage;
import com.github.afloarea.jackgammon.juliette.message.server.PromptMoveMessage;
import com.github.afloarea.jackgammon.juliette.message.server.PromptRollMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class DefaultGame implements Game {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultGame.class);

    private final Map<Color, String> playersByPlayingColor = new EnumMap<>(Color.class);
    private final GameBoard board;

    public DefaultGame(String firstPlayerId, String secondPlayerId) {
        final var firstPlayerColor = Color.getRandom();
        playersByPlayingColor.put(firstPlayerColor, firstPlayerId);
        playersByPlayingColor.put(firstPlayerColor.complement(), secondPlayerId);
        board = GameBoard.buildNewBoard();
    }

    @Override
    public GameToPlayersMessage handle(PlayerToGameMessage message) {
        if (message instanceof PlayerRollMessage) {
            return handleRoll((PlayerRollMessage) message);
        }
        if (message instanceof SelectMoveMessage) {
            return handleMove((SelectMoveMessage) message);
        }
        throw new UnsupportedOperationException("Unable to handle message of type " + message.getClass().getSimpleName());
    }

    @Override
    public GameToPlayersMessage init() {
        return GameToPlayersMessage.of(
                getPlayerId(Color.BLACK), List.of(new InitGameMessage(Color.BLACK), new PromptRollMessage()),
                getPlayerId(Color.WHITE), List.of(new InitGameMessage(Color.WHITE)));
    }

    private String getPlayerId(Color playingColor) {
        return playersByPlayingColor.get(playingColor);
    }

    private GameToPlayersMessage handleRoll(PlayerRollMessage rollMessage) {
        final var playingColor = rollMessage.getPlayingColor();
        final var diceResult = DiceResult.generate();
        final var notification = new NotifyRollMessage(playingColor, diceResult);

        board.updateDiceForPlayingColor(playingColor, diceResult);

        return GameToPlayersMessage.of(
                getPlayerId(playingColor), generateCurrentPlayerMessages(notification),
                getPlayerId(playingColor.complement()), generateOpponentMessages(notification));
    }

    private GameToPlayersMessage handleMove(SelectMoveMessage selectMoveMessage) {
        final var notifyMove = NotifyMoveMessage.from(selectMoveMessage);
        final var playingColor = notifyMove.getPlayingColor();
        board.executeMoveForPlayingColor(playingColor, notifyMove.getMove());

        return GameToPlayersMessage.of(
                getPlayerId(playingColor), generateCurrentPlayerMessages(notifyMove),
                getPlayerId(playingColor.complement()), generateOpponentMessages(notifyMove));
    }

    private List<GameToPlayerMessage> generateOpponentMessages(GameToPlayerMessage notification) {
        final var opponentMessages = new ArrayList<GameToPlayerMessage>();
        opponentMessages.add(notification);
        if (board.currentPlayingColorFinishedTurn()) {
            opponentMessages.add(new PromptRollMessage());
        }
        return Collections.unmodifiableList(opponentMessages);
    }

    private List<GameToPlayerMessage> generateCurrentPlayerMessages(GameToPlayerMessage notification) {
        return List.of(notification, new PromptMoveMessage(board.getPossibleMovesForCurrentPlayingColor()));
    }

    @Override
    public Collection<String> getPlayersIds() {
        return playersByPlayingColor.values();
    }
}
