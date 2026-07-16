package com.materiareborn.nbt;

import com.materiareborn.api.item.ItemKey;
import com.materiareborn.api.item.MateriaItemIdentity;

import java.util.Objects;

public record NbtValueContext(MateriaItemIdentity identity, boolean containsStoredItems, boolean containsEnergy) {
    public NbtValueContext {
        Objects.requireNonNull(identity, "identity");
    }

    public static NbtValueContext itemOnly(ItemKey itemKey, boolean containsStoredItems, boolean containsEnergy) {
        return new NbtValueContext(MateriaItemIdentity.itemOnly(itemKey), containsStoredItems, containsEnergy);
    }

    public ItemKey itemKey() {
        return identity.itemKey();
    }
}
