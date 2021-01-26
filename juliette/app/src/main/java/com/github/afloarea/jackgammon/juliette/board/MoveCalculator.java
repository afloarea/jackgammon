package com.github.afloarea.jackgammon.juliette.board;

import java.util.List;
import java.util.stream.Stream;

public interface MoveCalculator {

    Stream<Move> computeMovesFromStart(int startIndex, List<Integer> availableHops, Direction direction);

}
