package com.materiareborn.api.storage;

import com.materiareborn.api.essence.EssenceAmount;

public interface EssenceStorage {
    EssenceAmount stored();

    EssenceAmount capacity();

    default EssenceAmount freeSpace() {
        return capacity().minus(stored());
    }

    default boolean canExtract(EssenceAmount amount) {
        return stored().compareTo(amount) >= 0;
    }

    default boolean canReceive(EssenceAmount amount) {
        return freeSpace().compareTo(amount) >= 0;
    }
}
