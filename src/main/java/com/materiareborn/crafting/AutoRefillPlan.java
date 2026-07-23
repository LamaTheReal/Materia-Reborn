package com.materiareborn.crafting;

import java.util.List;
import net.minecraft.world.item.ItemStack;

public record AutoRefillPlan(
        Status status,
        long totalCost,
        List<Purchase> purchases
) {
    public AutoRefillPlan {
        totalCost = Math.max(0L, totalCost);
        purchases = List.copyOf(purchases);
    }

    public static AutoRefillPlan ready(long totalCost, List<Purchase> purchases) {
        return new AutoRefillPlan(Status.READY, totalCost, purchases);
    }

    public static AutoRefillPlan notEnoughEssence(long totalCost) {
        return new AutoRefillPlan(Status.NOT_ENOUGH_ESSENCE, totalCost, List.of());
    }

    public static AutoRefillPlan requiredItemUnavailable() {
        return new AutoRefillPlan(Status.REQUIRED_ITEM_UNAVAILABLE, 0L, List.of());
    }

    public boolean isReady() {
        return status == Status.READY;
    }

    public enum Status {
        READY,
        NOT_ENOUGH_ESSENCE,
        REQUIRED_ITEM_UNAVAILABLE
    }

    public record Purchase(int craftingSlot, int catalogIndex, ItemStack stack) {
        public Purchase {
            if (craftingSlot < 0 || craftingSlot >= 9) {
                throw new IllegalArgumentException("Crafting slot must be between 0 and 8.");
            }
            stack = stack.copyWithCount(1);
        }

        @Override
        public ItemStack stack() {
            return stack.copy();
        }
    }
}
