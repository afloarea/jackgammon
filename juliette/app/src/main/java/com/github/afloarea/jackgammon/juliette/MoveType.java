package com.github.afloarea.jackgammon.juliette;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum MoveType {
    @JsonProperty("simple") SIMPLE,
    @JsonProperty("collect") COLLECT,
    @JsonProperty("enter") ENTER
}
