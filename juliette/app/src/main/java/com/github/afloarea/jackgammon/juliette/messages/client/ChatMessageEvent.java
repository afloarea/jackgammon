package com.github.afloarea.jackgammon.juliette.messages.client;

//import org.owasp.encoder.Encode;

public record ChatMessageEvent(String author, String message) implements ClientToServerEvent {

//    public ChatMessageEvent {
//        author = Encode.forHtmlContent(author);
//        message = Encode.forHtmlContent(message);
//    }
}
