package com.materiareborn.api.knowledge;

import com.materiareborn.api.item.ItemKnowledge;
import com.materiareborn.api.item.MateriaItemIdentity;

import java.util.Map;
import java.util.Objects;

public record KnowledgeSnapshot(
        KnowledgeSubject subject,
        Map<MateriaItemIdentity, ItemKnowledge> entries
) {
    public KnowledgeSnapshot {
        Objects.requireNonNull(subject, "subject");
        entries = Map.copyOf(Objects.requireNonNull(entries, "entries"));
    }

    public static KnowledgeSnapshot empty(KnowledgeSubject subject) {
        return new KnowledgeSnapshot(subject, Map.of());
    }
}
