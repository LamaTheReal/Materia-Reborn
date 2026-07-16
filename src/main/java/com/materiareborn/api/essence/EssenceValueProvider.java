package com.materiareborn.api.essence;

import com.materiareborn.api.item.ItemKey;
import com.materiareborn.api.item.MateriaItemIdentity;

import java.util.Optional;

@FunctionalInterface
public interface EssenceValueProvider {
    Optional<EssenceValue> valueFor(MateriaItemIdentity identity);

    default Optional<EssenceValue> valueFor(ItemKey itemKey) {
        return valueFor(MateriaItemIdentity.itemOnly(itemKey));
    }
}
