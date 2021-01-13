package com.github.afloarea.jackgammon.juliette;

public class BoardColumn {

    int pieceCount;
    Color pieceColor;
    int position;

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
}
