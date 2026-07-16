package com.materiareborn.mapping;

import com.materiareborn.api.essence.EssenceValue;
import com.materiareborn.api.item.MateriaItemIdentity;
import com.materiareborn.api.mapping.EssenceConversion;
import com.materiareborn.api.mapping.EssenceMappingCollector;
import com.materiareborn.api.mapping.EssenceMappingSnapshot;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class SimpleEssenceMappingCollector implements EssenceMappingCollector {
    private final Map<MateriaItemIdentity, EssenceValue> fixedValues = new LinkedHashMap<>();
    private final List<EssenceConversion> conversions = new ArrayList<>();

    @Override
    public void setFixedValue(MateriaItemIdentity identity, EssenceValue value) {
        fixedValues.put(Objects.requireNonNull(identity, "identity"), Objects.requireNonNull(value, "value"));
    }

    @Override
    public void addConversion(EssenceConversion conversion) {
        conversions.add(Objects.requireNonNull(conversion, "conversion"));
    }

    public EssenceMappingSnapshot toSnapshot(long version) {
        return new EssenceMappingSnapshot(version, fixedValues, conversions);
    }
}
