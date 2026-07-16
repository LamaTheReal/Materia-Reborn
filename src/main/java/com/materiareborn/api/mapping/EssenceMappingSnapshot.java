package com.materiareborn.api.mapping;

import com.materiareborn.api.essence.EssenceValue;
import com.materiareborn.api.item.MateriaItemIdentity;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public record EssenceMappingSnapshot(
        long version,
        Map<MateriaItemIdentity, EssenceValue> fixedValues,
        List<EssenceConversion> conversions
) {
    public EssenceMappingSnapshot {
        fixedValues = Map.copyOf(Objects.requireNonNull(fixedValues, "fixedValues"));
        conversions = List.copyOf(Objects.requireNonNull(conversions, "conversions"));
    }

    public static EssenceMappingSnapshot empty() {
        return new EssenceMappingSnapshot(0L, Map.of(), List.of());
    }
}
