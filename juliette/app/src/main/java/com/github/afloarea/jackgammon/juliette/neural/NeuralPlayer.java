package com.github.afloarea.jackgammon.juliette.neural;

import com.github.afloarea.obge.moves.ObgMove;
import com.github.afloarea.obge.moves.ObgTransition;

import java.util.List;
import java.util.Set;

public interface NeuralPlayer {

    List<ObgTransition> selectSequence(Set<List<ObgTransition>> sequences);

    void updateBoard(List<ObgMove> moves);

}
