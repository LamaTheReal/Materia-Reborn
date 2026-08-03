package com.materiareborn.config;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.neoforged.neoforge.common.ModConfigSpec;

public final class MateriaConfig {
    public static final ModConfigSpec SPEC;
    private static final Values VALUES;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        VALUES = new Values(builder);
        SPEC = builder.build();
    }

    private MateriaConfig() {
    }

    public static long maxEssence() {
        return VALUES.maxEssence.getAsLong();
    }

    public static int initialBackpackSlots() {
        return VALUES.initialBackpackSlots.getAsInt();
    }

    public static int backpackSlotsForTier(int tier) {
        return switch (clamp(tier, 1, 4)) {
            case 2 -> VALUES.table2BackpackSlots.getAsInt();
            case 3 -> VALUES.table3BackpackSlots.getAsInt();
            case 4 -> VALUES.table4BackpackSlots.getAsInt();
            default -> initialBackpackSlots();
        };
    }

    public static int initialFurnaceSlotsPerSide() {
        return VALUES.initialFurnaceSlotsPerSide.getAsInt();
    }

    public static int furnaceSlotsPerSideForTier(int tier) {
        return switch (clamp(tier, 1, 4)) {
            case 3 -> VALUES.table3FurnaceSlotsPerSide.getAsInt();
            case 4 -> VALUES.table4FurnaceSlotsPerSide.getAsInt();
            default -> initialFurnaceSlotsPerSide();
        };
    }

    public static int backpackExtrasUnlockSlots() {
        return VALUES.backpackExtrasUnlockSlots.getAsInt();
    }

    public static int furnaceExtrasUnlockSlots() {
        return VALUES.furnaceExtrasUnlockSlots.getAsInt();
    }

    public static boolean smeltEssenceEnabled() {
        return VALUES.smeltEssenceEnabled.getAsBoolean();
    }

    public static int smeltEssenceUnlockSlots() {
        return VALUES.smeltEssenceUnlockSlots.getAsInt();
    }

    public static int smeltEssenceCostPerItem() {
        return VALUES.smeltEssenceCostPerItem.getAsInt();
    }

    public static int backpackSlotUpgradeCost(int upgradeNumber) {
        return VALUES.backpackSlotCosts.cost(upgradeNumber);
    }

    public static int furnaceSlotUpgradeCost(int upgradeNumber) {
        return VALUES.furnaceSlotCosts.cost(upgradeNumber);
    }

    public static boolean backpackUpgradeEnabled(String id) {
        UpgradeValues values = VALUES.backpackUpgrades.get(id);
        return values != null && values.enabled.getAsBoolean();
    }

    public static int backpackUpgradeMaxLevel(String id) {
        UpgradeValues values = VALUES.backpackUpgrades.get(id);
        return values == null || !values.enabled.getAsBoolean() ? 0 : values.maxLevel.getAsInt();
    }

    public static int backpackUpgradeCost(String id, int level) {
        UpgradeValues values = VALUES.backpackUpgrades.get(id);
        return values == null || !values.enabled.getAsBoolean() ? 0 : values.cost(level);
    }

    public static boolean furnaceUpgradeEnabled(String id) {
        UpgradeValues values = VALUES.furnaceUpgrades.get(id);
        return values != null && values.enabled.getAsBoolean();
    }

    public static int furnaceUpgradeMaxLevel(String id) {
        UpgradeValues values = VALUES.furnaceUpgrades.get(id);
        return values == null || !values.enabled.getAsBoolean() ? 0 : values.maxLevel.getAsInt();
    }

    public static int furnaceUpgradeCost(String id, int level) {
        UpgradeValues values = VALUES.furnaceUpgrades.get(id);
        return values == null || !values.enabled.getAsBoolean() ? 0 : values.cost(level);
    }

    public static int backpackStackLimit(int level) {
        return VALUES.stackLimits.value(level);
    }

    public static int maximumBackpackStackSize() {
        return VALUES.stackLimits.maximum();
    }

    public static int autopickupRange(int level) {
        return VALUES.autopickupRanges.value(level);
    }

    public static int filterSlotCount(int level) {
        return VALUES.filterSlots.value(level);
    }

    public static int baseCookTimeTicks() {
        return VALUES.baseCookTimeTicks.getAsInt();
    }

    public static int level29CookTimeTicks() {
        return VALUES.level29CookTimeTicks.getAsInt();
    }

    public static int instantCookTimeTicks() {
        return VALUES.instantCookTimeTicks.getAsInt();
    }

    public static int batchItemsPerLevel() {
        return VALUES.batchItemsPerLevel.getAsInt();
    }

    private static int clamp(int value, int minimum, int maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    private static final class Values {
        private final ModConfigSpec.LongValue maxEssence;
        private final ModConfigSpec.IntValue initialBackpackSlots;
        private final ModConfigSpec.IntValue table2BackpackSlots;
        private final ModConfigSpec.IntValue table3BackpackSlots;
        private final ModConfigSpec.IntValue table4BackpackSlots;
        private final ModConfigSpec.IntValue initialFurnaceSlotsPerSide;
        private final ModConfigSpec.IntValue table3FurnaceSlotsPerSide;
        private final ModConfigSpec.IntValue table4FurnaceSlotsPerSide;
        private final ModConfigSpec.IntValue backpackExtrasUnlockSlots;
        private final ModConfigSpec.IntValue furnaceExtrasUnlockSlots;
        private final ModConfigSpec.BooleanValue smeltEssenceEnabled;
        private final ModConfigSpec.IntValue smeltEssenceUnlockSlots;
        private final ModConfigSpec.IntValue smeltEssenceCostPerItem;
        private final SlotCostValues backpackSlotCosts;
        private final SlotCostValues furnaceSlotCosts;
        private final Map<String, UpgradeValues> backpackUpgrades = new LinkedHashMap<>();
        private final Map<String, UpgradeValues> furnaceUpgrades = new LinkedHashMap<>();
        private final LevelValues stackLimits;
        private final LevelValues autopickupRanges;
        private final LevelValues filterSlots;
        private final ModConfigSpec.IntValue baseCookTimeTicks;
        private final ModConfigSpec.IntValue level29CookTimeTicks;
        private final ModConfigSpec.IntValue instantCookTimeTicks;
        private final ModConfigSpec.IntValue batchItemsPerLevel;

        private Values(ModConfigSpec.Builder builder) {
            builder.comment("Core Essence limits.").push("essence");
            maxEssence = builder.comment("Maximum Essence stored by one player.")
                    .defineInRange("maximum", Long.MAX_VALUE, 0L, Long.MAX_VALUE);
            builder.pop();

            builder.comment("Player-owned slot progression and slot prices.").push("slots");
            builder.push("backpack");
            initialBackpackSlots = builder.defineInRange("initial", 9, 1, 540);
            table2BackpackSlots = builder.defineInRange("table_level_2_maximum", 78, 1, 540);
            table3BackpackSlots = builder.defineInRange("table_level_3_maximum", 234, 1, 540);
            table4BackpackSlots = builder.defineInRange("table_level_4_maximum", 540, 1, 540);
            backpackSlotCosts = new SlotCostValues(builder);
            builder.pop();
            builder.push("furnace");
            initialFurnaceSlotsPerSide = builder.defineInRange("initial_per_side", 1, 1, 18);
            table3FurnaceSlotsPerSide = builder.defineInRange("table_level_3_maximum_per_side", 9, 1, 18);
            table4FurnaceSlotsPerSide = builder.defineInRange("table_level_4_maximum_per_side", 18, 1, 18);
            furnaceSlotCosts = new SlotCostValues(builder);
            builder.pop(2);

            builder.comment("Backpack extras. Every extra has an enable switch, maximum level and one price per level.")
                    .push("backpack_extras");
            backpackExtrasUnlockSlots = builder.defineInRange("unlocked_at_slots", 18, 0, 540);
            backpackUpgrades.put("stacksize", new UpgradeValues(builder, "stacksize", true, 6,
                    4500, 9000, 18000, 36000, 72000, 144000));
            backpackUpgrades.put("autopickup", new UpgradeValues(builder, "autopickup", true, 4,
                    4000, 10000, 20000, 40000));
            backpackUpgrades.put("void", new UpgradeValues(builder, "void", true, 1, 20000));
            backpackUpgrades.put("filter", new UpgradeValues(builder, "filter", true, 4,
                    2000, 4000, 8000, 16000));
            backpackUpgrades.put("keep_inventory", new UpgradeValues(builder, "keep_inventory", true, 1, 25000));
            backpackUpgrades.put("soulbound", new UpgradeValues(builder, "soulbound", true, 1, 64000));
            stackLimits = new LevelValues(builder, "stacksize_limits", 64, 4096,
                    64, 96, 128, 192, 256, 384, 512);
            autopickupRanges = new LevelValues(builder, "autopickup_ranges", 0, 128,
                    0, 5, 10, 15, 20);
            filterSlots = new LevelValues(builder, "filter_slots", 0, 36,
                    0, 9, 18, 27, 36);
            builder.pop();

            builder.comment("Furnace extras and Smelt Essence.").push("furnace_extras");
            furnaceExtrasUnlockSlots = builder.defineInRange("unlocked_at_total_slots", 18, 0, 36);
            furnaceUpgrades.put("speed", new UpgradeValues(builder, "speed", true, 30,
                    speedCosts()));
            furnaceUpgrades.put("batch_smelting", new UpgradeValues(builder, "batch_smelting", true, 8,
                    2800, 5600, 8400, 11200, 22400, 44800, 89600, 179200));
            furnaceUpgrades.put("xp_storage", new UpgradeValues(builder, "xp_storage", true, 1, 45600));
            furnaceUpgrades.put("essence_generator", new UpgradeValues(builder, "essence_generator", false, 20,
                    new int[20]));
            baseCookTimeTicks = builder.defineInRange("speed_base_cook_ticks", 200, 1, 72000);
            level29CookTimeTicks = builder.defineInRange("speed_level_29_cook_ticks", 20, 1, 72000);
            instantCookTimeTicks = builder.defineInRange("speed_level_30_cook_ticks", 1, 1, 72000);
            batchItemsPerLevel = builder.defineInRange("batch_items_per_level", 8, 1, 64);
            builder.push("smelt_essence");
            smeltEssenceEnabled = builder.define("enabled", true);
            smeltEssenceUnlockSlots = builder.defineInRange("unlocked_at_total_slots", 18, 0, 36);
            smeltEssenceCostPerItem = builder.defineInRange("cost_per_smelted_item", 20, 0, Integer.MAX_VALUE);
            builder.pop(2);
        }

        private static int[] speedCosts() {
            int[] result = new int[30];
            for (int level = 1; level <= result.length; level++) {
                result[level - 1] = level <= 5 ? 5000
                        : level <= 10 ? 8500
                        : level <= 20 ? 10500
                        : level <= 25 ? 16000
                        : level <= 29 ? 24000
                        : 96000;
            }
            return result;
        }
    }

    private static final class UpgradeValues {
        private final ModConfigSpec.BooleanValue enabled;
        private final ModConfigSpec.IntValue maxLevel;
        private final List<ModConfigSpec.IntValue> costs;

        private UpgradeValues(
                ModConfigSpec.Builder builder,
                String id,
                boolean defaultEnabled,
                int defaultMaxLevel,
                int... defaultCosts
        ) {
            builder.push(id);
            enabled = builder.define("enabled", defaultEnabled);
            maxLevel = builder.defineInRange("maximum_level", defaultMaxLevel, 0, defaultMaxLevel);
            builder.push("costs");
            costs = new ArrayList<>(defaultCosts.length);
            for (int level = 1; level <= defaultCosts.length; level++) {
                costs.add(builder.defineInRange("level_" + level, defaultCosts[level - 1], 0, Integer.MAX_VALUE));
            }
            builder.pop(2);
        }

        private int cost(int level) {
            return level < 1 || level > costs.size() ? 0 : costs.get(level - 1).getAsInt();
        }
    }

    private static final class SlotCostValues {
        private final List<ModConfigSpec.IntValue> brackets;

        private SlotCostValues(ModConfigSpec.Builder builder) {
            builder.push("upgrade_costs");
            brackets = List.of(
                    builder.defineInRange("upgrades_1_to_4", 500, 0, Integer.MAX_VALUE),
                    builder.defineInRange("upgrades_5_to_8", 1000, 0, Integer.MAX_VALUE),
                    builder.defineInRange("upgrades_9_to_12", 2000, 0, Integer.MAX_VALUE),
                    builder.defineInRange("upgrades_13_to_16", 4000, 0, Integer.MAX_VALUE),
                    builder.defineInRange("upgrades_17_to_20", 8000, 0, Integer.MAX_VALUE),
                    builder.defineInRange("upgrades_21_and_later", 16000, 0, Integer.MAX_VALUE)
            );
            builder.pop();
        }

        private int cost(int upgradeNumber) {
            int bracket = upgradeNumber <= 4 ? 0
                    : upgradeNumber <= 8 ? 1
                    : upgradeNumber <= 12 ? 2
                    : upgradeNumber <= 16 ? 3
                    : upgradeNumber <= 20 ? 4
                    : 5;
            return brackets.get(bracket).getAsInt();
        }
    }

    private static final class LevelValues {
        private final List<ModConfigSpec.IntValue> values;

        private LevelValues(ModConfigSpec.Builder builder, String path, int minimum, int maximum, int... defaults) {
            builder.push(path);
            values = new ArrayList<>(defaults.length);
            for (int level = 0; level < defaults.length; level++) {
                values.add(builder.defineInRange("level_" + level, defaults[level], minimum, maximum));
            }
            builder.pop();
        }

        private int value(int level) {
            return values.get(clamp(level, 0, values.size() - 1)).getAsInt();
        }

        private int maximum() {
            int maximum = 1;
            for (ModConfigSpec.IntValue value : values) {
                maximum = Math.max(maximum, value.getAsInt());
            }
            return maximum;
        }
    }
}
