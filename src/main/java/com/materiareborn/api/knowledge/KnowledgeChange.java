package com.materiareborn.api.knowledge;

import com.materiareborn.api.item.MateriaItemIdentity;

import java.util.Objects;
import java.util.Optional;

public record KnowledgeChange(
        KnowledgeSubject subject,
        KnowledgeChangeType type,
        Optional<MateriaItemIdentity> identity
) {
    public KnowledgeChange {
        Objects.requireNonNull(subject, "subject");
        Objects.requireNonNull(type, "type");
        identity = Objects.requireNonNull(identity, "identity");
    }

    public static KnowledgeChange analyzed(KnowledgeSubject subject, MateriaItemIdentity identity) {
        return new KnowledgeChange(subject, KnowledgeChangeType.ANALYZED, Optional.of(identity));
    }

    public static KnowledgeChange unlocked(KnowledgeSubject subject, MateriaItemIdentity identity) {
        return new KnowledgeChange(subject, KnowledgeChangeType.UNLOCKED, Optional.of(identity));
    }

    public static KnowledgeChange cleared(KnowledgeSubject subject) {
        return new KnowledgeChange(subject, KnowledgeChangeType.CLEARED, Optional.empty());
    }
}
