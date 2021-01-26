package com.github.afloarea.jackgammon.juliette.board;

import com.github.afloarea.jackgammon.juliette.Color;
import com.github.afloarea.jackgammon.juliette.DiceResult;
import com.github.afloarea.jackgammon.juliette.GameMove;
import com.github.afloarea.jackgammon.juliette.IllegalGameActionException;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AdvancedGameBoard implements GameBoard {
    private static final Map<Color, Direction> DIRECTIONS_BY_COLOR = Map.of(
            Color.BLACK, Direction.FORWARD,
            Color.WHITE, Direction.BACKWARD,
            Color.NONE, Direction.NONE);

    private final Map<GameMove, Move> movesMap = new HashMap<>();
    private Color currentPlayingColor = Color.NONE;

    private final List<Integer> remainingDiceValues = new ArrayList<>();
    private DiceResult currentDiceResult;

    private TurnLogic turnLogic;

    @Override
    public void updateDiceForPlayingColor(Color playingColor, DiceResult dice) {
        performRollValidation(playingColor, dice);

        currentPlayingColor = playingColor;
        currentDiceResult = dice;
        dice.stream().forEach(remainingDiceValues::add);

        updatePossibleMoves();
    }

    private void performRollValidation(Color playingColor, DiceResult dice) {
        if (isGameComplete()) {
            throw new IllegalGameActionException("Unable to roll dice. Game is finished");
        }
        if (playingColor == Color.NONE) {
            throw new IllegalGameActionException("None color cannot update the dice");
        }
        if (!remainingDiceValues.isEmpty()) {
            throw new IllegalGameActionException("Cannot update dice. Turn is not yet over");
        }
        if (playingColor != currentPlayingColor.complement() && currentPlayingColor != Color.NONE) {
            throw new IllegalGameActionException("Wrong player color rolled dice.");
        }
    }

    private void updatePossibleMoves() {
        final var computedMoves =
                turnLogic.streamPossibleMoves(remainingDiceValues, DIRECTIONS_BY_COLOR.get(currentPlayingColor))
                        .collect(Collectors.toMap(
                                move -> new GameMove(move.getSource().getId(), move.getTarget().getId()),
                                Function.identity()));

        movesMap.clear();
        movesMap.putAll(computedMoves);

        if (movesMap.isEmpty()) {
            remainingDiceValues.clear();
        }
    }

    @Override
    public boolean currentPlayingColorFinishedTurn() {
        return remainingDiceValues.isEmpty();
    }

    @Override
    public Set<GameMove> getPossibleMovesForCurrentPlayingColor() {
        return Set.copyOf(movesMap.keySet());
    }


}
