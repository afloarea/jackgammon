package com.github.afloarea.jackgammon.juliette.neural;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Link(String source, String target, double weight) {
}
