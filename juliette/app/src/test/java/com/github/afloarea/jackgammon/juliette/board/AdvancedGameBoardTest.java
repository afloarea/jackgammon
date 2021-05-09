package com.github.afloarea.jackgammon.juliette.board;

import com.github.afloarea.jackgammon.juliette.GameMove;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AdvancedGameBoardTest {
    private static final Map<Integer, String> IDS_BY_POSITION;

    static {
        final String identifiers = "ABCDEFGHIJKLXWVUTSRQPONM";
        IDS_BY_POSITION = IntStream.range(0, identifiers.length()).boxed()
                .collect(Collectors.toUnmodifiableMap(
                        Function.identity(), index -> String.valueOf(identifiers.charAt(index))));
    }


    @Test
    void boardHandlesSimpleRoll() {
        final var board = GameBoard.buildNewBoard();
        final var diceResult = DiceRoll.of(2, 1);

        board.updateDiceForDirection(Direction.CLOCKWISE, diceResult);
        board.executeMoveInDirection(Direction.CLOCKWISE, buildMove(0, 1));
        board.executeMoveInDirection(Direction.CLOCKWISE, buildMove(0, 2));

        assertTrue(board.isCurrentTurnDone());
    }

    @Test
    void boardHandlesDouble() {
        final var board = GameBoard.buildNewBoard();
        final var diceResult = DiceRoll.of(2, 2);

        board.updateDiceForDirection(Direction.CLOCKWISE, diceResult);
        board.executeMoveInDirection(Direction.CLOCKWISE, buildMove(0, 2));
        board.executeMoveInDirection(Direction.CLOCKWISE, buildMove(2, 4));
        board.executeMoveInDirection(Direction.CLOCKWISE, buildMove(4, 6));
        board.executeMoveInDirection(Direction.CLOCKWISE, buildMove(0, 2));

        assertTrue(board.isCurrentTurnDone());
    }

    @Test
    void testCanEnter() {
        final var board = BoardFactory.build(new int[][]{
                {-2, +2, +2, +2, +2, +2, +5, -13, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}
        }, 1, 0, 0, 0);
        final var diceResult = DiceRoll.of(1, 6);

        board.updateDiceForDirection(Direction.CLOCKWISE, diceResult);
        assertTrue(board.getCurrentDirectionPossibleMoves().contains(buildEnter(Direction.CLOCKWISE, 0)));
        board.executeMoveInDirection(Direction.CLOCKWISE, buildEnter(Direction.CLOCKWISE, 0));
    }

    @Test
    void unableToEnter() {
        final var board = BoardFactory.build(new int[][]{
                        {-2, +2, +2, +2, +2, +2, +5, -13, 0, 0, 0, 0},
                        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}
                },
                1, 0, 0, 0
        );
        final var diceResult = DiceRoll.of(6, 6);

        board.updateDiceForDirection(Direction.CLOCKWISE, diceResult);
        assertTrue(board.isCurrentTurnDone());
    }

    @Test
    void testSuspend() {
        final var board = BoardFactory.build(new int[][]{
                        new int[]{-2, 0, 0, 0, 1, 4, 0, 3, 0, 0, 1, -5},
                        new int[]{+2, 0, 0, 0, 0, -5, 0, -3, 0, 0, 0, 4}
                }
        );
        final var diceResult = DiceRoll.of(4, 1);

        board.updateDiceForDirection(Direction.CLOCKWISE, diceResult);
        board.executeMoveInDirection(Direction.CLOCKWISE, buildMove(0, 4));
        board.executeMoveInDirection(Direction.CLOCKWISE, buildMove(0, 1));
        assertTrue(board.isCurrentTurnDone());

        final var secondDice = DiceRoll.of(6, 6);

        board.updateDiceForDirection(Direction.ANTICLOCKWISE, secondDice);
        assertTrue(board.isCurrentTurnDone());
    }

    @Test
    void testEnterWithSuspend() {
        final var board = BoardFactory.build(new int[][]{
                        new int[]{2, 2, 1, 2, 2, 2, 0, 2, 0, 0, 0, 0},
                        new int[]{-3, 2, 0, 0, 0, -5, 0, 0, 0, 0, 0, -5}
                },
                2, 0, 0, 0
        );
        final var diceResult = DiceRoll.of(4, 3);

        board.updateDiceForDirection(Direction.CLOCKWISE, diceResult);
        Assertions.assertEquals(Set.of(buildEnter(Direction.CLOCKWISE, 2)), board.getCurrentDirectionPossibleMoves());
        board.executeMoveInDirection(Direction.CLOCKWISE, buildEnter(Direction.CLOCKWISE, 2));
        assertTrue(board.isCurrentTurnDone());

        final var secondDice = DiceRoll.of(6, 2);

        board.updateDiceForDirection(Direction.ANTICLOCKWISE, secondDice);
        assertTrue(board.getCurrentDirectionPossibleMoves().contains(buildEnter(Direction.ANTICLOCKWISE, 22)));
        board.executeMoveInDirection(Direction.ANTICLOCKWISE, buildEnter(Direction.ANTICLOCKWISE, 22));
        Assertions.assertFalse(board.isCurrentTurnDone());
    }

    @Test
    void testGameWon() {
        final var board = BoardFactory.build(new int[][]{
                        new int[]{1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                        new int[]{0, -1, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0}
                },
                0, 0, 13, 14
        );
        final var diceResult = DiceRoll.of(3, 2);

        board.updateDiceForDirection(Direction.CLOCKWISE, diceResult);
        board.executeMoveInDirection(Direction.CLOCKWISE, buildCollect(Direction.CLOCKWISE, 22));
        board.executeMoveInDirection(Direction.CLOCKWISE, buildCollect(Direction.CLOCKWISE, 21));
        assertTrue(board.isCurrentTurnDone());
        assertTrue(board.isGameComplete());
        Assertions.assertEquals(Direction.CLOCKWISE, board.getWinningDirection());
    }

    @Test
    void collectWithHigh() {
        final var board = BoardFactory.build(new int[][]{
                        new int[]{4, 4, 4, 3, 0, 0, 0, 0, 0, 0, 0, 0},
                        new int[]{-5, -2, -4, -4, 0, 0, 0, 0, 0, 0, 0, 0}
                }
        );
        final var diceResult = DiceRoll.of(6, 5);

        board.updateDiceForDirection(Direction.CLOCKWISE, diceResult);
        Assertions.assertEquals(Set.of(buildCollect(Direction.CLOCKWISE, 20)), board.getCurrentDirectionPossibleMoves());
    }

    @Test
    void testMoveAndCollect() {
        final var board = BoardFactory.build(new int[][]{
                        new int[]{2, 2, 0, 1, 1, 7, 0, 0, 0, 0, 0, 0},
                        new int[]{0, 0, -1, -5, -2, -6, 0, -1, 0, 0, 0, 0}
                },
                0, 0, 0, 2
        );
        final var diceResult = DiceRoll.of(3, 5);

        board.updateDiceForDirection(Direction.CLOCKWISE, diceResult);
        board.executeMoveInDirection(Direction.CLOCKWISE, buildMove(16, 19));
        board.executeMoveInDirection(Direction.CLOCKWISE, buildCollect(Direction.CLOCKWISE, 19));

        assertTrue(board.isCurrentTurnDone());
        assertTrue(board.getCurrentDirectionPossibleMoves().isEmpty());
    }

    @Test
    void testForcedMove() {
        final var board = BoardFactory.build(new int[][]{
                        new int[]{-1, 2, 2, 0, 2, 2, 0, 0, 0, 2, 0, 0},
                        new int[]{0, 0, 0, -1, 0, 0, 0, 0, 0, 0, 0, 0}
                },
                0, 0, 13, 5
        );
        final var diceResult = DiceRoll.of(3, 6);

        board.updateDiceForDirection(Direction.CLOCKWISE, diceResult);

        Assertions.assertEquals(Set.of(buildMove(0, 6), buildMove(20, 23)), board.getCurrentDirectionPossibleMoves());
    }

    @Test
    void testNonForcedMove() {
        final var board = BoardFactory.build(new int[][]{
                        new int[]{2, 4, 3, 0, 0, 0, 0, 0, 0, 0, 0, -1},
                        new int[]{-2, -3, -2, -2, -2, -3, 0, 0, 0, 0, 0, 1}
                },
                0, 0, 0, 5
        );
        final var diceResult = DiceRoll.of(6, 1);

        board.updateDiceForDirection(Direction.CLOCKWISE, diceResult);

        final var availableMoves = board.getCurrentDirectionPossibleMoves();
        assertTrue(availableMoves.contains(buildMove(11, 12)));
    }

    @Test
    void testNonForcedWithCollect() {
        final var board = BoardFactory.build(new int[][]{
                        new int[]{6, 2, 2, 2, 2, 0, 0, 0, 0, 0, 0, 0},
                        new int[]{-2, -4, 0, 1, -3, -5, -1, 0, 0, 0, 0, 0}
                },
                0, 0, 0, 0
        );
        final var diceResult = DiceRoll.of(2, 1);

        board.updateDiceForDirection(Direction.CLOCKWISE, diceResult);
        board.executeMoveInDirection(Direction.CLOCKWISE, buildMove(17, 18));
        final var availableMoves = board.getCurrentDirectionPossibleMoves();
        assertTrue(availableMoves.contains(buildCollect(Direction.CLOCKWISE, 22)));
    }

    @Test
    void testNonForceWithCollect2() {
        final var board = BoardFactory.build(new int[][]{
                        new int[]{6, 2, 2, 2, 2, 0, 0, 0, 0, 0, 0, 0},
                        new int[]{-2, -9, 0, 1, -3, 0, 0, -1, 0, 0, 0, 0}
                },
                0, 0, 0, 0
        );
        final var diceResult = DiceRoll.of(6, 2);

        board.updateDiceForDirection(Direction.CLOCKWISE, diceResult);
        assertTrue(board.getCurrentDirectionPossibleMoves().contains(buildMove(16, 18)));
    }

    @Test
    void testCompositeMove() {
        final var board = BoardFactory.build(new int[][]{
                        new int[]{2, 0, 0, 0, 0, 5, 0, 2, -1, 0, -1, -5},
                        new int[]{0, 0, 0, 0, -2, -4, 2, -2, 0, 0, 0, 4}
                });
        final var diceResult = DiceRoll.of(6, 5);

        board.updateDiceForDirection(Direction.ANTICLOCKWISE, diceResult);
        board.executeMoveInDirection(Direction.ANTICLOCKWISE, buildMove(12, 1));
        assertTrue(board.isCurrentTurnDone());
    }

    @Test
    void testCompositeNoCollect() {
        final var board = BoardFactory.build(new int[][]{
                        new int[]{0, 0, 3, 3, 2, 4, 3, 0, 0, 0, 0, 0},
                        new int[]{0, -5, -2, -1, -3, -3, 0, -1, 0, 0, 0, 0}
                });
        final var diceResult = DiceRoll.of(3, 3);

        board.updateDiceForDirection(Direction.CLOCKWISE, diceResult);

        assertTrue(
                board.getCurrentDirectionPossibleMoves().stream().noneMatch(move -> move.getTo().equals("CB")));

    }

    @Test
    void testExecuteCollectWithHig() {
        final var board = BoardFactory.build(new int[][]{
                        new int[]{0, 1, 2, 2, 4, 0, 0, 0, 0, 0, 0, 0},
                        new int[]{0, 0, -1, -1, -2, -8, 0, 0, 0, 0, 0, 0}
                },
                0, 0, 3, 6
        );
        final var diceResult = DiceRoll.of(6, 2);

        board.updateDiceForDirection(Direction.ANTICLOCKWISE, diceResult);
        assertTrue(board.getCurrentDirectionPossibleMoves()
                .contains(buildCollect(Direction.ANTICLOCKWISE, 4)));
        board.executeMoveInDirection(Direction.ANTICLOCKWISE, buildCollect(Direction.ANTICLOCKWISE, 4));
        Assertions.assertFalse(board.isCurrentTurnDone());
    }

    @Test
    void testBasicMove() {
        final var board = BoardFactory.build(new int[][]{
                        new int[]{0, 0, 0, 0, -1, 5, 0, 3, 0, 0, 0, -4},
                        new int[]{-1, 0, 1, 1, 0, -5, 0, -4, 0, 0, 0, 5}
                });
        final var diceResult = DiceRoll.of(4, 1);

        board.updateDiceForDirection(Direction.ANTICLOCKWISE, diceResult);
        assertTrue(board.getCurrentDirectionPossibleMoves().contains(buildMove(21, 20)));
    }

    @Test
    void testFinishMove() {
        final var board = BoardFactory.build(new int[][]{
                new int[]{1, 2, 0, 0, 1, 2, 0, 0, 0, 0, 0, 0},
                new int[]{0, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}
        },
                0, 0, 14, 9);
        final var diceResult = DiceRoll.of(4, 1);

        board.updateDiceForDirection(Direction.CLOCKWISE, diceResult);

        board.executeMoveInDirection(Direction.CLOCKWISE, buildCollect(Direction.CLOCKWISE, 22));
        assertTrue(board.isGameComplete());
        Assertions.assertSame(Direction.CLOCKWISE, board.getWinningDirection());
    }

    @Test
    void testForcedComposite() {
        final var board = BoardFactory.build(new int[][]{
                new int[]{-3, 0, 0, 0, 0, 0, 8, 0, 0, 0, 0, -4},
                new int[]{-1, 2, 1, 2, 0, -3, 2, -4, 0, 0, 0, 0}
        });
        final var diceResult = DiceRoll.of(6, 3);

        board.updateDiceForDirection(Direction.CLOCKWISE, diceResult);

        Assertions.assertEquals(Set.of(buildMove(0, 3), buildMove(0, 9)), board.getCurrentDirectionPossibleMoves());
    }

    @Test
    void testForcedComposite2Columns() {
        final var board = BoardFactory.build(new int[][]{
                new int[]{-3, 0, 0, 0, 0, 0, 9, 0, 0, 0, 0, -4},
                new int[]{-1, 2, 1, 0, 0, -3, 3, -4, 0, 0, 0, 0}
        });
        final var diceResult = DiceRoll.of(6, 3);

        board.updateDiceForDirection(Direction.CLOCKWISE, diceResult);

        Assertions.assertEquals(Set.of(buildMove(0, 3), buildMove(0, 9), buildMove(11, 14), buildMove(11, 20)), board.getCurrentDirectionPossibleMoves());
    }

    @Test
    void testUnforcedToHomeArea() {
        final var board = BoardFactory.build(new int[][]{
                new int[]{2, 2, 1, 2, 4, 3, 0, 1, 0, 0, 0, 0},
                new int[]{-2, -2, -1, -2, -3, -4, 0, 0, 0, -1, 0, 0}
        });
        final var diceResult = DiceRoll.of(6, 4);

        board.updateDiceForDirection(Direction.ANTICLOCKWISE, diceResult);

        assertTrue(board.getCurrentDirectionPossibleMoves().containsAll(Set.of(buildMove(7, 3), buildMove(7, 1))));
    }

    private GameMove buildMove(int from, int to) {
        return new GameMove(IDS_BY_POSITION.get(from), IDS_BY_POSITION.get(to));
    }

    private GameMove buildEnter(Direction direction, int to) {
        return new GameMove("S" + getSymbolForDirection(direction), IDS_BY_POSITION.get(to));
    }

    private GameMove buildCollect(Direction direction, int from) {
        return new GameMove(IDS_BY_POSITION.get(from), "C" + getSymbolForDirection(direction));
    }

    private String getSymbolForDirection(Direction direction) {
        if (direction == Direction.NONE) {
            return " ";
        }
        return direction == Direction.CLOCKWISE ? "B" : "W";
    }

}
