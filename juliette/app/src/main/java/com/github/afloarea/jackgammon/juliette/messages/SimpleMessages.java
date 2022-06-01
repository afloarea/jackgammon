package com.github.afloarea.jackgammon.juliette.messages;

import com.github.afloarea.jackgammon.juliette.messages.client.PlayerRollMessage;
import com.github.afloarea.jackgammon.juliette.messages.server.PromptRollMessage;

public final class SimpleMessages {
    public static final DisconnectMessage DISCONNECT_MESSAGE = new DisconnectMessage();
    public static final PlayerRollMessage PLAYER_ROLL_MESSAGE = new PlayerRollMessage();
    public static final PromptRollMessage PROMPT_ROLL_MESSAGE = new PromptRollMessage();

    private SimpleMessages() {
    }
}
