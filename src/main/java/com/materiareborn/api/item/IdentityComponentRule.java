package com.materiareborn.api.item;

import java.util.Objects;

public record IdentityComponentRule(
        String componentId,
        boolean identityRelevant,
        boolean valueRelevant,
        String note
) {
    public IdentityComponentRule {
        if (componentId == null || componentId.isBlank()) {
            throw new IllegalArgumentException("Component id cannot be blank.");
        }
        note = Objects.requireNonNullElse(note, "");
    }
}
