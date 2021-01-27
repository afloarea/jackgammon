package com.github.afloarea.jackgammon.juliette.board;

import com.github.afloarea.jackgammon.juliette.Color;
import com.github.afloarea.jackgammon.juliette.DiceResult;
import com.github.afloarea.jackgammon.juliette.GameMove;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Set;

class BasicGameBoardTest {

    @Test
    void boardHandlesSimpleRoll() {
        final var board = GameBoard.buildNewBoard();
        final var diceResult = new DiceResult(2, 1);

        board.updateDiceForPlayingColor(Color.BLACK, diceResult);
        board.executeMoveForPlayingColor(Color.BLACK, buildMove(0, 1));
        board.executeMoveForPlayingColor(Color.BLACK, buildMove(0, 2));

        Assertions.assertTrue(board.currentPlayingColorFinishedTurn());
    }

    @Test
    void boardHandlesDouble() {
        final var board = GameBoard.buildNewBoard();
        final var diceResult = new DiceResult(2, 2);

        board.updateDiceForPlayingColor(Color.BLACK, diceResult);
        board.executeMoveForPlayingColor(Color.BLACK, buildMove(0, 2));
        board.executeMoveForPlayingColor(Color.BLACK, buildMove(2, 4));
        board.executeMoveForPlayingColor(Color.BLACK, buildMove(4, 6));
        board.executeMoveForPlayingColor(Color.BLACK, buildMove(0, 2));

        Assertions.assertTrue(board.currentPlayingColorFinishedTurn());
    }

    @Test
    void testCanEnter() {
        final var board = BoardFactory.build(new int[][]{
                {-2, +2, +2, +2, +2, +2, +5, -13, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}
        }, 1, 0, 0, 0);
        final var diceResult = new DiceResult(1, 6);

        board.updateDiceForPlayingColor(Color.BLACK, diceResult);
        Assertions.assertTrue(board.getPossibleMovesForCurrentPlayingColor().contains(buildEnter(Color.BLACK, 0)));
        board.executeMoveForPlayingColor(Color.BLACK, buildEnter(Color.BLACK, 0));
    }

    @Test
    void unableToEnter() {
        final var board = BoardFactory.build(new int[][]{
                        {-2, +2, +2, +2, +2, +2, +5, -13, 0, 0, 0, 0},
                        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}
                },
                1, 0, 0, 0
        );
        final var diceResult = new DiceResult(6, 6);

        board.updateDiceForPlayingColor(Color.BLACK, diceResult);
        Assertions.assertTrue(board.currentPlayingColorFinishedTurn());
    }

    @Test
    void testSuspend() {
        final var board = BoardFactory.build(new int[][]{
                        new int[]{-2, 0, 0, 0, 1, 4, 0, 3, 0, 0, 1, -5},
                        new int[]{+2, 0, 0, 0, 0, -5, 0, -3, 0, 0, 0, 4}
                }
        );
        final var diceResult = new DiceResult(4, 1);

        board.updateDiceForPlayingColor(Color.BLACK, diceResult);
        board.executeMoveForPlayingColor(Color.BLACK, buildMove(0, 4));
        board.executeMoveForPlayingColor(Color.BLACK, buildMove(0, 1));
        Assertions.assertTrue(board.currentPlayingColorFinishedTurn());

        final var secondDice = new DiceResult(6, 6);

        board.updateDiceForPlayingColor(Color.WHITE, secondDice);
        Assertions.assertTrue(board.currentPlayingColorFinishedTurn());
    }

    @Test
    void testEnterWithSuspend() {
        final var board = BoardFactory.build(new int[][]{
                        new int[]{2, 2, 1, 2, 2, 2, 0, 2, 0, 0, 0, 0},
                        new int[]{-3, 2, 0, 0, 0, -5, 0, 0, 0, 0, 0, -5}
                },
                2, 0, 0, 0
        );
        final var diceResult = new DiceResult(4, 3);

        board.updateDiceForPlayingColor(Color.BLACK, diceResult);
        Assertions.assertEquals(Set.of(buildEnter(Color.BLACK, 2)), board.getPossibleMovesForCurrentPlayingColor());
        board.executeMoveForPlayingColor(Color.BLACK, buildEnter(Color.BLACK, 2));
        Assertions.assertTrue(board.currentPlayingColorFinishedTurn());

        final var secondDice = new DiceResult(6, 2);

        board.updateDiceForPlayingColor(Color.WHITE, secondDice);
        Assertions.assertTrue(board.getPossibleMovesForCurrentPlayingColor().contains(buildEnter(Color.WHITE, 22)));
        board.executeMoveForPlayingColor(Color.WHITE, buildEnter(Color.WHITE, 22));
        Assertions.assertFalse(board.currentPlayingColorFinishedTurn());
    }

    @Test
    void testGameWon() {
        final var board = BoardFactory.build(new int[][]{
                        new int[]{1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                        new int[]{0, -1, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0}
                },
                0, 0, 13, 14
        );
        final var diceResult = new DiceResult(3, 2);

        board.updateDiceForPlayingColor(Color.BLACK, diceResult);
        board.executeMoveForPlayingColor(Color.BLACK, buildCollect(Color.BLACK, 22));
        board.executeMoveForPlayingColor(Color.BLACK, buildCollect(Color.BLACK, 21));
        Assertions.assertTrue(board.currentPlayingColorFinishedTurn());
        Assertions.assertTrue(board.isGameComplete());
        Assertions.assertEquals(Color.BLACK, board.getWinningColor());
    }

    @Test
    void collectWithHigh() {
        final var board = BoardFactory.build(new int[][]{
                        new int[]{4, 4, 4, 3, 0, 0, 0, 0, 0, 0, 0, 0},
                        new int[]{-5, -2, -4, -4, 0, 0, 0, 0, 0, 0, 0, 0}
                },
                0, 0, 0, 0
        );
        final var diceResult = new DiceResult(6, 5);

        board.updateDiceForPlayingColor(Color.BLACK, diceResult);
        Assertions.assertEquals(Set.of(buildCollect(Color.BLACK, 20)), board.getPossibleMovesForCurrentPlayingColor());
    }

    @Test
    void testMoveAndCollect() {
        final var board = BoardFactory.build(new int[][]{
                        new int[]{2, 2, 0, 1, 1, 7, 0, 0, 0, 0, 0, 0},
                        new int[]{0, 0, -1, -5, -2, -6, 0, -1, 0, 0, 0, 0}
                },
                0, 0, 0, 2
        );
        final var diceResult = new DiceResult(3, 5);

        board.updateDiceForPlayingColor(Color.BLACK, diceResult);
        board.executeMoveForPlayingColor(Color.BLACK, buildMove(16, 19));
        board.executeMoveForPlayingColor(Color.BLACK, buildCollect(Color.BLACK, 19));

        Assertions.assertTrue(board.currentPlayingColorFinishedTurn());
        Assertions.assertTrue(board.getPossibleMovesForCurrentPlayingColor().isEmpty());
    }

    @Test
    void testForcedMove() {
        final var board = BoardFactory.build(new int[][]{
                        new int[]{-1, 2, 2, 0, 2, 2, 0, 0, 0, 2, 0, 0},
                        new int[]{0, 0, 0, -1, 0, 0, 0, 0, 0, 0, 0, 0}
                },
                0, 0, 13, 5
        );
        final var diceResult = new DiceResult(3, 6);

        board.updateDiceForPlayingColor(Color.BLACK, diceResult);

        Assertions.assertEquals(2, board.getPossibleMovesForCurrentPlayingColor().size());
    }

    @Test
    void testNonForcedMove() {
        final var board = BoardFactory.build(new int[][]{
                        new int[]{2, 4, 3, 0, 0, 0, 0, 0, 0, 0, 0, -1},
                        new int[]{-2, -3, -2, -2, -2, -3, 0, 0, 0, 0, 0, 1}
                },
                0, 0, 0, 5
        );
        final var diceResult = new DiceResult(6, 1);

        board.updateDiceForPlayingColor(Color.BLACK, diceResult);

        final var availableMoves = board.getPossibleMovesForCurrentPlayingColor();
        Assertions.assertTrue(availableMoves.contains(buildMove(11, 12)));
    }

    @Test
    void testNonForcedWithCollect() {
        final var board = BoardFactory.build(new int[][]{
                        new int[]{6, 2, 2, 2, 2, 0, 0, 0, 0, 0, 0, 0},
                        new int[]{-2, -4, 0, 1, -3, -5, -1, 0, 0, 0, 0, 0}
                },
                0, 0, 0, 0
        );
        final var diceResult = new DiceResult(2, 1);

        board.updateDiceForPlayingColor(Color.BLACK, diceResult);
        board.executeMoveForPlayingColor(Color.BLACK, buildMove(17, 18));
        final var availableMoves = board.getPossibleMovesForCurrentPlayingColor();
        Assertions.assertTrue(availableMoves.contains(buildCollect(Color.BLACK, 22)));
    }

    @Test
    void testNonForceWithCollect2() {
        final var board = BoardFactory.build(new int[][]{
                        new int[]{6, 2, 2, 2, 2, 0, 0, 0, 0, 0, 0, 0},
                        new int[]{-2, -9, 0, 1, -3, 0, 0, -1, 0, 0, 0, 0}
                },
                0, 0, 0, 0
        );
        final var diceResult = new DiceResult(6, 2);

        board.updateDiceForPlayingColor(Color.BLACK, diceResult);
        Assertions.assertTrue(board.getPossibleMovesForCurrentPlayingColor().contains(buildMove(16, 18)));
    }

    private GameMove buildMove(int from, int to) {
        return new GameMove(BoardFactory.IDS_BY_POSITION.get(from), BoardFactory.IDS_BY_POSITION.get(to));
    }

    private GameMove buildEnter(Color color, int to) {
        return new GameMove("S" + color.getSymbol(), BoardFactory.IDS_BY_POSITION.get(to));
    }

    private GameMove buildCollect(Color color, int from) {
        return new GameMove(BoardFactory.IDS_BY_POSITION.get(from), "C" + color.getSymbol());
    }

}
