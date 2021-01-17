package com.github.afloarea.jackgammon.juliette.message.client;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = PlayerJoinMessage.class, name = "join"),
        @JsonSubTypes.Type(value = PlayerRollMessage.class, name = "roll")
})
public interface ClientToServerEvent {
}
