package com.materiareborn.menu;

import java.util.function.BooleanSupplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

final class MateriaGhostFilterSlot extends TabAwareItemHandlerSlot {
    MateriaGhostFilterSlot(IItemHandler itemHandler, int index, int x, int y, BooleanSupplier active) {
        super(itemHandler, index, x, y, active);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return false;
    }

    @Override
    public boolean mayPickup(Player player) {
        return false;
    }
}
