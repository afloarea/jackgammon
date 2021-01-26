package com.github.afloarea.jackgammon.juliette.board;

import java.util.*;
import java.util.stream.Stream;

public abstract class AbstractMoveCalculator implements MoveCalculator {

    protected final ColumnSequence columnSequence;
    private final Map<Integer, Integer> usageByDiceValue = new HashMap<>();

    protected AbstractMoveCalculator(ColumnSequence columnSequence) {
        this.columnSequence = columnSequence;
    }

    @Override
    public void init() {
        usageByDiceValue.clear();
    }

    @Override
    public Stream<Move> computeMovesFromStart(int startIndex, List<Integer> availableHops, Direction direction) {
        if (availableHops.isEmpty()) {
            return Stream.empty();
        }
        final var usedHops = new ArrayList<Integer>();
        final var moves = new ArrayList<Move>();

        int index = startIndex;
        for (int hop : availableHops) {
            final int newIndex = index + hop * direction.getSign();
            if (!canMoveTo(newIndex, direction)) {
                return moves.stream();
            }

            usedHops.add(hop);
            moves.add(new Move(
                    columnSequence.getColumn(startIndex, direction),
                    columnSequence.getColumn(Math.min(25, newIndex), direction),
                    new ArrayList<>(usedHops)));
            index = newIndex;
            usageByDiceValue.compute(hop, (key, value) -> value == null ? 1 : value + 1);
        }

        return moves.stream();
    }

    protected abstract boolean canMoveTo(int to, Direction direction);

    @Override
    public final Map<Integer, Integer> getUsageByDiceValue() {
        return Map.copyOf(usageByDiceValue);
    }
}
