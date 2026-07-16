package com.materiareborn.api.knowledge;

import com.materiareborn.api.item.ItemKey;
import com.materiareborn.api.item.ItemKnowledge;
import com.materiareborn.api.item.MateriaItemIdentity;

import java.util.Optional;

public interface ItemKnowledgeRepository {
    Optional<ItemKnowledge> find(KnowledgeSubject subject, MateriaItemIdentity identity);

    default Optional<ItemKnowledge> find(KnowledgeSubject subject, ItemKey itemKey) {
        return find(subject, MateriaItemIdentity.itemOnly(itemKey));
    }

    void save(KnowledgeSubject subject, ItemKnowledge knowledge);

    KnowledgeSnapshot snapshot(KnowledgeSubject subject);

    void clear(KnowledgeSubject subject);
}
