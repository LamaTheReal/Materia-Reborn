package com.materiareborn.menu;

import java.util.function.BooleanSupplier;
import java.util.function.ToIntFunction;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

final class BackpackItemHandlerSlot extends TabAwareItemHandlerSlot {
    private final ToIntFunction<ItemStack> stackLimit;

    BackpackItemHandlerSlot(IItemHandler itemHandler, int index, int x, int y, BooleanSupplier active, ToIntFunction<ItemStack> stackLimit) {
        super(itemHandler, index, x, y, active);
        this.stackLimit = stackLimit;
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return stackLimit.applyAsInt(stack);
    }
}