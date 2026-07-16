package com.materiareborn.api.transmutation;

import com.materiareborn.api.item.ItemKey;
import com.materiareborn.api.item.MateriaItemIdentity;
import com.materiareborn.api.table.TableTier;

import java.util.Objects;

public record TransmutationRequest(MateriaItemIdentity identity, int amount, TableTier tableTier) {
    public TransmutationRequest {
        Objects.requireNonNull(identity, "identity");
        Objects.requireNonNull(tableTier, "tableTier");
        if (amount <= 0) {
            throw new IllegalArgumentException("Transmutation amount must be positive.");
        }
    }

    public static TransmutationRequest itemOnly(ItemKey itemKey, int amount, TableTier tableTier) {
        return new TransmutationRequest(MateriaItemIdentity.itemOnly(itemKey), amount, tableTier);
    }

    public ItemKey itemKey() {
        return identity.itemKey();
    }
}
