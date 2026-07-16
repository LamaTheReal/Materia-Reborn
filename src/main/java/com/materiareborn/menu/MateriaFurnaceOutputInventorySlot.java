package com.materiareborn.menu;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

final class MateriaFurnaceOutputInventorySlot extends TabAwareItemHandlerSlot {
    private final Consumer<Player> onOutputTaken;

    MateriaFurnaceOutputInventorySlot(
            IItemHandler itemHandler,
            int index,
            int x,
            int y,
            BooleanSupplier active,
            Consumer<Player> onOutputTaken
    ) {
        super(itemHandler, index, x, y, active);
        this.onOutputTaken = onOutputTaken;
    }

    @Override
    public void onTake(Player player, ItemStack stack) {
        super.onTake(player, stack);
        onOutputTaken.accept(player);
    }
}
