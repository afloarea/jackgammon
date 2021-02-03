package com.github.afloarea.jackgammon.juliette.verticles;

import java.util.Objects;
import java.util.Set;

public final class RegistrationInfo {
    private final String address;
    private final Set<String> clientIds;

    public RegistrationInfo(String address, Set<String> clientIds) {
        this.address = address;
        this.clientIds = Set.copyOf(clientIds);
    }

    public String getAddress() {
        return address;
    }

    public Set<String> getClientIds() {
        return clientIds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RegistrationInfo that = (RegistrationInfo) o;
        return address.equals(that.address) && clientIds.equals(that.clientIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(address, clientIds);
    }

    @Override
    public String toString() {
        return "RegistrationInfo{" +
                "address='" + address + '\'' +
                ", clientIds=" + clientIds +
                '}';
    }
}
