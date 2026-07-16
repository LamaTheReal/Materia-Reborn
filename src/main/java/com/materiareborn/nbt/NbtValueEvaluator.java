package com.materiareborn.nbt;

import com.materiareborn.api.essence.EssenceAmount;

import java.util.Optional;

@FunctionalInterface
public interface NbtValueEvaluator {
    Optional<EssenceAmount> evaluate(NbtValueContext context);
}
