package com.materiareborn.crafting;

import com.materiareborn.api.essence.EssenceAmount;

import java.util.Objects;

public record CraftingBalanceReport(
        EssenceAmount ingredientSellValue,
        EssenceAmount resultSellValue,
        boolean safe,
        String reason
) {
    public CraftingBalanceReport {
        Objects.requireNonNull(ingredientSellValue, "ingredientSellValue");
        Objects.requireNonNull(resultSellValue, "resultSellValue");
        Objects.requireNonNull(reason, "reason");
    }
}
