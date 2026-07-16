package com.materiareborn.sync;

import com.materiareborn.api.sync.SyncGateway;
import com.materiareborn.api.sync.SyncPayload;

public enum NoopSyncGateway implements SyncGateway {
    INSTANCE;

    @Override
    public void publish(SyncPayload payload) {
    }
}
