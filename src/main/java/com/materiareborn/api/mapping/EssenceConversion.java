package com.materiareborn.api.mapping;

import com.materiareborn.api.item.MateriaItemIdentity;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public record EssenceConversion(
        String id,
        int priority,
        MateriaItemIdentity result,
        int resultAmount,
        Map<MateriaItemIdentity, Integer> ingredients
) {
    public EssenceConversion {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Conversion id cannot be blank.");
        }
        Objects.requireNonNull(result, "result");
        Objects.requireNonNull(ingredients, "ingredients");
        if (resultAmount <= 0) {
            throw new IllegalArgumentException("Result amount must be positive.");
        }
        LinkedHashMap<MateriaItemIdentity, Integer> copy = new LinkedHashMap<>();
        ingredients.forEach((identity, amount) -> {
            Objects.requireNonNull(identity, "ingredient identity");
            if (amount == null || amount <= 0) {
                throw new IllegalArgumentException("Ingredient amount must be positive.");
            }
            copy.put(identity, amount);
        });
        ingredients = Collections.unmodifiableMap(copy);
    }
}
