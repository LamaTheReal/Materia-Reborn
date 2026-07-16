package com.materiareborn.nbt;

import com.materiareborn.api.essence.EssenceAmount;

import java.util.Optional;

public interface NbtValuePolicy extends NbtValueEvaluator {
    static NbtValuePolicy deferred() {
        return context -> Optional.empty();
    }

    default EssenceAmount surcharge(EssenceAmount baseValue) {
        return baseValue;
    }
}
