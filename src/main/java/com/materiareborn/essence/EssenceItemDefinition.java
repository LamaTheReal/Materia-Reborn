package com.materiareborn.essence;

import com.materiareborn.api.essence.EssenceAmount;
import com.materiareborn.api.essence.EssenceTier;
import com.materiareborn.api.essence.EssenceValue;
import com.materiareborn.api.item.ItemKey;
import com.materiareborn.api.item.MateriaItemIdentity;
import java.util.Objects;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public record EssenceItemDefinition(
        ItemKey itemKey,
        Item item,
        EssenceItemVariant variant,
        EssenceTier tier,
        int tableLevel,
        String category,
        long baseValue,
        long sellValue,
        long purchaseCost,
        int requiredAnalysis
) {
    public EssenceItemDefinition {
        Objects.requireNonNull(itemKey, "itemKey");
        Objects.requireNonNull(item, "item");
        Objects.requireNonNull(variant, "variant");
        Objects.requireNonNull(tier, "tier");
        Objects.requireNonNull(category, "category");
        if (tableLevel < 1) {
            throw new IllegalArgumentException("Table level must be positive.");
        }
        if (baseValue <= 0L || sellValue <= 0L || purchaseCost <= 0L) {
            throw new IllegalArgumentException("Essence values must be positive.");
        }
        if (requiredAnalysis <= 0) {
            throw new IllegalArgumentException("Required analysis must be positive.");
        }
    }

    public EssenceValue mappingValue() {
        return new EssenceValue(
                EssenceAmount.of(baseValue),
                tier,
                EssenceAmount.of(sellValue),
                EssenceAmount.of(purchaseCost)
        );
    }

    public String catalogId() {
        return itemKey.asString() + variant.suffix();
    }

    public MateriaItemIdentity identity() {
        return MateriaItemIdentity.of(itemKey, variant.fingerprint());
    }

    public boolean matches(ItemStack stack) {
        return !stack.isEmpty() && stack.is(item) && variant.matches(stack);
    }

    public ItemStack createStack(HolderLookup.Provider registries) {
        ItemStack stack = new ItemStack(item);
        variant.apply(stack, registries);
        return stack;
    }
}