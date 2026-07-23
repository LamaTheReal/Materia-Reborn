package com.materiareborn.api.essence;

import java.math.BigDecimal;

public enum EssenceTier {
    BASIC("basic", "1.0", "1.15"),
    COMMON("common", "0.9", "1.2"),
    RARE("rare", "0.8", "1.3"),
    ARCANE("arcane", "0.6", "1.5"),
    MYTHIC("mythic", "0.5", "2.0");

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

    public BigDecimal sellMultiplier() {
        return sellMultiplier;
    }

    public BigDecimal purchaseMultiplier() {
        return purchaseMultiplier;
    }

    public EssenceAmount sellValue(EssenceAmount baseValue) {
        return baseValue.multiply(sellMultiplier);
    }

    public EssenceAmount purchaseCost(EssenceAmount baseValue) {
        return baseValue.multiply(purchaseMultiplier);
    }
}
