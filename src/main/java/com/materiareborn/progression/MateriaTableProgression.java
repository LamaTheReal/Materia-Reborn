package com.materiareborn.progression;

import com.materiareborn.config.MateriaConfig;
import com.materiareborn.registry.ModBlocks;
import net.minecraft.world.level.block.Block;

public final class MateriaTableProgression {
    private MateriaTableProgression() {
    }

    public static int tableTier(Block block) {
        if (block == ModBlocks.MATERIA_TABLE_4.get()) {
            return 4;
        }
        if (block == ModBlocks.MATERIA_TABLE_3.get()) {
            return 3;
        }
        if (block == ModBlocks.MATERIA_TABLE_2.get()) {
            return 2;
        }
        return 1;
    }

    public static int maxStorageSlots(int tableTier) {
        return Math.min(
                PlayerMateriaProgress.MAX_STORAGE_SLOTS,
                Math.max(MateriaConfig.initialBackpackSlots(), MateriaConfig.backpackSlotsForTier(clampTier(tableTier)))
        );
    }

    public static int maxFurnaceSlotsPerSide(int tableTier) {
        return Math.min(
                PlayerMateriaProgress.MAX_FURNACE_SLOTS_PER_SIDE,
                Math.max(
                        MateriaConfig.initialFurnaceSlotsPerSide(),
                        MateriaConfig.furnaceSlotsPerSideForTier(clampTier(tableTier))
                )
        );
    }

    public static boolean areBackpackExtrasUnlocked(int tableTier, int unlockedStorageSlots) {
        return clampTier(tableTier) >= 2
                && unlockedStorageSlots >= MateriaConfig.backpackExtrasUnlockSlots();
    }

    public static boolean areFurnaceExtrasUnlocked(int tableTier, int unlockedSlotsPerSide) {
        return clampTier(tableTier) >= 3
                && unlockedSlotsPerSide * 2 >= MateriaConfig.furnaceExtrasUnlockSlots();
    }

    public static int smeltEssenceUnlockedSlots() {
        return MateriaConfig.smeltEssenceUnlockSlots();
    }

    public static int maxBackpackExtraLevel(int tableTier, BackpackExtraUpgrade upgrade) {
        int tierCap = switch (clampTier(tableTier)) {
            case 2 -> switch (upgrade) {
                case STACKSIZE, FILTER -> 2;
                case AUTOPICKUP, KEEP_INVENTORY -> 1;
                case VOID, SOULBOUND -> 0;
            };
            case 3 -> switch (upgrade) {
                case STACKSIZE -> 4;
                case AUTOPICKUP, FILTER -> 3;
                case VOID, KEEP_INVENTORY -> 1;
                case SOULBOUND -> 0;
            };
            case 4 -> upgrade.maxLevel();
            default -> 0;
        };
        return Math.min(tierCap, upgrade.maxLevel());
    }

    public static int maxFurnaceExtraLevel(int tableTier, FurnaceExtraUpgrade upgrade) {
        int tierCap = switch (clampTier(tableTier)) {
            case 3 -> switch (upgrade) {
                case SPEED -> 20;
                case BATCH_SMELTING -> 4;
                case XP_STORAGE -> 1;
                case ESSENCE_GENERATOR -> 0;
            };
            case 4 -> upgrade.maxLevel();
            default -> 0;
        };
        return Math.min(tierCap, upgrade.maxLevel());
    }

    private static int clampTier(int tableTier) {
        return Math.max(1, Math.min(4, tableTier));
    }
}
