package com.materiareborn.api.item;

import java.util.Objects;
import java.util.Optional;

public record ItemKnowledge(
        MateriaItemIdentity identity,
        ItemKnowledgeState state,
        Optional<ItemUnlockRequirement> requirement
) {
    public ItemKnowledge {
        Objects.requireNonNull(identity, "identity");
        Objects.requireNonNull(state, "state");
        requirement = Objects.requireNonNull(requirement, "requirement");
    }

    public static ItemKnowledge unknown(MateriaItemIdentity identity) {
        return new ItemKnowledge(identity, ItemKnowledgeState.UNKNOWN, Optional.empty());
    }

    public static ItemKnowledge unknown(ItemKey itemKey) {
        return unknown(MateriaItemIdentity.itemOnly(itemKey));
    }

    public static ItemKnowledge analyzed(MateriaItemIdentity identity, Optional<ItemUnlockRequirement> requirement) {
        return new ItemKnowledge(identity, ItemKnowledgeState.ANALYZED, requirement);
    }

    public static ItemKnowledge analyzed(ItemKey itemKey, Optional<ItemUnlockRequirement> requirement) {
        return analyzed(MateriaItemIdentity.itemOnly(itemKey), requirement);
    }

    public static ItemKnowledge unlocked(MateriaItemIdentity identity) {
        return new ItemKnowledge(identity, ItemKnowledgeState.UNLOCKED, Optional.empty());
    }

    public static ItemKnowledge unlocked(ItemKey itemKey) {
        return unlocked(MateriaItemIdentity.itemOnly(itemKey));
    }

    public ItemKey itemKey() {
        return identity.itemKey();
    }

    public boolean isUnlocked() {
        return state == ItemKnowledgeState.UNLOCKED;
    }
}
