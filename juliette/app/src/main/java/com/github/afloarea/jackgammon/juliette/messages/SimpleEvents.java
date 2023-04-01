package com.github.afloarea.jackgammon.juliette.messages;

import com.github.afloarea.jackgammon.juliette.messages.client.PlayerRollEvent;
import com.github.afloarea.jackgammon.juliette.messages.server.PromptRollEvent;

public final class SimpleEvents {
    public static final DisconnectEvent DISCONNECT = new DisconnectEvent();
    public static final PlayerRollEvent PLAYER_ROLL = new PlayerRollEvent();
    public static final PromptRollEvent PROMPT_ROLL = new PromptRollEvent();

    private SimpleEvents() {
    }
}
