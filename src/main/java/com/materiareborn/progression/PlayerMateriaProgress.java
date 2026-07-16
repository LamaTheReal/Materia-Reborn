package com.materiareborn.progression;

import com.materiareborn.config.MateriaConfig;
import com.materiareborn.core.ModConstants;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;

public final class PlayerMateriaProgress {
    public static final int DEFAULT_STORAGE_SLOTS = 9;
    public static final int MAX_STORAGE_SLOTS = 540;
    public static final int DEFAULT_FURNACE_SLOTS_PER_SIDE = 1;
    public static final int MAX_FURNACE_SLOTS_PER_SIDE = 18;
    public static final int MAX_BACKPACK_FILTER_SLOTS = 36;
    public static final int MAX_ACTIVE_BACKPACK_FILTER_SLOTS = 36;

    private static final String STORAGE_SLOTS_TAG = ModConstants.MOD_ID + ".storage_slots";
    private static final String FURNACE_SLOTS_TAG = ModConstants.MOD_ID + ".furnace_slots";
    private static final String ACTIVE_TAB_TAG = ModConstants.MOD_ID + ".active_tab";
    private static final String FURNACE_EXTRA_PREFIX = ModConstants.MOD_ID + ".furnace_extra.";
    private static final String BACKPACK_EXTRA_PREFIX = ModConstants.MOD_ID + ".backpack_extra.";
    private static final String FURNACE_EXTRA_SETTING_PREFIX = ModConstants.MOD_ID + ".furnace_extra_setting.";
    private static final String BACKPACK_EXTRA_SETTING_PREFIX = ModConstants.MOD_ID + ".backpack_extra_setting.";
    private static final String STORED_XP_TAG = ModConstants.MOD_ID + ".stored_xp";
    private static final String BACKPACK_FILTER_GHOSTS_TAG = ModConstants.MOD_ID + ".backpack_filter_ghosts";
    private static final String BACKPACK_FILTER_IGNORE_NBT_TAG = ModConstants.MOD_ID + ".backpack_filter_ignore_nbt";
    private static final String BACKPACK_FILTER_IGNORE_DAMAGE_TAG = ModConstants.MOD_ID + ".backpack_filter_ignore_damage";

    private PlayerMateriaProgress() {
    }

    public static int storageSlots(Player player) {
        return clamp(
                getIntOrDefault(persisted(player), STORAGE_SLOTS_TAG, MateriaConfig.initialBackpackSlots()),
                MateriaConfig.initialBackpackSlots(),
                MAX_STORAGE_SLOTS
        );
    }

    public static int furnaceSlotsPerSide(Player player) {
        return clamp(
                getIntOrDefault(persisted(player), FURNACE_SLOTS_TAG, MateriaConfig.initialFurnaceSlotsPerSide()),
                MateriaConfig.initialFurnaceSlotsPerSide(),
                MAX_FURNACE_SLOTS_PER_SIDE
        );
    }

    public static boolean unlockStorageSlot(Player player) {
        return unlockStorageSlot(player, MAX_STORAGE_SLOTS);
    }

    public static boolean unlockStorageSlot(Player player, int maximumSlots) {
        int current = storageSlots(player);
        int cappedMaximum = clamp(maximumSlots, MateriaConfig.initialBackpackSlots(), MAX_STORAGE_SLOTS);
        if (current >= cappedMaximum) {
            return false;
        }
        persisted(player).putInt(STORAGE_SLOTS_TAG, current + 1);
        return true;
    }

    public static boolean unlockFurnaceSlotPair(Player player) {
        return unlockFurnaceSlotPair(player, MAX_FURNACE_SLOTS_PER_SIDE);
    }

    public static boolean unlockFurnaceSlotPair(Player player, int maximumSlotsPerSide) {
        int current = furnaceSlotsPerSide(player);
        int cappedMaximum = clamp(
                maximumSlotsPerSide,
                MateriaConfig.initialFurnaceSlotsPerSide(),
                MAX_FURNACE_SLOTS_PER_SIDE
        );
        if (current >= cappedMaximum) {
            return false;
        }
        persisted(player).putInt(FURNACE_SLOTS_TAG, current + 1);
        return true;
    }

    /**
     * Legacy migration primitive. The caller must verify ownership before using
     * block-local counters to raise permanent player progression.
     */
    public static void ensureAtLeast(Player player, int storageSlots, int furnaceSlotsPerSide) {
        CompoundTag persisted = persisted(player);
        int storage = Math.max(storageSlots(player), clamp(storageSlots, MateriaConfig.initialBackpackSlots(), MAX_STORAGE_SLOTS));
        int furnace = Math.max(
                furnaceSlotsPerSide(player),
                clamp(furnaceSlotsPerSide, MateriaConfig.initialFurnaceSlotsPerSide(), MAX_FURNACE_SLOTS_PER_SIDE)
        );
        persisted.putInt(STORAGE_SLOTS_TAG, storage);
        persisted.putInt(FURNACE_SLOTS_TAG, furnace);
    }

    public static void reset(Player player) {
        CompoundTag persisted = persisted(player);
        persisted.putInt(STORAGE_SLOTS_TAG, MateriaConfig.initialBackpackSlots());
        persisted.putInt(FURNACE_SLOTS_TAG, MateriaConfig.initialFurnaceSlotsPerSide());
        persisted.putInt(ACTIVE_TAB_TAG, 0);
        persisted.putInt(STORED_XP_TAG, 0);
        persisted.remove(BACKPACK_FILTER_GHOSTS_TAG);
        persisted.putBoolean(BACKPACK_FILTER_IGNORE_NBT_TAG, false);
        persisted.putBoolean(BACKPACK_FILTER_IGNORE_DAMAGE_TAG, false);
        for (FurnaceExtraUpgrade upgrade : FurnaceExtraUpgrade.values()) {
            persisted.putInt(extraTag(upgrade), 0);
            persisted.putInt(extraSettingTag(upgrade), 0);
        }
        for (BackpackExtraUpgrade upgrade : BackpackExtraUpgrade.values()) {
            persisted.putInt(backpackExtraTag(upgrade), 0);
            persisted.putInt(backpackExtraSettingTag(upgrade), 0);
        }
    }

    public static void unlockAll(Player player) {
        CompoundTag persisted = persisted(player);
        persisted.putInt(STORAGE_SLOTS_TAG, MateriaTableProgression.maxStorageSlots(4));
        persisted.putInt(FURNACE_SLOTS_TAG, MateriaTableProgression.maxFurnaceSlotsPerSide(4));
        for (FurnaceExtraUpgrade upgrade : FurnaceExtraUpgrade.values()) {
            int level = upgrade.maxLevel();
            persisted.putInt(extraTag(upgrade), level);
            persisted.putInt(extraSettingTag(upgrade), level);
        }
        for (BackpackExtraUpgrade upgrade : BackpackExtraUpgrade.values()) {
            int level = upgrade.maxLevel();
            persisted.putInt(backpackExtraTag(upgrade), level);
            persisted.putInt(backpackExtraSettingTag(upgrade), level);
        }
    }
    public static int activeTabId(Player player) {
        return clamp(getIntOrDefault(persisted(player), ACTIVE_TAB_TAG, 0), 0, 2);
    }

    public static void setActiveTabId(Player player, int activeTabId) {
        persisted(player).putInt(ACTIVE_TAB_TAG, clamp(activeTabId, 0, 2));
    }

    public static int furnaceExtraLevel(Player player, FurnaceExtraUpgrade upgrade) {
        return clamp(getIntOrDefault(persisted(player), extraTag(upgrade), 0), 0, upgrade.maxLevel());
    }

    public static boolean unlockFurnaceExtra(Player player, FurnaceExtraUpgrade upgrade) {
        return unlockFurnaceExtra(player, upgrade, upgrade.maxLevel());
    }

    public static boolean unlockFurnaceExtra(Player player, FurnaceExtraUpgrade upgrade, int maximumLevel) {
        int currentLevel = furnaceExtraLevel(player, upgrade);
        int cappedMaximum = clamp(maximumLevel, 0, upgrade.maxLevel());
        if (!upgrade.isAvailableForPurchase() || currentLevel >= cappedMaximum) {
            return false;
        }
        CompoundTag persisted = persisted(player);
        int nextLevel = currentLevel + 1;
        persisted.putInt(extraTag(upgrade), nextLevel);
        persisted.putInt(extraSettingTag(upgrade), nextLevel);
        return true;
    }

    public static int furnaceExtraSetting(Player player, FurnaceExtraUpgrade upgrade) {
        return clamp(
                getIntOrDefault(persisted(player), extraSettingTag(upgrade), 0),
                0,
                furnaceExtraLevel(player, upgrade)
        );
    }

    public static void setFurnaceExtraSetting(Player player, FurnaceExtraUpgrade upgrade, int level) {
        int purchasedLevel = furnaceExtraLevel(player, upgrade);
        persisted(player).putInt(extraSettingTag(upgrade), clamp(level, 0, purchasedLevel));
    }

    public static int backpackExtraLevel(Player player, BackpackExtraUpgrade upgrade) {
        return clamp(getIntOrDefault(persisted(player), backpackExtraTag(upgrade), 0), 0, upgrade.maxLevel());
    }

    public static boolean unlockBackpackExtra(Player player, BackpackExtraUpgrade upgrade) {
        return unlockBackpackExtra(player, upgrade, upgrade.maxLevel());
    }

    public static boolean unlockBackpackExtra(Player player, BackpackExtraUpgrade upgrade, int maximumLevel) {
        int currentLevel = backpackExtraLevel(player, upgrade);
        int cappedMaximum = clamp(maximumLevel, 0, upgrade.maxLevel());
        if (currentLevel >= cappedMaximum || !hasBackpackUpgradeDependencies(player, upgrade)) {
            return false;
        }
        CompoundTag persisted = persisted(player);
        int nextLevel = currentLevel + 1;
        persisted.putInt(backpackExtraTag(upgrade), nextLevel);
        persisted.putInt(backpackExtraSettingTag(upgrade), nextLevel);
        return true;
    }

    public static int backpackExtraSetting(Player player, BackpackExtraUpgrade upgrade) {
        return clamp(
                getIntOrDefault(persisted(player), backpackExtraSettingTag(upgrade), 0),
                0,
                backpackExtraLevel(player, upgrade)
        );
    }

    public static void setBackpackExtraSetting(Player player, BackpackExtraUpgrade upgrade, int level) {
        persisted(player).putInt(backpackExtraSettingTag(upgrade), clamp(level, 0, backpackExtraLevel(player, upgrade)));
    }

    public static boolean hasBackpackUpgradeDependencies(Player player, BackpackExtraUpgrade upgrade) {
        return switch (upgrade) {
            case VOID -> backpackExtraLevel(player, BackpackExtraUpgrade.FILTER) >= 1;
            case SOULBOUND -> backpackExtraLevel(player, BackpackExtraUpgrade.KEEP_INVENTORY) >= 1;
            default -> true;
        };
    }

    public static String missingBackpackUpgradeDependency(Player player, BackpackExtraUpgrade upgrade) {
        if (upgrade == BackpackExtraUpgrade.VOID
                && backpackExtraLevel(player, BackpackExtraUpgrade.FILTER) < 1) {
            return "filter";
        }
        if (upgrade == BackpackExtraUpgrade.SOULBOUND
                && backpackExtraLevel(player, BackpackExtraUpgrade.KEEP_INVENTORY) < 1) {
            return "keep_inventory";
        }
        return "";
    }
    public static int backpackFilterSlotCount(Player player) {
        return Math.min(
                MAX_ACTIVE_BACKPACK_FILTER_SLOTS,
                BackpackExtraUpgrade.filterSlotCount(backpackExtraSetting(player, BackpackExtraUpgrade.FILTER))
        );
    }

    public static ItemStackHandler loadBackpackFilterGhosts(Player player) {
        ItemStackHandler handler = new ItemStackHandler(MAX_BACKPACK_FILTER_SLOTS);
        CompoundTag saved = persisted(player).getCompound(BACKPACK_FILTER_GHOSTS_TAG);
        if (!saved.isEmpty()) {
            handler.deserializeNBT(player.registryAccess(), saved);
        }
        return handler;
    }

    public static void saveBackpackFilterGhosts(Player player, ItemStackHandler handler) {
        persisted(player).put(BACKPACK_FILTER_GHOSTS_TAG, handler.serializeNBT(player.registryAccess()));
    }

    public static boolean hasConfiguredBackpackFilter(Player player) {
        return hasConfiguredBackpackFilter(player, backpackFilterSlotCount(player));
    }

    public static boolean hasConfiguredBackpackFilter(Player player, int maximumActiveSlots) {
        ItemStackHandler filters = loadBackpackFilterGhosts(player);
        int activeSlots = Math.min(backpackFilterSlotCount(player), Math.max(0, maximumActiveSlots));
        for (int slot = 0; slot < activeSlots; slot++) {
            if (!filters.getStackInSlot(slot).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public static boolean matchesBackpackFilter(Player player, ItemStack candidate, boolean voidFilter) {
        return matchesBackpackFilter(player, candidate, voidFilter, backpackFilterSlotCount(player));
    }

    public static boolean matchesBackpackFilter(
            Player player,
            ItemStack candidate,
            boolean voidFilter,
            int maximumActiveSlots
    ) {
        if (candidate.isEmpty()) {
            return false;
        }
        ItemStackHandler filters = loadBackpackFilterGhosts(player);
        int activeSlots = Math.min(backpackFilterSlotCount(player), Math.max(0, maximumActiveSlots));
        for (int slot = 0; slot < activeSlots; slot++) {
            ItemStack configured = filters.getStackInSlot(slot);
            if (configured.isEmpty() || !ItemStack.isSameItem(configured, candidate)) {
                continue;
            }
            if (voidFilter
                    && candidate.get(DataComponents.CUSTOM_DATA) != null
                    && !ItemStack.isSameItemSameComponents(configured, candidate)) {
                continue;
            }
            boolean ignoreNbt = backpackFilterIgnoreNbt(player)
                    && maximumActiveSlots >= BackpackExtraUpgrade.filterSlotCount(3);
            boolean ignoreDamage = backpackFilterIgnoreDamage(player)
                    && maximumActiveSlots >= BackpackExtraUpgrade.filterSlotCount(4);
            if (filterStacksMatch(configured, candidate, ignoreNbt, ignoreDamage)) {
                return true;
            }
        }
        return false;
    }

    private static boolean filterStacksMatch(ItemStack configured, ItemStack candidate, boolean ignoreNbt, boolean ignoreDamage) {
        if (!ignoreNbt && !ignoreDamage) {
            return ItemStack.isSameItemSameComponents(configured, candidate);
        }
        ItemStack left = configured.copy();
        ItemStack right = candidate.copy();
        if (ignoreNbt) {
            left.remove(DataComponents.CUSTOM_DATA);
            right.remove(DataComponents.CUSTOM_DATA);
        }
        if (ignoreDamage) {
            left.remove(DataComponents.DAMAGE);
            right.remove(DataComponents.DAMAGE);
        }
        return ItemStack.isSameItemSameComponents(left, right);
    }
    public static boolean backpackFilterIgnoreNbt(Player player) {
        return persisted(player).getBoolean(BACKPACK_FILTER_IGNORE_NBT_TAG);
    }

    public static void setBackpackFilterIgnoreNbt(Player player, boolean ignoreNbt) {
        persisted(player).putBoolean(BACKPACK_FILTER_IGNORE_NBT_TAG, ignoreNbt);
    }

    public static boolean backpackFilterIgnoreDamage(Player player) {
        return persisted(player).getBoolean(BACKPACK_FILTER_IGNORE_DAMAGE_TAG);
    }

    public static void setBackpackFilterIgnoreDamage(Player player, boolean ignoreDamage) {
        persisted(player).putBoolean(BACKPACK_FILTER_IGNORE_DAMAGE_TAG, ignoreDamage);
    }
    public static int storedExperience(Player player) {
        return Math.max(0, getIntOrDefault(persisted(player), STORED_XP_TAG, 0));
    }

    public static int takeStoredExperience(Player player) {
        CompoundTag persisted = persisted(player);
        int stored = Math.max(0, getIntOrDefault(persisted, STORED_XP_TAG, 0));
        persisted.putInt(STORED_XP_TAG, 0);
        return stored;
    }

    private static String extraTag(FurnaceExtraUpgrade upgrade) {
        return FURNACE_EXTRA_PREFIX + upgrade.id();
    }
    private static String backpackExtraTag(BackpackExtraUpgrade upgrade) {
        return BACKPACK_EXTRA_PREFIX + upgrade.id();
    }

    private static String backpackExtraSettingTag(BackpackExtraUpgrade upgrade) {
        return BACKPACK_EXTRA_SETTING_PREFIX + upgrade.id();
    }
    private static String extraSettingTag(FurnaceExtraUpgrade upgrade) {
        return FURNACE_EXTRA_SETTING_PREFIX + upgrade.id();
    }

    private static CompoundTag persisted(Player player) {
        CompoundTag root = player.getPersistentData();
        CompoundTag persisted = root.getCompound(Player.PERSISTED_NBT_TAG);
        root.put(Player.PERSISTED_NBT_TAG, persisted);
        return persisted;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static int getIntOrDefault(CompoundTag tag, String key, int fallback) {
        return tag.contains(key) ? tag.getInt(key) : fallback;
    }
}
