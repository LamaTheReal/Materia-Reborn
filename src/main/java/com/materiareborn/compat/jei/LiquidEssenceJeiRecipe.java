package com.materiareborn.compat.jei;

import net.minecraft.world.item.ItemStack;

public record LiquidEssenceJeiRecipe(
        ItemStack essence,
        ItemStack crystal,
        ItemStack waterSource,
        ItemStack liquidEssence
) {
}
