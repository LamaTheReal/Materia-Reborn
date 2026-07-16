package com.materiareborn.crafting;

import com.materiareborn.api.essence.EssenceAmount;
import com.materiareborn.essence.EssenceManager;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class CraftingValueAuditor {
    private final EssenceManager essenceManager;

    public CraftingValueAuditor(EssenceManager essenceManager) {
        this.essenceManager = Objects.requireNonNull(essenceManager, "essenceManager");
    }

    public Optional<CraftingBalanceReport> audit(List<CraftingIngredient> ingredients, CraftingIngredient result) {
        Objects.requireNonNull(ingredients, "ingredients");
        Objects.requireNonNull(result, "result");

        EssenceAmount inputValue = EssenceAmount.ZERO;
        for (CraftingIngredient ingredient : ingredients) {
            Optional<EssenceAmount> quoted = essenceManager.quoteSellValue(ingredient.identity(), ingredient.amount());
            if (quoted.isEmpty()) {
                return Optional.empty();
            }
            inputValue = inputValue.plus(quoted.get());
        }

        Optional<EssenceAmount> resultValue = essenceManager.quoteSellValue(result.identity(), result.amount());
        if (resultValue.isEmpty()) {
            return Optional.empty();
        }

        boolean safe = resultValue.get().compareTo(inputValue) <= 0;
        return Optional.of(new CraftingBalanceReport(
                inputValue,
                resultValue.get(),
                safe,
                safe ? "safe" : "result_exceeds_ingredients"
        ));
    }
}
