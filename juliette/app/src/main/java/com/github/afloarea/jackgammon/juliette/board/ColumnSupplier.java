package com.github.afloarea.jackgammon.juliette.board;

public interface ColumnSupplier {

    BoardColumn getColumn(int index, Direction direction);

}
