package com.materiareborn.menu;

import java.util.function.BooleanSupplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

class TabAwareItemHandlerSlot extends SlotItemHandler {
    private final BooleanSupplier active;

    TabAwareItemHandlerSlot(IItemHandler itemHandler, int index, int x, int y, BooleanSupplier active) {
        super(itemHandler, index, x, y);
        this.active = active;
    }

    @Override
    public boolean isActive() {
        return active.getAsBoolean();
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return isActive() && super.mayPlace(stack);
    }

    @Override
    public boolean mayPickup(Player player) {
        return isActive() && super.mayPickup(player);
    }

    @Override
    public boolean isHighlightable() {
        return isActive();
    }
}
