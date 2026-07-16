package com.materiareborn.essence;

import com.materiareborn.api.essence.EssenceAmount;
import com.materiareborn.api.essence.EssenceValueProvider;
import com.materiareborn.api.item.ItemKey;
import com.materiareborn.api.item.MateriaItemIdentity;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

public final class EssenceManager {
    private final EssenceValueProvider values;

    public EssenceManager(EssenceValueProvider values) {
        this.values = Objects.requireNonNull(values, "values");
    }

    public Optional<EssenceAmount> quoteSellValue(ItemKey itemKey, int amount) {
        return quoteSellValue(MateriaItemIdentity.itemOnly(itemKey), amount);
    }

    public Optional<EssenceAmount> quoteSellValue(MateriaItemIdentity identity, int amount) {
        return values.valueFor(identity)
                .map(value -> value.sellValue().multiply(BigDecimal.valueOf(requirePositiveAmount(amount))));
    }

    public Optional<EssenceAmount> quotePurchaseCost(ItemKey itemKey, int amount) {
        return quotePurchaseCost(MateriaItemIdentity.itemOnly(itemKey), amount);
    }

    public Optional<EssenceAmount> quotePurchaseCost(MateriaItemIdentity identity, int amount) {
        return values.valueFor(identity)
                .map(value -> value.purchaseCost().multiply(BigDecimal.valueOf(requirePositiveAmount(amount))));
    }

    private static int requirePositiveAmount(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive.");
        }
        return amount;
    }
}
