package com.github.afloarea.jackgammon.juliette.board.moves.generator;

import com.github.afloarea.jackgammon.juliette.board.BoardColumn;
import com.github.afloarea.jackgammon.juliette.board.Direction;
import com.github.afloarea.jackgammon.juliette.board.Move;
import com.github.afloarea.jackgammon.juliette.board.layout.ColumnSequence;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.afloarea.jackgammon.juliette.board.Constants.HOME_START;

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

        final int noncollectablePieces = columnSequence.countPiecesUpToIndex(HOME_START, currentDirection);
        if (noncollectablePieces > 1) { // there can be no single piece collected
            return Stream.of(currentDice, reversed)
                    .flatMap(hops -> availableColumns.stream()
                            .flatMap(column -> computeBasic(column, hops, currentDirection)));
        }

        final Stream<Move> farthestColumnMoves = Stream.of(currentDice, reversed)
                .flatMap(hops -> computePermissive(firstColumn, hops, currentDirection));

        final var collectableCalculator = noncollectablePieces == 0 ? strictCalculator : basicMoveCalculator;
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

    private Stream<Move> filterMovesToMaximizeDiceValuesUsed(Stream<Move> originalMoves) {
        if (!isSimpleDice(currentDice)) {
            return originalMoves;
        }

        // group by used dice values combinations
        final Map<Set<Integer>, List<Move>> movesByDice = originalMoves
                .collect(Collectors.groupingBy(move -> new HashSet<>(move.getDistances())));
        final Stream<Move> moves = movesByDice.values().stream().flatMap(Collection::stream);

        if (movesByDice.keySet().size() != 2) {
            return moves;
        }

        // either both are sets with one value or one is with one value and the other with 2 values
        if (movesByDice.keySet().stream().allMatch(diceValues -> diceValues.size() == 1)) {
            return maximizeForSimpleMoves(movesByDice, moves);
        }

        return maximizeForCompositeMove(movesByDice, moves);
    }

    private Stream<Move> maximizeForCompositeMove(Map<Set<Integer>, List<Move>> movesByDice, Stream<Move> moves) {
        final var diceSet = Set.of(currentDice.get(0), currentDice.get(1));
        final Set<String> viableColumnIds = movesByDice.get(diceSet).stream()
                .map(move -> move.getSource().getId())
                .collect(Collectors.toSet());

        return moves.filter(move -> viableColumnIds.contains(move.getSource().getId()));
    }

    private Stream<Move> maximizeForSimpleMoves(Map<Set<Integer>, List<Move>> movesByDice, Stream<Move> moves) {
        final Integer firstDice = currentDice.get(0);
        final Integer secondDice = currentDice.get(1);

        final var firstConstrained = findConstrainedColumn(movesByDice.get(Set.of(firstDice)));
        final var secondConstrained = findConstrainedColumn(movesByDice.get(Set.of(secondDice)));

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

        // the other (non-constraining) dice value move must be removed from the constrained column if it's present
        return moves.filter(move -> !move.getSource().getId().equals(sourceColumnId)
                || move.getDistances().contains(constrainedDice));
    }

    private boolean isSimpleDice(List<Integer> dice) {
        return dice.size() == 2 && !dice.get(0).equals(dice.get(1));
    }

    private Optional<BoardColumn> findConstrainedColumn(List<Move> original) {
        if (original.size() != 1) {
            return Optional.empty();
        }

        final var move = original.get(0);
        if (move.getSource().getPieceCount() != 1) {
            return Optional.empty();
        }
        
        // (all other pieces are in home area or collected) and target column is in home area
        // in this case the "forced" move (fake forced) will be used to a collect piece after the other move
        final int piecesOutsideHome = columnSequence.countPiecesUpToIndex(HOME_START, currentDirection);
        if (piecesOutsideHome == 1 && columnSequence.getColumnIndex(move.getTarget(), currentDirection) >= HOME_START) {
            return Optional.empty();
        }

        return Optional.of(move.getSource());
    }
}
