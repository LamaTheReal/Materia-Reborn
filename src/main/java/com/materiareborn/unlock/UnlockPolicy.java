package com.materiareborn.unlock;

import com.materiareborn.api.essence.EssenceValue;
import com.materiareborn.api.item.ItemKey;
import com.materiareborn.api.item.ItemUnlockRequirement;
import com.materiareborn.api.item.MateriaItemIdentity;

import java.util.Optional;

@FunctionalInterface
public interface UnlockPolicy {
    Optional<ItemUnlockRequirement> requirementFor(MateriaItemIdentity identity, EssenceValue value);

    default Optional<ItemUnlockRequirement> requirementFor(ItemKey itemKey, EssenceValue value) {
        return requirementFor(MateriaItemIdentity.itemOnly(itemKey), value);
    }
}
