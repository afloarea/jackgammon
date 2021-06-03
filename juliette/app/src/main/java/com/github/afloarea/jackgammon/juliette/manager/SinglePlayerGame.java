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

import java.util.*;
import java.util.stream.Collectors;

public class SinglePlayerGame implements Game {
    private static final Random RANDOM = new Random();
    private static final NeuralBrain NEURAL_BRAIN = new NeuralBrain(TdNetwork.importResource("/agents/agent3.json"));

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
        if (message instanceof SelectMoveMessage) {
            return handleMove((SelectMoveMessage) message);
        }
        throw new UnsupportedOperationException("Unable to handle message of type " + message.getClass().getSimpleName());
    }

    @Override
    public GameToPlayersMessage init() {
        return GameToPlayersMessage.of(
                humanPlayer.getId(), List.of(new InitGameMessage(
                        humanPlayer.getName(), computerPlayer.getName(), true), new PromptRollMessage()),
                computerPlayer.getId(), List.of(new InitGameMessage(
                        computerPlayer.getName(), humanPlayer.getName(), false)));
    }

    private GameToPlayersMessage handleRoll() {
        final var playerDirection = humanPlayer.direction;
        final var diceResult = DiceRoll.generate();

        engine.applyDiceRoll(playerDirection, diceResult);

        final var notification = new NotifyRollMessage(humanPlayer.getName(), diceResult);
        final var playerMessages = new ArrayList<GameToPlayerMessage>();
        playerMessages.add(notification);

        if (engine.isCurrentTurnDone()) {
            playerMessages.addAll(playComputerTurn());
        }

        return generatePlayerMessage(playerMessages);
    }

    private Collection<GameToPlayerMessage> playComputerTurn() {
        // update board with player moves
        final var playerSequence = MoveSequenceConverter.convertMovesToSequence(humanPlayer.direction, playerTurnMoves);
        computerBoard.doSequence(humanPlayer.direction, playerSequence);
        playerTurnMoves.clear();

        final var playerMessages = new ArrayList<GameToPlayerMessage>();

        // roll the dice
        final var diceRoll = DiceRoll.generate();
        playerMessages.add(new NotifyRollMessage(computerPlayer.name, diceRoll));
        engine.applyDiceRoll(computerPlayer.direction, diceRoll);

        // no possible moves
        final var possibleSequences = engine.getPossibleSequences();
        if (possibleSequences.isEmpty()) {
            playerMessages.add(new PromptRollMessage());
            return playerMessages;
        }

        final var selectedSequence = engine.selectSequence(computerPlayer.direction, selectSequence(possibleSequences));
        final var executedMoves = MoveSequenceConverter.convertSequenceToMoves(computerPlayer.direction, selectedSequence);

        executedMoves.stream()
                .map(NotifyMoveMessage::new)
                .forEach(playerMessages::add);

        if (engine.isGameComplete()) {
            playerMessages.add(new NotifyGameEndedMessage(computerPlayer.getName()));
        } else {
            playerMessages.add(new PromptRollMessage());
        }

        return playerMessages;
    }

    private List<ObgTransition> selectSequence(Set<List<ObgTransition>> sequences) {
        if (computerPlayer.name.equals("RandomComputer")) {
            return new ArrayList<>(sequences).get(RANDOM.nextInt(sequences.size()));
        }

        return NEURAL_BRAIN.selectSequence(computerPlayer.direction, sequences, computerBoard);
    }

    private GameToPlayersMessage handleMove(SelectMoveMessage selectMoveMessage) {
        final var move = selectMoveMessage.getSelectedMove();
        final var playerDirection = humanPlayer.direction;
        final var executedMoves = engine.execute(humanPlayer.direction, move.getFrom(), move.getTo());

        playerTurnMoves.addAll(executedMoves);

        final List<GameToPlayerMessage> playerMessages =
                engine.execute(playerDirection, move.getFrom(), move.getTo()).stream()
                        .map(GameMove::fromBgMove)
                        .map(NotifyMoveMessage::new)
                        .collect(Collectors.toCollection(ArrayList::new));

        if (engine.isGameComplete()) {
            final var endMessage = new NotifyGameEndedMessage(humanPlayer.getName());
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
        return GameToPlayersMessage.of(humanPlayer.id, playerMessages, computerPlayer.id, Collections.emptyList());
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
