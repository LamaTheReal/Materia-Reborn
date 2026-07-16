package com.materiareborn.api.essence;

import java.math.BigDecimal;

public enum EssenceTier {
    BASIC("basic", "1.0", "1.0"),
    COMMON("common", "0.9", "1.0"),
    RARE("rare", "0.8", "1.2"),
    ARCANE("arcane", "0.5", "1.5"),
    MYTHIC("mythic", "0.5", "3.0");

    private final String id;
    private final BigDecimal sellMultiplier;
    private final BigDecimal purchaseMultiplier;

    EssenceTier(String id, String sellMultiplier, String purchaseMultiplier) {
        this.id = id;
        this.sellMultiplier = new BigDecimal(sellMultiplier);
        this.purchaseMultiplier = new BigDecimal(purchaseMultiplier);
    }

    public String id() {
        return id;
    }

    public EssenceAmount sellValue(EssenceAmount baseValue) {
        return baseValue.multiply(sellMultiplier);
    }

    public EssenceAmount purchaseCost(EssenceAmount baseValue) {
        return baseValue.multiply(purchaseMultiplier);
    }
}
