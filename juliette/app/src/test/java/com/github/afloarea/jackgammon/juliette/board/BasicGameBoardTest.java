package com.github.afloarea.jackgammon.juliette.board;

import com.github.afloarea.jackgammon.juliette.Color;
import com.github.afloarea.jackgammon.juliette.DiceResult;
import com.github.afloarea.jackgammon.juliette.GameMove;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Set;

class BasicGameBoardTest {

    @Test void boardHandlesSimpleRoll() {
        final var board = GameBoard.buildNewBoard();
        final var diceResult = new DiceResult(2, 1);

        board.updateDiceForPlayingColor(Color.BLACK, diceResult);
        board.executeMoveForPlayingColor(Color.BLACK, GameMove.move(0, 1));
        board.executeMoveForPlayingColor(Color.BLACK, GameMove.move(0, 2));

        Assertions.assertTrue(board.currentPlayingColorFinishedTurn());
    }

    @Test void boardHandlesDouble() {
        final var board = GameBoard.buildNewBoard();
        final var diceResult = new DiceResult(2, 2);

        board.updateDiceForPlayingColor(Color.BLACK, diceResult);
        board.executeMoveForPlayingColor(Color.BLACK, GameMove.move(0, 2));
        board.executeMoveForPlayingColor(Color.BLACK, GameMove.move(2, 4));
        board.executeMoveForPlayingColor(Color.BLACK, GameMove.move(4, 6));
        board.executeMoveForPlayingColor(Color.BLACK, GameMove.move(0, 2));

        Assertions.assertTrue(board.currentPlayingColorFinishedTurn());
    }

    @Test void testCanEnter() {
        final var board = BoardFactory.build(
                new int[] {-2, +2, +2, +2, +2, +2,  +5, -13, 0, 0, 0, 0},
                new int[] {0, 0, 0, 0, 0, 0,   0, 0, 0, 0, 0 ,0},
                1, 0, 0, 0
        );
        final var diceResult = new DiceResult(1, 6);

        board.updateDiceForPlayingColor(Color.BLACK, diceResult);
        Assertions.assertTrue(board.getPossibleMovesForCurrentPlayingColor().contains(GameMove.enter(0)));
        board.executeMoveForPlayingColor(Color.BLACK, GameMove.enter(0));
    }

    @Test void unableToEnter() {
        final var board = BoardFactory.build(
                new int[] {-2, +2, +2, +2, +2, +2,  +5, -13, 0, 0, 0, 0},
                new int[] {0, 0, 0, 0, 0, 0,   0, 0, 0, 0, 0 ,0},
                1, 0, 0, 0
        );
        final var diceResult = new DiceResult(6, 6);

        board.updateDiceForPlayingColor(Color.BLACK, diceResult);
        Assertions.assertTrue(board.currentPlayingColorFinishedTurn());
    }

    @Test void testSuspend() {
        final var board = BoardFactory.build(
                new int[]{-2, 0, 0, 0, 1, 4,   0, 3, 0, 0, 1, -5},
                new int[]{+2, 0, 0, 0, 0,-5,   0,-3, 0, 0, 0,  4}
        );
        final var diceResult = new DiceResult(4, 1);

        board.updateDiceForPlayingColor(Color.BLACK, diceResult);
        board.executeMoveForPlayingColor(Color.BLACK, GameMove.move(0, 4));
        board.executeMoveForPlayingColor(Color.BLACK, GameMove.move(0, 1));
        Assertions.assertTrue(board.currentPlayingColorFinishedTurn());

        final var secondDice = new DiceResult(6, 6);

        board.updateDiceForPlayingColor(Color.WHITE, secondDice);
        Assertions.assertTrue(board.currentPlayingColorFinishedTurn());
    }

    @Test void testEnterWithSuspend() {
        final var board = BoardFactory.build(
                new int[] { 2, 2, 1, 2, 2,  2,   0, 2, 0, 0, 0,  0},
                new int[] {-3, 2, 0, 0, 0, -5,   0, 0, 0, 0, 0, -5},
                2, 0, 0, 0
        );
        final var diceResult = new DiceResult(4, 3);

        board.updateDiceForPlayingColor(Color.BLACK, diceResult);
        Assertions.assertEquals(Set.of(GameMove.enter(2)), board.getPossibleMovesForCurrentPlayingColor());
        board.executeMoveForPlayingColor(Color.BLACK, GameMove.enter(2));
        Assertions.assertTrue(board.currentPlayingColorFinishedTurn());

        final var secondDice = new DiceResult(6, 2);

        board.updateDiceForPlayingColor(Color.WHITE, secondDice);
        Assertions.assertEquals(Set.of(GameMove.enter(22)), board.getPossibleMovesForCurrentPlayingColor());
        board.executeMoveForPlayingColor(Color.WHITE, GameMove.enter(22));
        Assertions.assertFalse(board.currentPlayingColorFinishedTurn());
    }

    @Test void testGameWon() {
        final var board = BoardFactory.build(
                new int[] {1, 0, 0, 0, 0, 0,    0, 0, 0, 0, 0, 0},
                new int[] {0, -1, -1, 0, 0, 0,  0, 0, 0, 0, 0, 0},
                0, 0, 13, 14
        );
        final var diceResult = new DiceResult(3, 2);

        board.updateDiceForPlayingColor(Color.BLACK, diceResult);
        board.executeMoveForPlayingColor(Color.BLACK, GameMove.collect(22));
        board.executeMoveForPlayingColor(Color.BLACK, GameMove.collect(21));
        Assertions.assertTrue(board.currentPlayingColorFinishedTurn());
        Assertions.assertTrue(board.isGameComplete());
        Assertions.assertEquals(Color.BLACK, board.getWinningColor());
    }

    @Test void collectWithHigh() {
        final var board = BoardFactory.build(
                new int[] {4, 4, 4, 3, 0, 0,    0, 0, 0, 0, 0, 0},
                new int[] {-5, -2, -4, -4, 0, 0,  0, 0, 0, 0, 0, 0},
                0, 0, 0, 0
        );
        final var diceResult = new DiceResult(6, 5);

        board.updateDiceForPlayingColor(Color.BLACK, diceResult);
        Assertions.assertEquals(Set.of(GameMove.collect(20)), board.getPossibleMovesForCurrentPlayingColor());
    }

    @Test void testMoveAndCollect() {
        final var board = BoardFactory.build(
                new int[] {2, 2, 0, 1, 1, 7,    0, 0, 0, 0, 0, 0},
                new int[] {0, 0, -1, -5, -2, -6,  0, -1, 0, 0, 0, 0},
                0, 0, 0, 2
        );
        final var diceResult = new DiceResult(3, 5);

        board.updateDiceForPlayingColor(Color.BLACK, diceResult);
        board.executeMoveForPlayingColor(Color.BLACK, GameMove.move(16, 19));
        board.executeMoveForPlayingColor(Color.BLACK, GameMove.collect(19));

        Assertions.assertTrue(board.currentPlayingColorFinishedTurn());
    }

}
