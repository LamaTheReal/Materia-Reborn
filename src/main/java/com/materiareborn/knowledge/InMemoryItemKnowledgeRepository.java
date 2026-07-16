package com.materiareborn.knowledge;

import com.materiareborn.api.item.ItemKey;
import com.materiareborn.api.item.ItemKnowledge;
import com.materiareborn.api.item.MateriaItemIdentity;
import com.materiareborn.api.knowledge.ItemKnowledgeRepository;
import com.materiareborn.api.knowledge.KnowledgeSnapshot;
import com.materiareborn.api.knowledge.KnowledgeSubject;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class InMemoryItemKnowledgeRepository implements ItemKnowledgeRepository {
    private final Map<KnowledgeSubject, Map<MateriaItemIdentity, ItemKnowledge>> knowledge = new ConcurrentHashMap<>();

    @Override
    public Optional<ItemKnowledge> find(KnowledgeSubject subject, MateriaItemIdentity identity) {
        return Optional.ofNullable(knowledge.get(subject))
                .map(items -> items.get(identity));
    }

    @Override
    public void save(KnowledgeSubject subject, ItemKnowledge itemKnowledge) {
        knowledge.computeIfAbsent(subject, ignored -> new ConcurrentHashMap<>())
                .put(itemKnowledge.identity(), itemKnowledge);
    }

    @Override
    public KnowledgeSnapshot snapshot(KnowledgeSubject subject) {
        Map<MateriaItemIdentity, ItemKnowledge> entries = knowledge.get(subject);
        if (entries == null) {
            return KnowledgeSnapshot.empty(subject);
        }
        return new KnowledgeSnapshot(subject, entries);
    }

    @Override
    public void clear(KnowledgeSubject subject) {
        knowledge.remove(subject);
    }
}
