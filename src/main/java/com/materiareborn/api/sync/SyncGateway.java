package com.materiareborn.api.sync;

@FunctionalInterface
public interface SyncGateway {
    void publish(SyncPayload payload);
}
