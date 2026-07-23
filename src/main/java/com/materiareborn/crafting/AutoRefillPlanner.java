package com.materiareborn.crafting;

import com.materiareborn.essence.EssenceItemCatalog;
import com.materiareborn.essence.EssenceItemDefinition;
import com.materiareborn.menu.MateriaTableMenu;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class AutoRefillPlanner {
    private static final int CRAFTING_GRID_SIZE = 9;

    private AutoRefillPlanner() {
    }

    public static AutoRefillPlan evaluate(
            MateriaTableMenu menu,
            Player player,
            List<ItemStack> craftedPattern
    ) {
        if (craftedPattern.size() != CRAFTING_GRID_SIZE) {
            return AutoRefillPlan.requiredItemUnavailable();
        }

        List<AutoRefillPlan.Purchase> purchases = new ArrayList<>();
        long totalCost = 0L;
        for (int slot = 0; slot < CRAFTING_GRID_SIZE; slot++) {
            ItemStack consumedIngredient = craftedPattern.get(slot);
            if (consumedIngredient.isEmpty()) {
                continue;
            }

            int catalogIndex = EssenceItemCatalog.indexOf(consumedIngredient);
            if (!menu.isEssenceItemAvailable(catalogIndex)
                    || !menu.isEssenceItemUnlocked(catalogIndex)) {
                return AutoRefillPlan.requiredItemUnavailable();
            }

            EssenceItemDefinition definition = EssenceItemCatalog.get(catalogIndex);
            ItemStack refillStack = definition.createStack(player.level().registryAccess()).copyWithCount(1);
            if (!ItemStack.isSameItemSameComponents(consumedIngredient, refillStack)
                    || !canInsert(menu.craftingInputItem(slot), refillStack)) {
                return AutoRefillPlan.requiredItemUnavailable();
            }

            purchases.add(new AutoRefillPlan.Purchase(slot, catalogIndex, refillStack));
            totalCost = safeAdd(totalCost, definition.purchaseCost());
        }

        if (purchases.isEmpty()) {
            return AutoRefillPlan.requiredItemUnavailable();
        }
        if (menu.displayedEssenceValue() < totalCost) {
            return AutoRefillPlan.notEnoughEssence(totalCost);
        }
        return AutoRefillPlan.ready(totalCost, purchases);
    }

    private static boolean canInsert(ItemStack current, ItemStack refill) {
        if (current.isEmpty()) {
            return true;
        }
        return ItemStack.isSameItemSameComponents(current, refill)
                && current.getCount() < refill.getMaxStackSize();
    }

    private static long safeAdd(long left, long right) {
        return Long.MAX_VALUE - left < right ? Long.MAX_VALUE : left + right;
    }
}
