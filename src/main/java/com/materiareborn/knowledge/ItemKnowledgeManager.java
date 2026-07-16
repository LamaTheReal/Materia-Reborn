package com.materiareborn.knowledge;

import com.materiareborn.api.essence.EssenceValueProvider;
import com.materiareborn.api.item.ItemKey;
import com.materiareborn.api.item.ItemKnowledge;
import com.materiareborn.api.item.ItemUnlockRequirement;
import com.materiareborn.api.item.MateriaItemIdentity;
import com.materiareborn.api.knowledge.ItemKnowledgeRepository;
import com.materiareborn.api.knowledge.KnowledgeChange;
import com.materiareborn.api.knowledge.KnowledgeSnapshot;
import com.materiareborn.api.knowledge.KnowledgeSubject;
import com.materiareborn.api.sync.SyncGateway;
import com.materiareborn.sync.KnowledgeChangeSyncPayload;
import com.materiareborn.sync.KnowledgeSnapshotSyncPayload;
import com.materiareborn.unlock.UnlockPolicy;

import java.util.Optional;
import java.util.Objects;

public final class ItemKnowledgeManager {
    private final ItemKnowledgeRepository repository;
    private final UnlockPolicy unlockPolicy;
    private final EssenceValueProvider essenceValues;
    private final SyncGateway syncGateway;

    public ItemKnowledgeManager(
            ItemKnowledgeRepository repository,
            UnlockPolicy unlockPolicy,
            EssenceValueProvider essenceValues,
            SyncGateway syncGateway
    ) {
        this.repository = Objects.requireNonNull(repository, "repository");
        this.unlockPolicy = Objects.requireNonNull(unlockPolicy, "unlockPolicy");
        this.essenceValues = Objects.requireNonNull(essenceValues, "essenceValues");
        this.syncGateway = Objects.requireNonNull(syncGateway, "syncGateway");
    }

    public ItemKnowledge get(KnowledgeSubject subject, MateriaItemIdentity identity) {
        return repository.find(subject, identity).orElseGet(() -> ItemKnowledge.unknown(identity));
    }

    public ItemKnowledge get(KnowledgeSubject subject, ItemKey itemKey) {
        return get(subject, MateriaItemIdentity.itemOnly(itemKey));
    }

    public ItemKnowledge analyze(KnowledgeSubject subject, MateriaItemIdentity identity) {
        Optional<ItemUnlockRequirement> requirement = essenceValues.valueFor(identity)
                .flatMap(value -> unlockPolicy.requirementFor(identity, value));
        ItemKnowledge knowledge = ItemKnowledge.analyzed(identity, requirement);
        repository.save(subject, knowledge);
        syncGateway.publish(new KnowledgeChangeSyncPayload(KnowledgeChange.analyzed(subject, identity)));
        return knowledge;
    }

    public ItemKnowledge analyze(KnowledgeSubject subject, ItemKey itemKey) {
        return analyze(subject, MateriaItemIdentity.itemOnly(itemKey));
    }

    public ItemKnowledge unlock(KnowledgeSubject subject, MateriaItemIdentity identity) {
        ItemKnowledge knowledge = ItemKnowledge.unlocked(identity);
        repository.save(subject, knowledge);
        syncGateway.publish(new KnowledgeChangeSyncPayload(KnowledgeChange.unlocked(subject, identity)));
        return knowledge;
    }

    public ItemKnowledge unlock(KnowledgeSubject subject, ItemKey itemKey) {
        return unlock(subject, MateriaItemIdentity.itemOnly(itemKey));
    }

    public KnowledgeSnapshot snapshot(KnowledgeSubject subject) {
        KnowledgeSnapshot snapshot = repository.snapshot(subject);
        syncGateway.publish(new KnowledgeSnapshotSyncPayload(snapshot));
        return snapshot;
    }

    public void clear(KnowledgeSubject subject) {
        repository.clear(subject);
        syncGateway.publish(new KnowledgeChangeSyncPayload(KnowledgeChange.cleared(subject)));
    }
}
