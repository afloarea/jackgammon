package com.github.afloarea.jackgammon.juliette.board;

import com.github.afloarea.jackgammon.juliette.Color;

import java.util.Map;

public final class Constants {
    public static final int BOARD_COLUMNS = 24;
    public static final int PIECES_PER_PLAYER = 15;
    public static final int MAX_DICE = 6;
    public static final int MIN_DICE = 1;

    public static final Map<Direction, Color> COLORS_BY_DIRECTION = Map.of(
            Direction.NONE, Color.NONE,
            Direction.FORWARD, Color.BLACK,
            Direction.BACKWARD, Color.WHITE);
    public static final Map<Color, Direction> DIRECTION_BY_COLOR = Map.of(
            Color.NONE, Direction.NONE,
            Color.BLACK, Direction.FORWARD,
            Color.WHITE, Direction.BACKWARD);

    public static final String[][] BOARD_TEMPLATE = {
            {"A", "B", "C", "D"},
            {}
    };



    private Constants() {}
}
