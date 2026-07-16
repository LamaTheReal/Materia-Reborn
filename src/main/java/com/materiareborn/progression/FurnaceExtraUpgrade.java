package com.materiareborn.progression;

import com.materiareborn.config.MateriaConfig;

public enum FurnaceExtraUpgrade {
    SPEED("speed", 30),
    BATCH_SMELTING("batch_smelting", 8),
    XP_STORAGE("xp_storage", 1),
    ESSENCE_GENERATOR("essence_generator", 20);

    private final String id;
    private final int supportedMaxLevel;

    FurnaceExtraUpgrade(String id, int supportedMaxLevel) {
        this.id = id;
        this.supportedMaxLevel = supportedMaxLevel;
    }

    public String id() {
        return id;
    }

    public int maxLevel() {
        return Math.min(supportedMaxLevel, MateriaConfig.furnaceUpgradeMaxLevel(id));
    }

    public boolean isAvailableForPurchase() {
        return MateriaConfig.furnaceUpgradeEnabled(id) && maxLevel() > 0;
    }

    public int nextLevelCost(int currentLevel) {
        int nextLevel = currentLevel + 1;
        if (nextLevel < 1 || nextLevel > maxLevel() || !isAvailableForPurchase()) {
            return 0;
        }
        return MateriaConfig.furnaceUpgradeCost(id, nextLevel);
    }
}
