package com.github.afloarea.jackgammon.juliette.messages.server;

import com.github.afloarea.jackgammon.juliette.GameMove;
import com.github.afloarea.jackgammon.juliette.manager.GameToPlayerMessage;
import com.github.afloarea.obge.moves.ObgMove;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public record PromptMoveMessage(Map<String, Set<String>> possibleMoves) implements GameToPlayerMessage {

    public PromptMoveMessage(Set<ObgMove> possibleMoves) {
        this(groupPossibleMoves(possibleMoves));
    }

    private static Map<String, Set<String>> groupPossibleMoves(Set<ObgMove> possibleMoves) {
        return possibleMoves.stream()
                .map(GameMove::fromBgMove)
                .collect(Collectors.groupingBy(
                        GameMove::from, Collectors.mapping(GameMove::to, Collectors.toSet())));
    }

    public Map<String, Set<String>> getPossibleMoves() {
        return possibleMoves;
    }
}
