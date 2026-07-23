package com.materiareborn.directcraft;

import java.util.List;
import net.minecraft.world.item.ItemStack;

public record DirectCraftPlan(
        Status status,
        long totalCost,
        long missingEssence,
        String itemId,
        int requiredTableLevel,
        int blockedSlot,
        List<Purchase> purchases
) {
    public DirectCraftPlan {
        totalCost = Math.max(0L, totalCost);
        missingEssence = Math.max(0L, missingEssence);
        itemId = itemId == null ? "" : itemId;
        requiredTableLevel = Math.max(0, requiredTableLevel);
        purchases = List.copyOf(purchases);
    }

    public static DirectCraftPlan ready(long totalCost, List<Purchase> purchases) {
        return new DirectCraftPlan(Status.READY, totalCost, 0L, "", 0, -1, purchases);
    }

    public static DirectCraftPlan notEnoughEssence(long totalCost, long missingEssence) {
        return new DirectCraftPlan(
                Status.NOT_ENOUGH_ESSENCE,
                totalCost,
                missingEssence,
                "",
                0,
                -1,
                List.of()
        );
    }

    public static DirectCraftPlan itemNotUnlocked(String itemId) {
        return problem(Status.ITEM_NOT_UNLOCKED, itemId, 0, -1);
    }

    public static DirectCraftPlan itemUnavailable(String itemId) {
        return problem(Status.ITEM_UNAVAILABLE, itemId, 0, -1);
    }

    public static DirectCraftPlan requiresTableLevel(String itemId, int tableLevel) {
        return problem(Status.REQUIRES_TABLE_LEVEL, itemId, tableLevel, -1);
    }

    public static DirectCraftPlan blockedSlot(int slot) {
        return problem(Status.CRAFTING_SLOT_BLOCKED, "", 0, slot);
    }

    public static DirectCraftPlan unsupportedRecipe() {
        return problem(Status.UNSUPPORTED_RECIPE, "", 0, -1);
    }

    private static DirectCraftPlan problem(Status status, String itemId, int tableLevel, int slot) {
        return new DirectCraftPlan(status, 0L, 0L, itemId, tableLevel, slot, List.of());
    }

    public boolean isReady() {
        return status == Status.READY;
    }

    public enum Status {
        READY,
        NOT_ENOUGH_ESSENCE,
        ITEM_NOT_UNLOCKED,
        ITEM_UNAVAILABLE,
        REQUIRES_TABLE_LEVEL,
        CRAFTING_SLOT_BLOCKED,
        UNSUPPORTED_RECIPE
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
