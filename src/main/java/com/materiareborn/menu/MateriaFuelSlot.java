package com.materiareborn.menu;

import java.util.function.BooleanSupplier;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;

final class MateriaFuelSlot extends TabAwareSlot {
    MateriaFuelSlot(Container container, int slot, int x, int y, BooleanSupplier active) {
        super(container, slot, x, y, active);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return isActive() && (stack.getBurnTime(RecipeType.SMELTING) > 0 || stack.is(Items.BUCKET));
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return stack.is(Items.BUCKET) ? 1 : super.getMaxStackSize(stack);
    }
}
