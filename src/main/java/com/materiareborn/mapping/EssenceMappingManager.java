package com.materiareborn.mapping;

import com.materiareborn.api.mapping.EssenceMappingSnapshot;
import com.materiareborn.api.mapping.EssenceMappingSource;
import com.materiareborn.api.sync.SyncGateway;
import com.materiareborn.essence.InMemoryEssenceCatalog;
import com.materiareborn.sync.EssenceMappingSyncPayload;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class EssenceMappingManager {
    private final InMemoryEssenceCatalog catalog;
    private final SyncGateway syncGateway;
    private final List<EssenceMappingSource> sources = new ArrayList<>();
    private EssenceMappingSnapshot snapshot = EssenceMappingSnapshot.empty();
    private long nextVersion = 1L;

    public EssenceMappingManager(InMemoryEssenceCatalog catalog, SyncGateway syncGateway) {
        this.catalog = Objects.requireNonNull(catalog, "catalog");
        this.syncGateway = Objects.requireNonNull(syncGateway, "syncGateway");
    }

    public void registerSource(EssenceMappingSource source) {
        sources.add(Objects.requireNonNull(source, "source"));
    }

    public EssenceMappingSnapshot rebuild() {
        SimpleEssenceMappingCollector collector = new SimpleEssenceMappingCollector();
        for (EssenceMappingSource source : sources) {
            source.collect(collector);
        }
        snapshot = collector.toSnapshot(nextVersion++);
        catalog.replaceAll(snapshot.fixedValues());
        syncGateway.publish(new EssenceMappingSyncPayload(snapshot));
        return snapshot;
    }

    public EssenceMappingSnapshot snapshot() {
        return snapshot;
    }

    public int sourceCount() {
        return sources.size();
    }
}
