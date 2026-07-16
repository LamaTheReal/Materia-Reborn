package com.materiareborn.sync;

import com.materiareborn.api.knowledge.KnowledgeSnapshot;
import com.materiareborn.api.sync.SyncPayload;

import java.util.Objects;

public record KnowledgeSnapshotSyncPayload(KnowledgeSnapshot snapshot) implements SyncPayload {
    public KnowledgeSnapshotSyncPayload {
        Objects.requireNonNull(snapshot, "snapshot");
    }

    @Override
    public String type() {
        return "knowledge_snapshot";
    }
}
