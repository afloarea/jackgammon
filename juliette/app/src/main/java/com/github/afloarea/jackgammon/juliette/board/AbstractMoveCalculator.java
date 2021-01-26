package com.github.afloarea.jackgammon.juliette.board;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public abstract class AbstractMoveCalculator implements MoveCalculator {

    private final ColumnSupplier columnSupplier;

    protected AbstractMoveCalculator(ColumnSupplier columnSupplier) {
        this.columnSupplier = columnSupplier;
    }

    @Override
    public final Stream<Move> computeMovesFromStart(int startIndex, List<Integer> availableHops, Direction direction) {
        reset();
        if (availableHops.isEmpty()) {
            return Stream.empty();
        }
        final var usedHops = new ArrayList<Integer>();
        final var moves = new ArrayList<Move>();

        int index = startIndex;
        for (int hop : availableHops) {
            if (!canHop(index, hop, direction)) {
                return moves.stream();
            }
            usedHops.add(hop);
            final int newIndex = computeNewIndex(index, hop, direction);
            moves.add(new Move(
                    columnSupplier.getColumn(startIndex, direction),
                    columnSupplier.getColumn(newIndex, direction),
                    new ArrayList<>(usedHops)));
            index = newIndex;
        }

        return moves.stream();
    }

    protected void reset() {}

    protected int computeNewIndex(int start, int hop, Direction direction) {
        return start + hop * direction.getSign();
    }

    protected abstract boolean canHop(int start, int hop, Direction direction);

}
