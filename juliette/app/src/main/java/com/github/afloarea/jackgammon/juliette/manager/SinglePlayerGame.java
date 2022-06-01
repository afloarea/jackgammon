package com.github.afloarea.jackgammon.juliette.manager;

import com.github.afloarea.jackgammon.juliette.GameMove;
import com.github.afloarea.jackgammon.juliette.messages.client.PlayerRollMessage;
import com.github.afloarea.jackgammon.juliette.messages.client.SelectMoveMessage;
import com.github.afloarea.jackgammon.juliette.messages.server.*;
import com.github.afloarea.jackgammon.juliette.neural.MoveSequenceConverter;
import com.github.afloarea.jackgammon.juliette.neural.NeuralNetwork;
import com.github.afloarea.jackgammon.juliette.neural.TdMapper;
import com.github.afloarea.jackgammon.juliette.neural.TdNetwork;
import com.github.afloarea.obge.Direction;
import com.github.afloarea.obge.MixedModeObgEngine;
import com.github.afloarea.obge.board.ObgBoard;
import com.github.afloarea.obge.dice.DiceRoll;
import com.github.afloarea.obge.factory.BoardTemplate;
import com.github.afloarea.obge.factory.ObgEngines;
import com.github.afloarea.obge.moves.ObgMove;
import com.github.afloarea.obge.moves.ObgTransition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public final class SinglePlayerGame implements Game {
    private static final Logger LOG = LoggerFactory.getLogger(SinglePlayerGame.class);
    private static final Random RANDOM = new Random();
    private static final NeuralBrain NEURAL_BRAIN = new NeuralBrain(TdNetwork.importResource("/agents/neural1.json"));

    private final MixedModeObgEngine engine;

    private final PlayerInfo humanPlayer;
    private final PlayerInfo computerPlayer;

    private final List<ObgMove> playerTurnMoves = new ArrayList<>();
    private final ObgBoard computerBoard;

    public SinglePlayerGame(String humanPlayerId, String computerPlayerId, Map<String, String> playerNamesById) {
        this.humanPlayer = new PlayerInfo(humanPlayerId, playerNamesById.get(humanPlayerId), Direction.CLOCKWISE);
        this.computerPlayer = new PlayerInfo(computerPlayerId, playerNamesById.get(computerPlayerId), Direction.ANTICLOCKWISE);

        engine = ObgEngines.newMixed(BoardTemplate.getDefault());
        computerBoard = ObgBoard.createStartingBoard(BoardTemplate.getDefault());
    }

    @Override
    public boolean isOver() {
        return engine.isGameComplete();
    }

    @Override
    public GameToPlayersMessage handle(String playerId, String opponentId, PlayerToGameMessage message) {
        if (message instanceof PlayerRollMessage) {
            return handleRoll();
        }
        if (message instanceof SelectMoveMessage selectMoveMessage) {
            return handleMove(selectMoveMessage);
        }
        throw new UnsupportedOperationException("Unable to handle message of type " + message.getClass().getSimpleName());
    }

    @Override
    public GameToPlayersMessage init() {
        return GameToPlayersMessage.of(
                humanPlayer.id(), List.of(new InitGameMessage(
                        humanPlayer.name(), computerPlayer.name(), true), new PromptRollMessage()),
                computerPlayer.id(), List.of(new InitGameMessage(
                        computerPlayer.name(), humanPlayer.name(), false)));
    }

    private GameToPlayersMessage handleRoll() {
        final var playerDirection = humanPlayer.direction();
        final var diceResult = DiceRoll.generate();

        engine.applyDiceRoll(playerDirection, diceResult);

        final var notification = new NotifyRollMessage(humanPlayer.name(), diceResult);
        final var playerMessages = new ArrayList<GameToPlayerMessage>();
        playerMessages.add(notification);

        if (engine.isCurrentTurnDone()) {
            playerMessages.addAll(playComputerTurn());
        } else {
            playerMessages.add(new PromptMoveMessage(engine.getPossibleMoves()));
        }

        return generatePlayerMessage(playerMessages);
    }

    private Collection<GameToPlayerMessage> playComputerTurn() {
        LOG.warn("PLAYER MOVES SO FAR: {}", playerTurnMoves);

        // update board with player moves
        final var playerSequence = MoveSequenceConverter.convertMovesToSequence(humanPlayer.direction(), playerTurnMoves);
        LOG.warn("PLAYER SEQUENCE: {}", playerSequence);
        computerBoard.doSequence(humanPlayer.direction(), playerSequence);
        playerTurnMoves.clear();

        final var playerMessages = new ArrayList<GameToPlayerMessage>();

        // roll the dice
        final var diceRoll = DiceRoll.generate();
        playerMessages.add(new NotifyRollMessage(computerPlayer.name(), diceRoll));
        engine.applyDiceRoll(computerPlayer.direction(), diceRoll);

        // no possible moves
        if (engine.isCurrentTurnDone()) {
            playerMessages.add(new PromptRollMessage());
            return playerMessages;
        }

        final var possibleSequences = engine.getPossibleSequences();
        final var selectedSequence = engine.selectSequence(computerPlayer.direction(), selectSequence(possibleSequences));
        computerBoard.doSequence(computerPlayer.direction(), selectedSequence);
        final var executedMoves = MoveSequenceConverter.convertSequenceToMoves(computerPlayer.direction(), selectedSequence);

        executedMoves.stream()
                .map(NotifyMoveMessage::new)
                .forEach(playerMessages::add);

        if (engine.isGameComplete()) {
            playerMessages.add(new NotifyGameEndedMessage(computerPlayer.name()));
        } else {
            playerMessages.add(new PromptRollMessage());
        }

        return playerMessages;
    }

    private List<ObgTransition> selectSequence(Set<List<ObgTransition>> sequences) {
        if (computerPlayer.name.equals("RandomComputer")) {
            return new ArrayList<>(sequences).get(RANDOM.nextInt(sequences.size()));
        }

        return NEURAL_BRAIN.selectSequence(computerPlayer.direction(), sequences, computerBoard);
    }

    private GameToPlayersMessage handleMove(SelectMoveMessage selectMoveMessage) {
        final var move = selectMoveMessage.selectedMove();
        final var executedMoves = engine.execute(humanPlayer.direction(), move.from(), move.to());

        playerTurnMoves.addAll(executedMoves);

        final List<GameToPlayerMessage> playerMessages = executedMoves.stream()
                .map(GameMove::fromBgMove)
                .map(NotifyMoveMessage::new)
                .collect(Collectors.toCollection(ArrayList::new));

        if (engine.isGameComplete()) {
            final var endMessage = new NotifyGameEndedMessage(humanPlayer.name());
            playerMessages.add(endMessage);
        } else {
            playerMessages.add(new PromptMoveMessage(engine.getPossibleMoves()));
            if (engine.isCurrentTurnDone()) {
                playerMessages.addAll(playComputerTurn());
            }
        }

        return generatePlayerMessage(playerMessages);
    }

    private GameToPlayersMessage generatePlayerMessage(Collection<GameToPlayerMessage> playerMessages) {
        return GameToPlayersMessage.of(humanPlayer.id(), playerMessages, computerPlayer.id(), Collections.emptyList());
    }

    private record PlayerInfo(String id, String name, Direction direction) {
    }

    private static final class NeuralBrain {
        private final NeuralNetwork network;

        public NeuralBrain(NeuralNetwork network) {
            this.network = network;
        }

        public List<ObgTransition> selectSequence(Direction direction, Set<List<ObgTransition>> sequences, ObgBoard board) {
            List<ObgTransition> selectedTransition = null;
            var selectedScore = -1D;
            for (var sequence : sequences) {
                final var inputs = TdMapper.mapToNeuralInputs(direction, board);
                final var score = network.compute(inputs);
                if (score > selectedScore) {
                    selectedScore = score;
                    selectedTransition = sequence;
                }
            }
            return selectedTransition;
        }
    }
}
