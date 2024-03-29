package com.github.afloarea.jackgammon.juliette.manager;

import com.github.afloarea.jackgammon.juliette.GameMove;
import com.github.afloarea.jackgammon.juliette.messages.client.PlayerRollEvent;
import com.github.afloarea.jackgammon.juliette.messages.client.SelectMoveEvent;
import com.github.afloarea.jackgammon.juliette.messages.server.*;
import com.github.afloarea.jackgammon.juliette.neural.NeuralNetwork;
import com.github.afloarea.jackgammon.juliette.neural.TdMapper;
import com.github.afloarea.jackgammon.juliette.neural.TdNetwork;
import com.github.afloarea.obge.Direction;
import com.github.afloarea.obge.MixedModeObgEngine;
import com.github.afloarea.obge.board.BoardSnapshot;
import com.github.afloarea.obge.dice.DiceRoll;
import com.github.afloarea.obge.factory.BoardTemplate;
import com.github.afloarea.obge.factory.ObgEngines;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.random.RandomGenerator;
import java.util.stream.Collectors;

public final class SinglePlayerGame implements Game {
    private static final Logger LOG = LoggerFactory.getLogger(SinglePlayerGame.class);
    private static final RandomGenerator RAND = RandomGenerator.of("Random");
    private static final NeuralBrain NEURAL_BRAIN = new NeuralBrain(TdNetwork.importResource("/agents/neural1.json"));

    private final MixedModeObgEngine engine;

    private final PlayerInfo humanPlayer;
    private final PlayerInfo computerPlayer;

    public SinglePlayerGame(String humanPlayerId, String computerPlayerId, Map<String, String> playerNamesById) {
        this.humanPlayer = new PlayerInfo(humanPlayerId, playerNamesById.get(humanPlayerId), Direction.CLOCKWISE);
        this.computerPlayer = new PlayerInfo(computerPlayerId, playerNamesById.get(computerPlayerId), Direction.ANTICLOCKWISE);

        this.engine = ObgEngines.create(MixedModeObgEngine.class, BoardTemplate.getDefault());
    }

    @Override
    public boolean isOver() {
        return engine.isGameComplete();
    }

    @Override
    public GameToPlayersMessages handle(String playerId, String opponentId, PlayerToGameEvent message) {
        if (message instanceof PlayerRollEvent) {
            return handleRoll();
        }
        if (message instanceof SelectMoveEvent selectMoveMessage) {
            return handleMove(selectMoveMessage);
        }
        throw new UnsupportedOperationException("Unable to handle message of type " + message.getClass().getSimpleName());
    }

    @Override
    public GameToPlayersMessages init() {
        return GameToPlayersMessages.of(
                humanPlayer.id(), List.of(new InitGameEvent(
                        humanPlayer.name(), computerPlayer.name(), true), new PromptRollEvent()),
                computerPlayer.id(), List.of(new InitGameEvent(
                        computerPlayer.name(), humanPlayer.name(), false)));
    }

    private GameToPlayersMessages handleRoll() {
        final var playerDirection = humanPlayer.direction();
        final var diceResult = DiceRoll.generate();

        engine.applyDiceRoll(playerDirection, diceResult);

        final var notification = new NotifyRollEvent(humanPlayer.name(), diceResult);
        final var playerMessages = new ArrayList<GameToPlayerEvent>();
        playerMessages.add(notification);

        if (engine.isCurrentTurnDone()) {
            playerMessages.addAll(playComputerTurn());
        } else {
            playerMessages.add(new PromptMoveEvent(engine.getPossibleMoves()));
        }

        return generatePlayerMessage(playerMessages);
    }

    private Collection<GameToPlayerEvent> playComputerTurn() {
        final var notifications = new ArrayList<GameToPlayerEvent>();

        // computer rolls the dice
        final var diceResult = DiceRoll.generate();
        notifications.add(new NotifyRollEvent(computerPlayer.name(), diceResult));
        engine.applyDiceRoll(computerPlayer.direction(), diceResult);

        if (engine.isCurrentTurnDone()) {
            // no possible moves with the roll result
            notifications.add(new PromptRollEvent());
            return notifications;
        }

        // computer selects an outcome
        final var boardOptions = engine.getBoardChoices();
        final var selectedBoard = selectBoard(boardOptions);

        // notify the player about the computers' selected outcome
        final var transitions = engine.transitionTo(computerPlayer.direction(), selectedBoard);
        transitions.stream()
                .<GameMove>mapMulti(((transition, consumer) -> {
                    if (transition.isSuspending()) {
                        consumer.accept(GameMove.fromSuspendedPart(transition));
                    }
                    consumer.accept(GameMove.fromSimplePart(transition));
                }))
                .map(NotifyMoveEvent::new)
                .forEach(notifications::add);

        // either finish the game or let the human player continue
        if (engine.isGameComplete()) {
            notifications.add(new NotifyGameEndedEvent(computerPlayer.name()));
        } else {
            notifications.add(new PromptRollEvent());
        }

        return Collections.unmodifiableList(notifications);
    }

    private BoardSnapshot selectBoard(Set<BoardSnapshot> possibleBoards) {
        if (computerPlayer.name().equals("RandomComputer")) {
            return possibleBoards.stream().skip(RAND.nextInt(possibleBoards.size())).findFirst().orElseThrow();
        }

        return NEURAL_BRAIN.selectBoard(computerPlayer.direction(), possibleBoards);
    }

    private GameToPlayersMessages handleMove(SelectMoveEvent selectMoveMessage) {
        final var move = selectMoveMessage.selectedMove();
        final var executedMoves = engine.execute(humanPlayer.direction(), move.from(), move.to());

        final List<GameToPlayerEvent> playerMessages = executedMoves.stream()
                .<GameMove>mapMulti((transition, consumer) -> {
                    if (transition.isSuspending()) {
                        consumer.accept(GameMove.fromSuspendedPart(transition));
                    }
                    consumer.accept(GameMove.fromSimplePart(transition));
                })
                .map(NotifyMoveEvent::new)
                .collect(Collectors.toCollection(ArrayList::new));

        if (engine.isGameComplete()) {
            playerMessages.add(new NotifyGameEndedEvent(humanPlayer.name()));
        } else {
            playerMessages.add(new PromptMoveEvent(engine.getPossibleMoves()));
            if (engine.isCurrentTurnDone()) {
                playerMessages.addAll(playComputerTurn());
            }
        }

        return generatePlayerMessage(playerMessages);
    }

    private GameToPlayersMessages generatePlayerMessage(Collection<GameToPlayerEvent> playerMessages) {
        return GameToPlayersMessages.of(humanPlayer.id(), playerMessages, computerPlayer.id(), Collections.emptyList());
    }

    private record PlayerInfo(String id, String name, Direction direction) {
    }

    private static final class NeuralBrain {
        private final NeuralNetwork network;

        public NeuralBrain(NeuralNetwork network) {
            this.network = network;
        }

        public BoardSnapshot selectBoard(Direction direction, Set<BoardSnapshot> possibleBoards) {
            BoardSnapshot selectedBoard = null;
            double selectedScore = -1D;
            for (var board : possibleBoards) {
                final double[] inputs = TdMapper.mapToNeuralInputs(direction, board);
                final double score = network.compute(inputs);
                if (score > selectedScore) {
                    selectedScore = score;
                    selectedBoard = board;
                }
            }

            return selectedBoard;
        }
    }
}
