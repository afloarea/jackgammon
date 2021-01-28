package com.github.afloarea.jackgammon.juliette.board.moves.generator;

import com.github.afloarea.jackgammon.juliette.board.BoardColumn;
import com.github.afloarea.jackgammon.juliette.board.Constants;
import com.github.afloarea.jackgammon.juliette.board.layout.ColumnSequence;
import com.github.afloarea.jackgammon.juliette.board.Direction;
import com.github.afloarea.jackgammon.juliette.board.Move;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class DefaultMoveProvider implements PossibleMovesProvider {
    private final ColumnSequence columnSequence;

    private final MoveCalculator basicMoveCalculator;
    private final MoveCalculator permissiveCalculator;
    private final MoveCalculator strictCalculator;

    private List<Integer> currentDice;
    private Direction currentDirection;

    public DefaultMoveProvider(ColumnSequence columnSequence) {
        this.columnSequence = columnSequence;
        this.basicMoveCalculator = new BasicMoveCalculator(columnSequence);
        this.permissiveCalculator = new PermissiveCollectMoveCalculator(columnSequence);
        this.strictCalculator = new StrictCollectMoveCalculator(columnSequence);
    }

    @Override
    public Stream<Move> streamPossibleMoves(List<Integer> dice, Direction direction) {
        this.currentDice = dice;
        this.currentDirection = direction;
        return filterMovesToMaximizeDiceValuesUsed(computePossibleMoves().distinct());
    }

    private Stream<Move> computePossibleMoves() {
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
        if (isSimpleDice(currentDice)) {
            reversed.addAll(currentDice);
            Collections.reverse(reversed);
        }

        final var availableColumns = columnSequence.stream(currentDirection)
                .filter(column -> currentDirection == column.getMovingDirectionOfElements())
                .collect(Collectors.toList());

        final var firstColumn = availableColumns.get(0);
        if (isSuspendColumn(firstColumn)) {
            final var sourceStream = firstColumn.getPieceCount() > 1
                    ? currentDice.stream().distinct().map(Collections::singletonList)
                    : Stream.of(currentDice, reversed);
            return sourceStream.flatMap(hops -> computeBasic(firstColumn, hops, currentDirection));
        }

        final int uncollectablePieces = columnSequence.countPiecesUpToIndex(Constants.HOME_START, currentDirection);
        if (uncollectablePieces > 1) { // there can be no single piece collected
            return Stream.of(currentDice, reversed)
                    .flatMap(hops -> availableColumns.stream()
                            .flatMap(column -> computeBasic(column, hops, currentDirection)));
        }

        final Stream<Move> farthestColumnMoves = Stream.of(currentDice, reversed)
                .flatMap(hops -> computePermissive(firstColumn, hops, currentDirection));

        final var collectableCalculator = uncollectablePieces == 0 ? strictCalculator : basicMoveCalculator;
        final Stream<Move> potentiallyCollectableColumnMoves = Stream.of(currentDice, reversed)
                .flatMap(hops -> availableColumns.subList(1, availableColumns.size()).stream()
                        .flatMap(column -> computeMoves(collectableCalculator, column, hops, currentDirection)));

        return Stream.concat(farthestColumnMoves, potentiallyCollectableColumnMoves);
    }

    private boolean isSuspendColumn(BoardColumn column) {
        return Objects.equals(column.getId(), columnSequence.getSuspendedColumn(currentDirection).getId());
    }

    private Stream<Move> computeMoves(
            MoveCalculator calculator, BoardColumn from, List<Integer> hops, Direction direction) {
        return calculator.computeMovesFromStart(columnSequence.getColumnIndex(from, direction), hops, direction);
    }

    private Stream<Move> computeBasic(BoardColumn from, List<Integer> hops, Direction direction) {
        return computeMoves(basicMoveCalculator, from, hops, direction);
    }
    private Stream<Move> computePermissive(BoardColumn from, List<Integer> hops, Direction direction) {
        return computeMoves(permissiveCalculator, from, hops, direction);
    }

    private Stream<Move> filterMovesToMaximizeDiceValuesUsed(
            Stream<Move> originalMoves) {

        if (!isSimpleDice(currentDice)) {
            return originalMoves;
        }

        final Map<List<Integer>, List<Move>> movesByDice = originalMoves.collect(Collectors.groupingBy(Move::getDistances));
        final Stream<Move> moves = movesByDice.values().stream().flatMap(Collection::stream);

        if (movesByDice.keySet().size() != 2) { // if there are composite moves or only one dice value was used
            return moves;
        }

        final Integer firstDice = currentDice.get(0);
        final Integer secondDice = currentDice.get(1);

        final var firstConstrained = findConstrainedColumn(movesByDice.get(List.of(firstDice)));
        final var secondConstrained = findConstrainedColumn(movesByDice.get(List.of(secondDice)));

        if (firstConstrained.isPresent() && secondConstrained.isPresent()
                || firstConstrained.isEmpty() && secondConstrained.isEmpty()) {
            return moves;
        }

        final String sourceColumnId;
        final Integer constrainedDice;
        if (firstConstrained.isPresent()) {
            sourceColumnId = firstConstrained.get().getId();
            constrainedDice = firstDice;
        } else {
            sourceColumnId = secondConstrained.get().getId();
            constrainedDice = secondDice;
        }

        return moves.filter(move -> !move.getSource().getId().equals(sourceColumnId)
                || move.getDistances().contains(constrainedDice));
    }

    private boolean isSimpleDice(List<Integer> dice) {
        return dice.size() == 2 && !dice.get(0).equals(dice.get(1));
    }

    private Optional<BoardColumn> findConstrainedColumn(List<Move> original) {
        if (original == null || original.size() != 1) {
            return Optional.empty();
        }
        return Optional.of(original.get(0).getSource())
                .filter(column -> column.getPieceCount() == 1);
    }
}
