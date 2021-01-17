package com.github.afloarea.jackgammon.juliette.message.client;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.github.afloarea.jackgammon.juliette.message.MoveMessage;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = PlayerJoinMessage.class, name = "join"),
        @JsonSubTypes.Type(value = PlayerRollMessage.class, name = "roll"),
        @JsonSubTypes.Type(value = MoveMessage.class, name = "select-move")
})
public interface ClientToServerEvent {
}
