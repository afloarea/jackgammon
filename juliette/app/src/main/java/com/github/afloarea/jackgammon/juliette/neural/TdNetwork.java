package com.github.afloarea.jackgammon.juliette.neural;

import io.vertx.core.json.JsonObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class TdNetwork implements NeuralNetwork {

    private final int inputSize;
    private final Map<String, Double> outputsById;
    private final Map<String, List<Link>> linksByTarget;
    private final String outputId;

    private TdNetwork(int inputSize, Map<String, Double> outputsById, Map<String, List<Link>> linksByTarget) {
        this.inputSize = inputSize;
        this.outputsById = outputsById;
        this.linksByTarget = linksByTarget;
        this.outputId = outputsById.keySet().stream().skip(outputsById.size() - 1).findFirst().orElseThrow();
    }

    @Override
    public double compute(double[] inputs) {
        // set inputs
        final var inputEntries = outputsById.keySet().stream().limit(inputSize).toArray(String[]::new);
        for (int index = 0; index < inputSize; index++) {
            outputsById.put(inputEntries[index], inputs[index]);
        }

        // fire
        outputsById.entrySet().stream().skip(inputSize + 1).forEach(neuronEntry -> {
            final var inputLinks = linksByTarget.get(neuronEntry.getKey());
            final var weightedSum = inputLinks.stream()
                    .mapToDouble(link -> link.weight() * outputsById.get(link.source()))
                    .sum();
            neuronEntry.setValue(activate(weightedSum));
        });

        // get output
        return outputsById.get(outputId);
    }

    private static double activate(double x) {
        return 1 / (1 + Math.exp(-x));
    }

    public static TdNetwork importResource(String resource) {
        resource = resource.startsWith("/") ? resource : "/" + resource;

        final var inputStream = TdNetwork.class.getResourceAsStream(resource);
        final String resourceContent;
        try (inputStream) {
            resourceContent = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalArgumentException("Invalid resource");
        }

        final var json = new JsonObject(resourceContent);

        final var outputsById = new LinkedHashMap<String, Double>();

        // inputs
        final var inputs = json.getJsonArray("inputs");
        final var inputSize = inputs.size();
        inputs.stream().map(String.class::cast).forEach(id -> outputsById.put(id, 0D));

        // add bias
        outputsById.put("bias", 1D);

        // hidden
        json.getJsonArray("hidden").getJsonArray(0).stream()
                .map(JsonObject.class::cast)
                .map(hiddenEntry -> hiddenEntry.getString("id"))
                .forEach(hiddenId -> outputsById.put(hiddenId, 0D));

        // output
        final var outputId = json.getJsonObject("output").getString("id");
        outputsById.put(outputId, 0D);

        // links
        final var groupedLinksByTarget = json.getJsonArray("connections").stream()
                .map(JsonObject.class::cast)
                .map(jsonEntry -> jsonEntry.mapTo(Link.class))
                .collect(Collectors.groupingBy(Link::target));


        return new TdNetwork(inputSize, outputsById, groupedLinksByTarget);
    }
}
