package com.github.afloarea.jackgammon.juliette.board;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GameTable {

    // columns[0] = suspended for FORWARD direction
    // columns[25] = suspended for BACKWARD direction
    private final List<BoardColumn> columns = new ArrayList<>();
    private final Map<Direction, BoardColumn> collectColumnsByDirection = new HashMap<>();
    private final Map<String, Integer> columnPositionById = new HashMap<>();



    private Stream<Move> computePossibleMoves(List<Integer> dice, Direction direction) {
        // FILTERING
        // filter columns, starting with the suspended column, so that only those that can move in the given direction are kept

        // ENTER
        // if after filtering, the suspended column is kept and cannot advance then return an empty result
        // if the suspended column can advance, but there are still elements after moving, then return the single result


        // calculate how many elements are in the home area + collected
        // if all pieces are there, then simply compute collect type moves

        // for each column, try to advance as much as possible
        // if all but one are there, then attempt to collect the piece after advancing as much as possible

        //------------------------------------

        final var reversed = new ArrayList<Integer>();
        if (dice.size() == 2 && !dice.get(0).equals(dice.get(1))) {
            reversed.addAll(dice);
            Collections.reverse(reversed);
        }

        final var availableColumns = columns.stream()
                .filter(column -> direction == column.getMovingDirectionOfElements())
                .collect(Collectors.toList());

        final var firstColumn = availableColumns.get(0);
        if (isSuspendColumn(firstColumn, direction)) {
            final var sourceStream = firstColumn.getPieceCount() > 1
                    ? dice.stream().distinct().map(Collections::singletonList)
                    : Stream.of(dice, reversed);
            return sourceStream.flatMap(hops -> computeMovesFromStart(firstColumn, hops, direction));
        }

        final int uncollectablePieces = computeUncollectablePieces(direction);
        if (uncollectablePieces > 1) { // there can be no single piece collected
            return Stream.of(dice, reversed)
                    .flatMap(hops -> availableColumns.stream()
                            .flatMap(column -> computeMovesFromStart(column, hops, direction)));
        }

        //TODO: generate moves that could end with collect

    }

    private int computeUncollectablePieces(Direction direction) {
        final List<BoardColumn> homeColumns = direction == Direction.FORWARD
                ? columns.subList(19, 25)
                : columns.subList(1, 7);

        final int homeColumnPieces = homeColumns.stream().mapToInt(BoardColumn::getPieceCount).sum();

        return 15 - homeColumnPieces + collectColumnsByDirection.get(direction).getPieceCount();
    }

    private boolean isSuspendColumn(BoardColumn column, Direction direction) {
        return columnPositionById.get(column.getId()) == (direction == Direction.FORWARD ? 0 : 25);
    }

    private Stream<Move> computeMovesFromStart(BoardColumn start, List<Integer> availableHops, Direction direction) {
        if (availableHops.isEmpty()) {
            return Stream.empty();
        }
        final var usedHops = new ArrayList<Integer>();
        final var moves = new ArrayList<Move>();

        int index = columnPositionById.get(start.getId());
        for (int hop : availableHops) {
            if (!canHop(index, hop, direction)) {
                return moves.stream();
            }
            usedHops.add(hop);
            final int newIndex = computeNewIndex(index, hop, direction);
            moves.add(new Move(columns.get(index), columns.get(newIndex), new ArrayList<>(usedHops)));
            index = newIndex;
        }

        return moves.stream();
    }

    private int computeNewIndex(int from, int distance, Direction direction) {
        return from + distance * direction.getSign();
    }

    private boolean canHop(int from, int distance, Direction direction) {
        final int target = computeNewIndex(from, distance, direction);
        return !isOutOfBounds(target) && !columns.get(target).isBlockedForDirection(direction);
    }

    private boolean isOutOfBounds(int index) {
        return index < 1 || index > 24;
    }

    private interface Hoppable {

        default void init() {}

        boolean canHop(int start, int hop, Direction direction);

        int computeNewIndex(int start, int hop, Direction direction);

    }

    private final Hoppable simpleHop = new Hoppable() {
        @Override
        public boolean canHop(int start, int hop, Direction direction) {
            final int target = computeNewIndex(start, hop, direction);
            return !isOutOfBounds(target) && !columns.get(target).isBlockedForDirection(direction);
        }

        @Override
        public int computeNewIndex(int start, int hop, Direction direction) {
            return start + hop * direction.getSign();
        }

        private boolean isOutOfBounds(int index) {
            return  index < 1 || index > 24;
        }
    };

    private final Hoppable permissiveCollect = new Hoppable() {
        private boolean hopped = false;

        @Override
        public void init() {
            hopped = false;
        }

        @Override
        public boolean canHop(int start, int hop, Direction direction) {
            if (hopped) {
                return false;
            }
            final int target = computeNewIndex(start, hop, direction);
            if (target == (direction == Direction.FORWARD ? 25 : 0)) {
                hopped = true;
                return true;
            }
            return !columns.get(target).isBlockedForDirection(direction);
        }

        @Override
        public int computeNewIndex(int start, int hop, Direction direction) {
            if (Direction.FORWARD == direction) {
                return Math.min(25, start + hop);
            } else {
                return Math.max(0, start - hop);
            }
        }
    };

    private final Hoppable strictCollect = new Hoppable() {
        @Override
        public boolean canHop(int start, int hop, Direction direction) {
            final int target = computeNewIndex(start, hop, direction);
            if (target == (direction == Direction.FORWARD ? 25 : 0)) {
                return true;
            }
            return !columns.get(target).isBlockedForDirection(direction);
        }

        @Override
        public int computeNewIndex(int start, int hop, Direction direction) {
            return start + hop * direction.getSign();
        }
    };


}
