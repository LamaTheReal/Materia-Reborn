package com.materiareborn.sync;

import com.materiareborn.api.mapping.EssenceMappingSnapshot;
import com.materiareborn.api.sync.SyncPayload;

import java.util.Objects;

public record EssenceMappingSyncPayload(EssenceMappingSnapshot snapshot) implements SyncPayload {
    public EssenceMappingSyncPayload {
        Objects.requireNonNull(snapshot, "snapshot");
    }

    @Override
    public String type() {
        return "essence_mapping";
    }
}
