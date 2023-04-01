package com.github.afloarea.jackgammon.juliette.messages.client;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = PlayerJoinEvent.class, name = "join"),
        @JsonSubTypes.Type(value = ChatMessageEvent.class, name = "chat-message"),

        @JsonSubTypes.Type(value = PlayerRollEvent.class, name = "roll"),
        @JsonSubTypes.Type(value = SelectMoveEvent.class, name = "select-move")
})
public interface ClientToServerEvent {
}
