package com.github.afloarea.jackgammon.juliette.board;

import java.util.Objects;
import java.util.StringJoiner;

public final class BgMove {
    private final String source;
    private final String target;

    private BgMove(String source, String target) {
        this.source = source;
        this.target = target;
    }

    public static BgMove of(String source, String target) {
        Objects.requireNonNull(source);
        Objects.requireNonNull(target);
        return new BgMove(source, target);
    }

    public String getSource() {
        return source;
    }

    public String getTarget() {
        return target;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BgMove)) return false;
        BgMove bgMove = (BgMove) o;
        return source.equals(bgMove.source) && target.equals(bgMove.target);
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, target);
    }

    @Override
    public String toString() {
        return new StringJoiner(",", "M{", "}")
                .add("s=" + source)
                .add("t=" + target)
                .toString();
    }
}
