package com.materiareborn.essence;

import com.materiareborn.api.essence.EssenceValue;
import com.materiareborn.api.essence.EssenceValueProvider;
import com.materiareborn.api.item.ItemKey;
import com.materiareborn.api.item.MateriaItemIdentity;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class InMemoryEssenceCatalog implements EssenceValueProvider {
    private final Map<MateriaItemIdentity, EssenceValue> values = new LinkedHashMap<>();

    public void register(MateriaItemIdentity identity, EssenceValue value) {
        values.put(Objects.requireNonNull(identity, "identity"), Objects.requireNonNull(value, "value"));
    }

    public void register(ItemKey itemKey, EssenceValue value) {
        register(MateriaItemIdentity.itemOnly(itemKey), value);
    }

    @Override
    public Optional<EssenceValue> valueFor(MateriaItemIdentity identity) {
        return Optional.ofNullable(values.get(identity));
    }

    public Map<MateriaItemIdentity, EssenceValue> snapshot() {
        return Collections.unmodifiableMap(values);
    }

    public void replaceAll(Map<MateriaItemIdentity, EssenceValue> replacements) {
        values.clear();
        values.putAll(replacements);
    }

    public void clear() {
        values.clear();
    }
}
