package com.github.afloarea.jackgammon.juliette.messages.server;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = InitGameEvent.class, name = "game-init"),
        @JsonSubTypes.Type(value = PromptRollEvent.class, name = "prompt-roll"),
        @JsonSubTypes.Type(value = PromptMoveEvent.class, name = "prompt-move"),
        @JsonSubTypes.Type(value = NotifyRollEvent.class, name = "notify-roll"),
        @JsonSubTypes.Type(value = NotifyGameEndedEvent.class, name = "notify-end"),
        @JsonSubTypes.Type(value = NotifyMoveEvent.class, name = "notify-move")
})
public interface ServerToClientEvent {
}
