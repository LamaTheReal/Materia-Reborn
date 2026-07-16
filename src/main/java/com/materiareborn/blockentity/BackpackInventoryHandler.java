package com.materiareborn.blockentity;

import com.materiareborn.progression.BackpackExtraUpgrade;
import java.util.function.IntSupplier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;

/** Inventory implementation used only by the Materia Table backpack storage. */
final class BackpackInventoryHandler extends ItemStackHandler {
    private final IntSupplier unlockedSlots;
    private final IntSupplier stacksizeLevel;
    private final Runnable changed;

    BackpackInventoryHandler(int slots, IntSupplier unlockedSlots, IntSupplier stacksizeLevel, Runnable changed) {
        super(slots);
        this.unlockedSlots = unlockedSlots;
        this.stacksizeLevel = stacksizeLevel;
        this.changed = changed;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (!isUnlocked(slot) || amount <= 0) {
            return ItemStack.EMPTY;
        }
        ItemStack existing = getStackInSlot(slot);
        if (existing.isEmpty()) {
            return ItemStack.EMPTY;
        }
        int extracted = Math.min(amount, existing.getCount());
        ItemStack result = existing.copyWithCount(extracted);
        if (!simulate) {
            if (extracted == existing.getCount()) {
                setStackInSlot(slot, ItemStack.EMPTY);
            } else {
                existing.shrink(extracted);
                setStackInSlot(slot, existing);
            }
        }
        return result;
    }

    @Override
    public int getSlotLimit(int slot) {
        return BackpackExtraUpgrade.maximumBackpackStackSize();
    }

    @Override
    protected int getStackLimit(int slot, ItemStack stack) {
        if (!isUnlocked(slot)) {
            return 0;
        }
        return BackpackExtraUpgrade.backpackStackLimit(stacksizeLevel.getAsInt(), stack);
    }

    @Override
    protected void onContentsChanged(int slot) {
        changed.run();
    }

    private boolean isUnlocked(int slot) {
        return slot >= 0 && slot < Math.min(getSlots(), Math.max(0, unlockedSlots.getAsInt()));
    }
}