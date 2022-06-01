package com.github.afloarea.jackgammon.juliette.verticles;

import java.util.Set;

public record RegistrationInfo(String address, Set<String> clientIds) {

    public RegistrationInfo {
        clientIds = Set.copyOf(clientIds);
    }
}
