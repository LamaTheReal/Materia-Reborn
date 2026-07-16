package com.materiareborn.menu;

import java.util.function.BooleanSupplier;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.item.ItemStack;

final class TabAwareResultSlot extends ResultSlot {
    private final BooleanSupplier active;

    TabAwareResultSlot(Player player, CraftingContainer craftSlots, Container container, int slot, int x, int y, BooleanSupplier active) {
        super(player, craftSlots, container, slot, x, y);
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
