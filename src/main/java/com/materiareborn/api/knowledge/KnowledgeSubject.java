package com.materiareborn.api.knowledge;

import java.util.Objects;
import java.util.UUID;

public record KnowledgeSubject(String type, String id) {
    public KnowledgeSubject {
        type = requirePart(type, "type");
        id = requirePart(id, "id");
    }

    public static KnowledgeSubject player(UUID playerId) {
        return new KnowledgeSubject("player", playerId.toString());
    }

    public static KnowledgeSubject global() {
        return new KnowledgeSubject("global", "default");
    }

    private static String requirePart(String value, String name) {
        Objects.requireNonNull(value, name);
        if (value.isBlank()) {
            throw new IllegalArgumentException("Knowledge subject " + name + " cannot be blank.");
        }
        return value;
    }
}
