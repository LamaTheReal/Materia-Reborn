package com.materiareborn.crafting;

import com.materiareborn.api.item.ItemKey;
import com.materiareborn.api.item.MateriaItemIdentity;

import java.util.Objects;

public record CraftingIngredient(MateriaItemIdentity identity, int amount) {
    public CraftingIngredient {
        Objects.requireNonNull(identity, "identity");
        if (amount <= 0) {
            throw new IllegalArgumentException("Ingredient amount must be positive.");
        }
    }

    public static CraftingIngredient itemOnly(ItemKey itemKey, int amount) {
        return new CraftingIngredient(MateriaItemIdentity.itemOnly(itemKey), amount);
    }

    public ItemKey itemKey() {
        return identity.itemKey();
    }
}
