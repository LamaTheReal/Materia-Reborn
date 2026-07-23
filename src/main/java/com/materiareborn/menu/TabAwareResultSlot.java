package com.materiareborn.menu;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.item.ItemStack;

final class TabAwareResultSlot extends ResultSlot {
    private final CraftingContainer craftSlots;
    private final BooleanSupplier active;
    private final BiConsumer<Player, List<ItemStack>> afterCraft;

    TabAwareResultSlot(
            Player player,
            CraftingContainer craftSlots,
            Container container,
            int slot,
            int x,
            int y,
            BooleanSupplier active,
            BiConsumer<Player, List<ItemStack>> afterCraft
    ) {
        super(player, craftSlots, container, slot, x, y);
        this.craftSlots = craftSlots;
        this.active = active;
        this.afterCraft = afterCraft;
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

    @Override
    public void onTake(Player player, ItemStack result) {
        List<ItemStack> craftedPattern = new ArrayList<>(craftSlots.getContainerSize());
        for (int slot = 0; slot < craftSlots.getContainerSize(); slot++) {
            craftedPattern.add(craftSlots.getItem(slot).copy());
        }
        super.onTake(player, result);
        afterCraft.accept(player, List.copyOf(craftedPattern));
    }
}
