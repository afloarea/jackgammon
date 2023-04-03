package com.github.afloarea.jackgammon.juliette.messages.client;

import com.github.afloarea.jackgammon.juliette.utils.EncodingUtils;

public record ChatMessageEvent(String author, String message) implements ClientToServerEvent {

    public ChatMessageEvent {
        author = EncodingUtils.encodeHtmlContent(author);
        message = EncodingUtils.encodeHtmlContent(message);
    }
}
