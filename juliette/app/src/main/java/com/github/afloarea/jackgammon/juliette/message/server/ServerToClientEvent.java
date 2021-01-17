package com.github.afloarea.jackgammon.juliette.message.server;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = InitGameMessage.class, name = "game-init"),
        @JsonSubTypes.Type(value = PromptRollMessage.class, name = "prompt-roll"),
        @JsonSubTypes.Type(value = PromptMoveMessage.class, name = "prompt-move"),
        @JsonSubTypes.Type(value = NotifyRollMessage.class, name = "notify-roll"),
        @JsonSubTypes.Type(value = NotifyGameEndedMessage.class, name = "notify-end")
})
public interface ServerToClientEvent {
}
