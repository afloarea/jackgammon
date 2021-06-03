package com.github.afloarea.jackgammon.juliette.neural;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;
import java.util.StringJoiner;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class Link {
    private final String source;
    private final String target;
    private final double weight;

    @JsonCreator
    public Link(@JsonProperty("source") String source,
                @JsonProperty("target") String target,
                @JsonProperty("weight") double weight) {
        this.source = source;
        this.target = target;
        this.weight = weight;
    }

    public String getSource() {
        return source;
    }

    public String getTarget() {
        return target;
    }

    public double getWeight() {
        return weight;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Link)) return false;
        Link link = (Link) o;
        return Double.compare(link.weight, weight) == 0 && Objects.equals(source, link.source) && Objects.equals(target, link.target);
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, target, weight);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Link.class.getSimpleName() + "[", "]")
                .add("source='" + source + "'")
                .add("target='" + target + "'")
                .add("weight=" + weight)
                .toString();
    }
}
