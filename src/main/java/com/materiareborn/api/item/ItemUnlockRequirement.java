package com.materiareborn.api.item;

import com.materiareborn.api.essence.EssenceAmount;
import com.materiareborn.api.table.TableTier;

import java.util.Objects;

public record ItemUnlockRequirement(
        MateriaItemIdentity identity,
        int requiredItemCount,
        EssenceAmount requiredEssence,
        TableTier requiredTableTier
) {
    public ItemUnlockRequirement {
        Objects.requireNonNull(identity, "identity");
        Objects.requireNonNull(requiredEssence, "requiredEssence");
        Objects.requireNonNull(requiredTableTier, "requiredTableTier");
        if (requiredItemCount < 0) {
            throw new IllegalArgumentException("Required item count cannot be negative.");
        }
    }

    public static ItemUnlockRequirement itemOnly(
            ItemKey itemKey,
            int requiredItemCount,
            EssenceAmount requiredEssence,
            TableTier requiredTableTier
    ) {
        return new ItemUnlockRequirement(
                MateriaItemIdentity.itemOnly(itemKey),
                requiredItemCount,
                requiredEssence,
                requiredTableTier
        );
    }

    public ItemKey itemKey() {
        return identity.itemKey();
    }
}
