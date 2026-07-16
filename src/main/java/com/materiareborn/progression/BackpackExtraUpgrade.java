package com.materiareborn.progression;

import com.materiareborn.config.MateriaConfig;
import net.minecraft.world.item.ItemStack;

public enum BackpackExtraUpgrade {
    STACKSIZE("stacksize", 6),
    AUTOPICKUP("autopickup", 4),
    VOID("void", 1),
    FILTER("filter", 4),
    KEEP_INVENTORY("keep_inventory", 1),
    SOULBOUND("soulbound", 1);

    private final String id;
    private final int supportedMaxLevel;

    BackpackExtraUpgrade(String id, int supportedMaxLevel) {
        this.id = id;
        this.supportedMaxLevel = supportedMaxLevel;
    }

    public String id() {
        return id;
    }

    public int maxLevel() {
        return Math.min(supportedMaxLevel, MateriaConfig.backpackUpgradeMaxLevel(id));
    }

    public boolean isEnabled() {
        return MateriaConfig.backpackUpgradeEnabled(id) && maxLevel() > 0;
    }

    public static int backpackStackLimit(int stacksizeLevel, ItemStack stack) {
        if (stack.getMaxStackSize() < 64) {
            return stack.getMaxStackSize();
        }
        return backpackStackLimit(stacksizeLevel);
    }

    public static int backpackStackLimit(int stacksizeLevel) {
        return MateriaConfig.backpackStackLimit(stacksizeLevel);
    }

    public static int maximumBackpackStackSize() {
        return MateriaConfig.maximumBackpackStackSize();
    }

    public static int autopickupRange(int level) {
        return MateriaConfig.autopickupRange(level);
    }

    public static int filterSlotCount(int level) {
        return MateriaConfig.filterSlotCount(level);
    }

    public int nextLevelCost(int currentLevel) {
        int nextLevel = currentLevel + 1;
        if (nextLevel < 1 || nextLevel > maxLevel()) {
            return 0;
        }
        return MateriaConfig.backpackUpgradeCost(id, nextLevel);
    }
}
