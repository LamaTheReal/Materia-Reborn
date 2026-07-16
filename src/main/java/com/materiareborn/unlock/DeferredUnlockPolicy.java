package com.materiareborn.unlock;

import com.materiareborn.api.essence.EssenceValue;
import com.materiareborn.api.item.ItemKey;
import com.materiareborn.api.item.ItemUnlockRequirement;
import com.materiareborn.api.item.MateriaItemIdentity;

import java.util.Optional;

public final class DeferredUnlockPolicy implements UnlockPolicy {
    @Override
    public Optional<ItemUnlockRequirement> requirementFor(MateriaItemIdentity identity, EssenceValue value) {
        return Optional.empty();
    }
}
