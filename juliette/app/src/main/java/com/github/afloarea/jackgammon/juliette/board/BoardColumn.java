package com.github.afloarea.jackgammon.juliette.board;

import com.github.afloarea.jackgammon.juliette.Color;
import com.github.afloarea.jackgammon.juliette.IllegalGameActionException;

import java.util.Objects;

public final class BoardColumn {

    private int pieceCount;
    private Color pieceColor;
    private final String id;

    public BoardColumn(String id) {
        this.id = id;
        this.pieceColor = Color.NONE;
        this.pieceCount = 0;
    }

    public BoardColumn(int pieceCount, Color pieceColor, String id) {
        this.pieceCount = pieceCount;
        this.pieceColor = pieceCount == 0 ? Color.NONE : pieceColor;
        this.id = id;
    }

    public BoardColumn(int pieceCount, Direction elementDirection, String id) {
        this(pieceCount, Constants.COLORS_BY_DIRECTION.get(elementDirection), id);
    }

    public int getPieceCount() {
        return pieceCount;
    }

    public Color getPieceColor() {
        return pieceColor;
    }

    public String getId() {
        return id;
    }

    public boolean canAccept(Color pieceColor) {
        return this.pieceCount <= 1 || this.pieceColor == pieceColor;
    }

    public boolean isEmpty() {
        return this.pieceColor == Color.NONE;
    }

    public void removeElement() {
        removePiece();
    }

    public void removePiece() {
        pieceCount--;
        if (pieceCount == 0) {
            pieceColor = Color.NONE;
        }
        if (pieceCount < 0) {
            pieceCount = 0;
            throw new IllegalStateException("Cannot remove non-existing pieces");
        }
    }

    public void addPiece(Color color) {
        if (pieceCount == 0) {
            this.pieceColor = color;
            pieceCount++;
            return;
        }
        if (color != pieceColor) {
            throw new IllegalGameActionException("Cannot add piece of color " + color + "! Current color: " + pieceColor);
        }
        pieceCount++;
    }

    public void addElement(Direction elementDirection) {
        addPiece(Constants.COLORS_BY_DIRECTION.get(elementDirection));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BoardColumn)) return false;
        BoardColumn that = (BoardColumn) o;
        return pieceCount == that.pieceCount && id.equals(that.id) && pieceColor == that.pieceColor;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceCount, pieceColor, id);
    }

    public Direction getMovingDirectionOfElements() {
        return Constants.DIRECTION_BY_COLOR.get(pieceColor);
    }

    public boolean isClearForDirection(Direction direction) {
        return canAccept(Constants.COLORS_BY_DIRECTION.get(direction));
    }
}
