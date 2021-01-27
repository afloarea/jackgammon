package com.github.afloarea.jackgammon.juliette.board;

import java.util.List;
import java.util.stream.Stream;

public interface PossibleMovesProvider {

    Stream<Move> streamPossibleMoves(List<Integer> dice, Direction direction);
}
