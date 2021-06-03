package com.github.afloarea.jackgammon.juliette.neural;

import com.github.afloarea.jackgammon.juliette.GameMove;
import com.github.afloarea.obge.Direction;
import com.github.afloarea.obge.dice.DiceValues;
import com.github.afloarea.obge.factory.BoardTemplate;
import com.github.afloarea.obge.moves.ObgMove;
import com.github.afloarea.obge.moves.ObgTransition;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class MoveSequenceConverter {
    private static final BoardTemplate BOARD_TEMPLATE = BoardTemplate.getDefault();

    public static List<GameMove> convertSequenceToMoves(Direction direction, List<ObgTransition> sequence) {
        return sequence.stream()
                .flatMap(transition -> {
                    final var suspendMoveStream = Stream.ofNullable(
                            transition.isSuspending()
                                    ? new GameMove(transition.getTarget(), BOARD_TEMPLATE.getSuspendId(direction.reverse()))
                                    : null
                    );
                    return Stream.concat(suspendMoveStream, Stream.of(new GameMove(transition.getSource(), transition.getTarget())));
                })
                .collect(Collectors.toList());
    }

    public static List<ObgTransition> convertMovesToSequence(Direction direction, List<ObgMove> moves) {
        final var sequence = new ArrayList<ObgTransition>();

        for (int index = 0; index < moves.size(); index++) {
            final var move = moves.get(index);
            if (!move.getDiceValues().equals(DiceValues.NONE)) {
                sequence.add(new ObgTransition(move.getSource(), move.getTarget(), move.getDiceValues().iterator().next(), false));
                continue;
            }

            final var nextMove = moves.get(index + 1);
            sequence.add(new ObgTransition(nextMove.getSource(), nextMove.getTarget(), nextMove.getDiceValues().iterator().next(), true));
        }

        return sequence;
    }

    private MoveSequenceConverter() {
    }
}
