package com.materiareborn.menu;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.FurnaceResultSlot;
import net.minecraft.world.item.ItemStack;

final class MateriaFurnaceResultSlot extends FurnaceResultSlot {
    private final BooleanSupplier active;
    private final Consumer<Player> onOutputTaken;

    MateriaFurnaceResultSlot(Player player, Container container, int slot, int x, int y, BooleanSupplier active, Consumer<Player> onOutputTaken) {
        super(player, container, slot, x, y);
        this.active = active;
        this.onOutputTaken = onOutputTaken;
    }

    @Override
    public boolean isActive() {
        return active.getAsBoolean();
    }

    @Override
    public boolean mayPickup(Player player) {
        return isActive() && super.mayPickup(player);
    }

    @Override
    public void onTake(Player player, ItemStack stack) {
        super.onTake(player, stack);
        onOutputTaken.accept(player);
    }

    @Override
    public boolean isHighlightable() {
        return isActive();
    }
}
