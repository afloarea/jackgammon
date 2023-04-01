package com.github.afloarea.jackgammon.juliette.messages.server;

public record DisplayChatMessageEvent(String author, String message) implements ServerToClientEvent {
}
