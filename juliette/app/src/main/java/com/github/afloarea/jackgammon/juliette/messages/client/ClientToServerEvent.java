package com.github.afloarea.jackgammon.juliette.messages.client;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = PlayerJoinMessage.class, name = "join"),
        @JsonSubTypes.Type(value = PlayerRollMessage.class, name = "roll"),
        @JsonSubTypes.Type(value = SelectMoveMessage.class, name = "select-move")
})
public interface ClientToServerEvent {
}
