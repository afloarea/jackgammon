package com.github.afloarea.jackgammon.juliette.board;

import com.github.afloarea.jackgammon.juliette.Color;
import com.github.afloarea.jackgammon.juliette.IllegalGameActionException;

import java.util.Objects;

public final class BoardColumn {

    private int pieceCount;
    private Color pieceColor;
    private final int position;

    public BoardColumn(int position) {
        this.position = position;
        this.pieceColor = Color.NONE;
        this.pieceCount = 0;
    }

    public BoardColumn(int pieceCount, Color pieceColor, int position) {
        this.pieceCount = pieceCount;
        this.pieceColor = pieceColor;
        this.position = position;
    }

    public int getPieceCount() {
        return pieceCount;
    }

    public Color getPieceColor() {
        return pieceColor;
    }

    public int getPosition() {
        return position;
    }

    public boolean canAccept(Color pieceColor) {
        return this.pieceCount <= 1 || this.pieceColor == pieceColor;
    }

    public boolean isEmpty() {
        return this.pieceColor == Color.NONE;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BoardColumn)) return false;
        BoardColumn that = (BoardColumn) o;
        return pieceCount == that.pieceCount && position == that.position && pieceColor == that.pieceColor;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceCount, pieceColor, position);
    }
}
