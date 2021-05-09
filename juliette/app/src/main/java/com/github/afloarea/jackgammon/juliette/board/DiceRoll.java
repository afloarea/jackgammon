package com.github.afloarea.jackgammon.juliette.board;

import java.util.Objects;
import java.util.Random;
import java.util.StringJoiner;
import java.util.stream.IntStream;

public final class DiceRoll {
    private static final int MIN_DICE = 1;
    private static final int MAX_DICE = 6;
    private static final Random RANDOM = new Random();

    private final int dice1;
    private final int dice2;

    private DiceRoll(int dice1, int dice2) {
        this.dice1 = dice1;
        this.dice2 = dice2;
    }

    public static DiceRoll of(int dice1, int dice2) {
        if (dice1 < MIN_DICE || dice1 > MAX_DICE || dice2 < MIN_DICE || dice2 > MAX_DICE) {
            throw new IllegalArgumentException("Dice values must be between " + MIN_DICE + " and " + MAX_DICE);
        }
        return new DiceRoll(Math.max(dice1, dice2), Math.min(dice1, dice2));
    }

    public boolean isDouble() {
        return dice1 == dice2;
    }

    public boolean isSimple() {
        return dice1 != dice2;
    }

    public IntStream stream() {
        return isSimple() ? IntStream.of(dice1, dice2) : IntStream.generate(() -> dice1).limit(4);
    }

    public static DiceRoll generate() {
        final int first = MIN_DICE + RANDOM.nextInt(MAX_DICE);
        final int second = MIN_DICE + RANDOM.nextInt(MAX_DICE);
        return new DiceRoll(Math.max(first, second), Math.min(first, second));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DiceRoll)) return false;
        DiceRoll that = (DiceRoll) o;
        return dice1 == that.dice1 && dice2 == that.dice2;
    }

    @Override
    public int hashCode() {
        return Objects.hash(dice1, dice2);
    }

    public int getDice1() {
        return dice1;
    }

    public int getDice2() {
        return dice2;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", DiceRoll.class.getSimpleName() + "[", "]")
                .add("dice1=" + dice1)
                .add("dice2=" + dice2)
                .toString();
    }
}
