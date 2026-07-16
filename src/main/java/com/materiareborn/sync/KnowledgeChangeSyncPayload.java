package com.materiareborn.sync;

import com.materiareborn.api.knowledge.KnowledgeChange;
import com.materiareborn.api.sync.SyncPayload;

import java.util.Objects;

public record KnowledgeChangeSyncPayload(KnowledgeChange change) implements SyncPayload {
    public KnowledgeChangeSyncPayload {
        Objects.requireNonNull(change, "change");
    }

    @Override
    public String type() {
        return "knowledge_change";
    }
}
