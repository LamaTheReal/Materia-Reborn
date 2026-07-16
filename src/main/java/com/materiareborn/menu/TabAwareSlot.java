package com.materiareborn.menu;

import java.util.function.BooleanSupplier;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

class TabAwareSlot extends Slot {
    private final BooleanSupplier active;

    TabAwareSlot(Container container, int slot, int x, int y, BooleanSupplier active) {
        super(container, slot, x, y);
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
