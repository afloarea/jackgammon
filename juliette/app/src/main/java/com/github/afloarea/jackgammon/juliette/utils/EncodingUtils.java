package com.github.afloarea.jackgammon.juliette.utils;

public final class EncodingUtils {

    public static String encodeHtmlContent(String content) {
        if (content == null) {
            return null;
        }
        return content
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private EncodingUtils() {
    }
}
