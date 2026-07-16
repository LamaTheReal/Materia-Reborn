package com.materiareborn.api.essence;

import java.util.Objects;

public record EssenceValue(
        EssenceAmount baseValue,
        EssenceTier tier,
        EssenceAmount sellValue,
        EssenceAmount purchaseCost
) {
    public EssenceValue {
        Objects.requireNonNull(baseValue, "baseValue");
        Objects.requireNonNull(tier, "tier");
        Objects.requireNonNull(sellValue, "sellValue");
        Objects.requireNonNull(purchaseCost, "purchaseCost");
    }

    public EssenceValue(EssenceAmount baseValue, EssenceTier tier) {
        this(baseValue, tier, tier.sellValue(baseValue), tier.purchaseCost(baseValue));
    }
}
