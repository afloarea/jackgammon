package com.github.afloarea.jackgammon.juliette;

import java.util.Objects;

public final class BoardPosition {
    public static final int MAX_POSITION_INDEX = 25;

    private int pieceCount = 0;
    private Color pieceColor = Color.NONE;
    private final int blackViewIndex;
    private final int whiteViewIndex;

    public BoardPosition(int pieceCount, Color pieceColor, int blackViewIndex, int whiteViewIndex) {
        this.blackViewIndex = blackViewIndex;
        this.whiteViewIndex = whiteViewIndex;
        this.pieceCount = pieceCount;
        this.pieceColor = pieceColor;
    }

    public static BoardPosition emptyFromBlackView(int blackViewIndex) {
        return new BoardPosition(0, Color.BLACK, blackViewIndex, complementPosition(blackViewIndex));
    }

    public static BoardPosition emptyFromWhiteView(int whiteViewIndex) {
        return new BoardPosition(0, Color.WHITE, complementPosition(whiteViewIndex), whiteViewIndex);
    }

    public static BoardPosition ofColorFromBlackView(int pieceCount, Color color, int blackViewIndex) {
        return new BoardPosition(pieceCount, color, blackViewIndex, complementPosition(blackViewIndex));
    }

    public static BoardPosition ofColorFromWhiteView(int pieceCount, Color color, int whiteViewIndex) {
        return new BoardPosition(pieceCount, color, complementPosition(whiteViewIndex), whiteViewIndex);
    }

    private static int complementPosition(int position) {
        return MAX_POSITION_INDEX - position;
    }

    // additional methods to be added

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BoardPosition)) return false;
        BoardPosition that = (BoardPosition) o;
        return pieceCount == that.pieceCount
                && blackViewIndex == that.blackViewIndex
                && whiteViewIndex == that.whiteViewIndex
                && pieceColor == that.pieceColor;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceCount, pieceColor, blackViewIndex, whiteViewIndex);
    }

    public int getPieceCount() {
        return pieceCount;
    }

    public Color getPieceColor() {
        return pieceColor;
    }

    public int getBlackViewIndex() {
        return blackViewIndex;
    }

    public int getWhiteViewIndex() {
        return whiteViewIndex;
    }
}
