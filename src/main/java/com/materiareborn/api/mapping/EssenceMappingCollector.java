package com.materiareborn.api.mapping;

import com.materiareborn.api.essence.EssenceValue;
import com.materiareborn.api.item.ItemKey;
import com.materiareborn.api.item.MateriaItemIdentity;

public interface EssenceMappingCollector {
    void setFixedValue(MateriaItemIdentity identity, EssenceValue value);

    default void setFixedValue(ItemKey itemKey, EssenceValue value) {
        setFixedValue(MateriaItemIdentity.itemOnly(itemKey), value);
    }

    void addConversion(EssenceConversion conversion);
}
