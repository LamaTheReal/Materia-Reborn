package com.materiareborn.compat.jei;

import net.minecraft.world.item.ItemStack;

public record EssenceCondensationJeiRecipe(
        ItemStack dust,
        ItemStack crystal,
        ItemStack cauldron,
        ItemStack buddingAmethyst,
        ItemStack bottle,
        ItemStack essence
) {
}