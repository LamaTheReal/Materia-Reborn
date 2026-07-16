package com.materiareborn.api.item;

import java.util.Objects;

public record MateriaItemIdentity(ItemKey itemKey, ComponentFingerprint components) {
    public MateriaItemIdentity {
        Objects.requireNonNull(itemKey, "itemKey");
        components = Objects.requireNonNullElse(components, ComponentFingerprint.empty());
    }

    public static MateriaItemIdentity itemOnly(ItemKey itemKey) {
        return new MateriaItemIdentity(itemKey, ComponentFingerprint.empty());
    }

    public static MateriaItemIdentity of(ItemKey itemKey, ComponentFingerprint components) {
        return new MateriaItemIdentity(itemKey, components);
    }

    public boolean hasComponents() {
        return !components.isEmpty();
    }

    public String stableId() {
        if (!hasComponents()) {
            return itemKey.asString();
        }
        return itemKey.asString() + "{" + components.canonicalForm() + "}";
    }

    @Override
    public String toString() {
        return stableId();
    }
}
