package com.example.cqrs.services;

import java.util.UUID;

public class LocalData {

    private final UUID uuid;

    public LocalData() {
        uuid = UUID.randomUUID();
    }

    public UUID getUuid() {
        return uuid;
    }
}
