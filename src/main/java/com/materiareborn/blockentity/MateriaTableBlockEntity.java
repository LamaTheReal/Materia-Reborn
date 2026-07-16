package com.materiareborn.blockentity;

import com.materiareborn.config.MateriaConfig;
import com.materiareborn.essence.PlayerEssence;
import com.materiareborn.menu.MateriaTableMenu;
import com.materiareborn.progression.BackpackExtraUpgrade;
import com.materiareborn.progression.FurnaceExtraUpgrade;
import com.materiareborn.progression.MateriaTableProgression;
import com.materiareborn.progression.PlayerMateriaProgress;
import com.materiareborn.registry.ModBlockEntityTypes;
import com.materiareborn.ritual.MateriaTableRitualService;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.items.ItemStackHandler;

public final class MateriaTableBlockEntity extends AbstractFurnaceBlockEntity {
    private static final String ANALYZE_INPUT_TAG = "AnalyzeInput";
    private static final String STORAGE_TAG = "Storage";
    private static final String FURNACE_INPUT_INVENTORY_TAG = "FurnaceInputInventory";
    private static final String FURNACE_OUTPUT_INVENTORY_TAG = "FurnaceOutputInventory";
    private static final String SMELT_ESSENCE_TAG = "SmeltEssence";
    private static final String SMELT_ESSENCE_OWNER_TAG = "SmeltEssenceOwner";
    private static final String FURNACE_OWNER_TAG = "FurnaceOwner";
    private static final String BACKPACK_OWNER_TAG = "BackpackOwner";
    private static final String OWNED_PROGRESS_MIGRATION_COMPLETE_TAG = "OwnedProgressMigrationComplete";
    private static final String UNLOCKED_STORAGE_SLOTS_TAG = "UnlockedStorageSlots";
    private static final String UNLOCKED_FURNACE_SLOTS_TAG = "UnlockedFurnaceSlots";
    private static final String ACTIVE_SPEED_TAG = "ActiveSpeed";
    private static final String ACTIVE_BATCH_TAG = "ActiveBatch";
    private static final String XP_STORAGE_UPGRADE_TAG = "XpStorageUpgrade";
    private static final String XP_STORAGE_ACTIVE_TAG = "XpStorageActive";
    private static final String PENDING_EXPERIENCE_TAG = "PendingExperience";
    private static final String STORED_EXPERIENCE_TAG = "StoredExperience";
    private static final String UPGRADE_RITUAL_TARGET_TIER_TAG = "UpgradeRitualTargetTier";
    private static final String UPGRADE_RITUAL_STEP_TAG = "UpgradeRitualStep";
    private static final String UPGRADE_RITUAL_TICKS_TAG = "UpgradeRitualTicks";
    private static final String UPGRADE_RITUAL_OWNER_TAG = "UpgradeRitualOwner";

    private static final int STORAGE_SLOT_COUNT = 540;
    private static final int FURNACE_INVENTORY_SLOT_COUNT = 18;
    private static final int AUTOPICKUP_INTERVAL_TICKS = 10;
    private static final int INPUT_SLOT = 0;
    private static final int FUEL_SLOT = 1;
    private static final int RESULT_SLOT = 2;


    private final ItemStackHandler analyzeInput = handler(1);
    private final ItemStackHandler storage = new BackpackInventoryHandler(
            STORAGE_SLOT_COUNT,
            this::unlockedStorageSlots,
            this::backpackStacksizeLevel,
            this::setChangedAndUpdate
    );
    private final ItemStackHandler furnaceInputInventory = handler(FURNACE_INVENTORY_SLOT_COUNT);
    private final ItemStackHandler furnaceOutputInventory = handler(FURNACE_INVENTORY_SLOT_COUNT);

    private boolean smeltEssenceEnabled;
    private UUID smeltEssenceOwner;
    private UUID furnaceOwner;
    private UUID backpackOwner;
    private boolean ownedProgressMigrationComplete = true;
    private int autopickupTicks;
    private int unlockedStorageSlots = MateriaConfig.initialBackpackSlots();
    private int unlockedFurnaceSlotsPerSide = MateriaConfig.initialFurnaceSlotsPerSide();
    private int activeSpeedLevel;
    private int activeBatchLevel;
    private boolean xpStorageUpgrade;
    private boolean xpStorageActive;
    private double pendingExperience;
    private double storedExperience;
    private BatchPlan activeBatchPlan;
    private boolean preserveBackpackOnBreak;
    private int upgradeRitualTargetTier;
    private int upgradeRitualStep;
    private int upgradeRitualTicks;
    private UUID upgradeRitualOwner;
    private boolean upgradeReplacementInProgress;

    public MateriaTableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.MATERIA_TABLE.get(), pos, state, RecipeType.SMELTING);
    }

    public ItemStackHandler storage() {
        return storage;
    }

    public ItemStackHandler analyzeInput() {
        return analyzeInput;
    }

    public ItemStackHandler furnaceInputInventory() {
        return furnaceInputInventory;
    }

    public ItemStackHandler furnaceOutputInventory() {
        return furnaceOutputInventory;
    }

    public ContainerData furnaceData() {
        return dataAccess;
    }

    public boolean isSmeltEssenceEnabled() {
        return smeltEssenceEnabled;
    }

    public boolean hasXpStorageUpgrade() {
        return xpStorageUpgrade;
    }

    public int storedExperienceWhole() {
        return Mth.floor((float) storedExperience);
    }

    public void setSmeltEssenceEnabled(boolean enabled) {
        setSmeltEssenceEnabled(enabled, null);
    }

    public void setSmeltEssenceEnabled(boolean enabled, Player owner) {
        UUID ownerId = enabled && owner != null ? owner.getUUID() : null;
        boolean changed = smeltEssenceEnabled != enabled || (ownerId == null ? smeltEssenceOwner != null : !ownerId.equals(smeltEssenceOwner));
        smeltEssenceEnabled = enabled && ownerId != null;
        smeltEssenceOwner = smeltEssenceEnabled ? ownerId : null;
        activeBatchPlan = null;
        if (!smeltEssenceEnabled) {
            dataAccess.set(AbstractFurnaceBlockEntity.DATA_LIT_TIME, 0);
            dataAccess.set(AbstractFurnaceBlockEntity.DATA_LIT_DURATION, 0);
        }
        if (changed) {
            setChangedAndUpdate();
        }
    }

    public int unlockedStorageSlots() {
        return unlockedStorageSlots;
    }

    public int unlockedFurnaceSlotsPerSide() {
        return unlockedFurnaceSlotsPerSide;
    }

    /**
     * Migrates the old block-local slot counters only back to the player who last
     * owned that part of the table. A different opener must never inherit paid
     * progression from the table.
     */
    public void migrateOwnedStoredProgress(Player player) {
        if (ownedProgressMigrationComplete) {
            return;
        }
        ownedProgressMigrationComplete = true;
        UUID playerId = player.getUUID();
        boolean ownsBackpackProgress = playerId.equals(backpackOwner);
        boolean ownsFurnaceProgress = playerId.equals(furnaceOwner);
        if (!ownsBackpackProgress && !ownsFurnaceProgress) {
            return;
        }
        PlayerMateriaProgress.ensureAtLeast(
                player,
                ownsBackpackProgress ? unlockedStorageSlots : PlayerMateriaProgress.storageSlots(player),
                ownsFurnaceProgress ? unlockedFurnaceSlotsPerSide : PlayerMateriaProgress.furnaceSlotsPerSide(player)
        );
    }

    public void applyPlayerProgress(Player player) {
        migrateOwnedStoredProgress(player);
        int tableTier = tableTier();
        unlockedStorageSlots = Math.min(
                PlayerMateriaProgress.storageSlots(player),
                MateriaTableProgression.maxStorageSlots(tableTier)
        );
        unlockedFurnaceSlotsPerSide = Math.min(
                PlayerMateriaProgress.furnaceSlotsPerSide(player),
                MateriaTableProgression.maxFurnaceSlotsPerSide(tableTier)
        );
        furnaceOwner = player.getUUID();
        backpackOwner = player.getUUID();
        activeSpeedLevel = effectiveFurnaceSetting(player, FurnaceExtraUpgrade.SPEED);
        activeBatchLevel = effectiveFurnaceSetting(player, FurnaceExtraUpgrade.BATCH_SMELTING);
        xpStorageUpgrade = effectiveFurnaceLevel(player, FurnaceExtraUpgrade.XP_STORAGE) > 0;
        xpStorageActive = effectiveFurnaceSetting(player, FurnaceExtraUpgrade.XP_STORAGE) > 0;
        activeBatchPlan = null;
        setChangedAndUpdate();
    }

    private int tableTier() {
        return MateriaTableProgression.tableTier(getBlockState().getBlock());
    }

    private boolean areBackpackExtrasUnlocked() {
        return MateriaTableProgression.areBackpackExtrasUnlocked(tableTier(), unlockedStorageSlots);
    }

    private boolean areFurnaceExtrasUnlocked() {
        return MateriaTableProgression.areFurnaceExtrasUnlocked(tableTier(), unlockedFurnaceSlotsPerSide);
    }

    private int effectiveBackpackLevel(Player player, BackpackExtraUpgrade upgrade) {
        if (!areBackpackExtrasUnlocked()) {
            return 0;
        }
        return Math.min(
                PlayerMateriaProgress.backpackExtraLevel(player, upgrade),
                MateriaTableProgression.maxBackpackExtraLevel(tableTier(), upgrade)
        );
    }

    private int effectiveBackpackSetting(Player player, BackpackExtraUpgrade upgrade) {
        return Math.min(
                PlayerMateriaProgress.backpackExtraSetting(player, upgrade),
                effectiveBackpackLevel(player, upgrade)
        );
    }

    private int effectiveFurnaceLevel(Player player, FurnaceExtraUpgrade upgrade) {
        if (!areFurnaceExtrasUnlocked() || !upgrade.isAvailableForPurchase()) {
            return 0;
        }
        return Math.min(
                PlayerMateriaProgress.furnaceExtraLevel(player, upgrade),
                MateriaTableProgression.maxFurnaceExtraLevel(tableTier(), upgrade)
        );
    }

    private int effectiveFurnaceSetting(Player player, FurnaceExtraUpgrade upgrade) {
        return Math.min(
                PlayerMateriaProgress.furnaceExtraSetting(player, upgrade),
                effectiveFurnaceLevel(player, upgrade)
        );
    }

    public void prepareKeepInventoryDrop(Player player) {
        preserveBackpackOnBreak = !player.isCreative()
                && effectiveBackpackSetting(player, BackpackExtraUpgrade.KEEP_INVENTORY) > 0;
    }

    public boolean shouldPreserveBackpackOnBreak() {
        return preserveBackpackOnBreak;
    }

    public ItemStack createPreservedBlockItem(net.minecraft.world.level.ItemLike tableItem) {
        ItemStack stack = new ItemStack(tableItem);
        if (level != null) {
            saveToItem(stack, level.registryAccess());
        }
        return stack;
    }

    public void clearBreakPreservation() {
        preserveBackpackOnBreak = false;
    }

    public void startUpgradeRitual(int targetTier, UUID owner) {
        upgradeRitualTargetTier = targetTier;
        upgradeRitualStep = 0;
        upgradeRitualTicks = 0;
        upgradeRitualOwner = owner;
        setChangedAndUpdate();
    }

    public boolean isUpgradeRitualActive() {
        return upgradeRitualTargetTier >= 2 && upgradeRitualTargetTier <= 4;
    }

    public int upgradeRitualTargetTier() {
        return upgradeRitualTargetTier;
    }

    public int upgradeRitualStep() {
        return upgradeRitualStep;
    }

    public UUID upgradeRitualOwner() {
        return upgradeRitualOwner;
    }

    public int advanceUpgradeRitualTick() {
        upgradeRitualTicks++;
        setChanged();
        return upgradeRitualTicks;
    }

    public void advanceUpgradeRitualStep() {
        upgradeRitualStep++;
        upgradeRitualTicks = 0;
        setChanged();
    }

    public void clearUpgradeRitual() {
        upgradeRitualTargetTier = 0;
        upgradeRitualStep = 0;
        upgradeRitualTicks = 0;
        upgradeRitualOwner = null;
        setChanged();
    }

    public void setUpgradeReplacementInProgress(boolean inProgress) {
        upgradeReplacementInProgress = inProgress;
    }
    public int totalUnlockedFurnaceSlots() {
        return unlockedFurnaceSlotsPerSide * 2;
    }

    public boolean canUseSmeltEssence() {
        return MateriaConfig.smeltEssenceEnabled()
                && tableTier() >= 3
                && totalUnlockedFurnaceSlots() >= MateriaConfig.smeltEssenceUnlockSlots();
    }

    public void resetProgression() {
        smeltEssenceEnabled = false;
        smeltEssenceOwner = null;
        furnaceOwner = null;
        backpackOwner = null;
        ownedProgressMigrationComplete = true;
        unlockedStorageSlots = MateriaConfig.initialBackpackSlots();
        unlockedFurnaceSlotsPerSide = MateriaConfig.initialFurnaceSlotsPerSide();
        activeSpeedLevel = 0;
        activeBatchLevel = 0;
        xpStorageUpgrade = false;
        xpStorageActive = false;
        pendingExperience = 0.0D;
        storedExperience = 0.0D;
        activeBatchPlan = null;
        clearUpgradeRitual();
        clearHandler(analyzeInput);
        clearHandler(storage);
        clearHandler(furnaceInputInventory);
        clearHandler(furnaceOutputInventory);
        for (int slot = 0; slot < getContainerSize(); slot++) {
            setItem(slot, ItemStack.EMPTY);
        }
        dataAccess.set(AbstractFurnaceBlockEntity.DATA_LIT_TIME, 0);
        dataAccess.set(AbstractFurnaceBlockEntity.DATA_LIT_DURATION, 0);
        dataAccess.set(AbstractFurnaceBlockEntity.DATA_COOKING_PROGRESS, 0);
        setChangedAndUpdate();
    }

    public void releasePendingExperience(Player player) {
        if (level instanceof ServerLevel serverLevel && !xpStorageActive) {
            awardExperience(serverLevel, player, false);
        }
    }

    public boolean claimStoredExperience(ServerPlayer player) {
        if (!(level instanceof ServerLevel serverLevel)
                || !areFurnaceExtrasUnlocked()
                || !xpStorageUpgrade
                || !xpStorageActive) {
            return false;
        }
        int claimable = Mth.floor((float) storedExperience);
        if (claimable <= 0) {
            return false;
        }
        storedExperience -= claimable;
        setChangedAndUpdate();
        ExperienceOrb.award(serverLevel, player.position(), claimable);
        return true;
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.materia_reborn.materia_table");
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory playerInventory) {
        return new MateriaTableMenu(containerId, playerInventory, this, ContainerLevelAccess.create(playerInventory.player.level(), worldPosition));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        deserializeHandler(tag, ANALYZE_INPUT_TAG, analyzeInput, registries);
        deserializeHandler(tag, STORAGE_TAG, storage, registries);
        deserializeHandler(tag, FURNACE_INPUT_INVENTORY_TAG, furnaceInputInventory, registries);
        deserializeHandler(tag, FURNACE_OUTPUT_INVENTORY_TAG, furnaceOutputInventory, registries);
        smeltEssenceEnabled = tag.getBoolean(SMELT_ESSENCE_TAG);
        smeltEssenceOwner = tag.hasUUID(SMELT_ESSENCE_OWNER_TAG) ? tag.getUUID(SMELT_ESSENCE_OWNER_TAG) : null;
        furnaceOwner = tag.hasUUID(FURNACE_OWNER_TAG) ? tag.getUUID(FURNACE_OWNER_TAG) : null;
        backpackOwner = tag.hasUUID(BACKPACK_OWNER_TAG) ? tag.getUUID(BACKPACK_OWNER_TAG) : null;
        ownedProgressMigrationComplete = tag.getBoolean(OWNED_PROGRESS_MIGRATION_COMPLETE_TAG);
        if (smeltEssenceOwner == null) {
            smeltEssenceEnabled = false;
        }
        unlockedStorageSlots = clamp(tag.getInt(UNLOCKED_STORAGE_SLOTS_TAG), MateriaConfig.initialBackpackSlots(), STORAGE_SLOT_COUNT);
        unlockedFurnaceSlotsPerSide = clamp(tag.getInt(UNLOCKED_FURNACE_SLOTS_TAG), MateriaConfig.initialFurnaceSlotsPerSide(), FURNACE_INVENTORY_SLOT_COUNT);
        activeSpeedLevel = clamp(tag.getInt(ACTIVE_SPEED_TAG), 0, 30);
        activeBatchLevel = clamp(tag.getInt(ACTIVE_BATCH_TAG), 0, 8);
        xpStorageUpgrade = tag.getBoolean(XP_STORAGE_UPGRADE_TAG);
        xpStorageActive = tag.getBoolean(XP_STORAGE_ACTIVE_TAG);
        pendingExperience = Math.max(0.0D, tag.getDouble(PENDING_EXPERIENCE_TAG));
        storedExperience = Math.max(0.0D, tag.getDouble(STORED_EXPERIENCE_TAG));
        int tableTier = tableTier();
        unlockedStorageSlots = Math.min(
                unlockedStorageSlots,
                MateriaTableProgression.maxStorageSlots(tableTier)
        );
        unlockedFurnaceSlotsPerSide = Math.min(
                unlockedFurnaceSlotsPerSide,
                MateriaTableProgression.maxFurnaceSlotsPerSide(tableTier)
        );
        int speedCap = areFurnaceExtrasUnlocked()
                ? MateriaTableProgression.maxFurnaceExtraLevel(tableTier, FurnaceExtraUpgrade.SPEED)
                : 0;

        int batchCap = areFurnaceExtrasUnlocked()
                ? MateriaTableProgression.maxFurnaceExtraLevel(tableTier, FurnaceExtraUpgrade.BATCH_SMELTING)
                : 0;
        activeSpeedLevel = Math.min(
                activeSpeedLevel,
                speedCap
        );

        activeBatchLevel = Math.min(
                activeBatchLevel,
                batchCap
        );
        if (!areFurnaceExtrasUnlocked()
                || MateriaTableProgression.maxFurnaceExtraLevel(tableTier, FurnaceExtraUpgrade.XP_STORAGE) <= 0) {
            xpStorageUpgrade = false;
            xpStorageActive = false;
        }
        upgradeRitualTargetTier = clamp(tag.getInt(UPGRADE_RITUAL_TARGET_TIER_TAG), 0, 4);
        if (upgradeRitualTargetTier < 2) {
            upgradeRitualTargetTier = 0;
        }
        upgradeRitualStep = clamp(tag.getInt(UPGRADE_RITUAL_STEP_TAG), 0, 12);
        upgradeRitualTicks = clamp(tag.getInt(UPGRADE_RITUAL_TICKS_TAG), 0, 70);
        upgradeRitualOwner = tag.hasUUID(UPGRADE_RITUAL_OWNER_TAG)
                ? tag.getUUID(UPGRADE_RITUAL_OWNER_TAG)
                : null;
        activeBatchPlan = null;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put(ANALYZE_INPUT_TAG, analyzeInput.serializeNBT(registries));
        tag.put(STORAGE_TAG, storage.serializeNBT(registries));
        tag.put(FURNACE_INPUT_INVENTORY_TAG, furnaceInputInventory.serializeNBT(registries));
        tag.put(FURNACE_OUTPUT_INVENTORY_TAG, furnaceOutputInventory.serializeNBT(registries));
        tag.putBoolean(SMELT_ESSENCE_TAG, smeltEssenceEnabled);
        if (smeltEssenceOwner != null) {
            tag.putUUID(SMELT_ESSENCE_OWNER_TAG, smeltEssenceOwner);
        }
        if (furnaceOwner != null) {
            tag.putUUID(FURNACE_OWNER_TAG, furnaceOwner);
        }
        if (backpackOwner != null) {
            tag.putUUID(BACKPACK_OWNER_TAG, backpackOwner);
        }
        tag.putBoolean(OWNED_PROGRESS_MIGRATION_COMPLETE_TAG, ownedProgressMigrationComplete);
        tag.putInt(UNLOCKED_STORAGE_SLOTS_TAG, unlockedStorageSlots);
        tag.putInt(UNLOCKED_FURNACE_SLOTS_TAG, unlockedFurnaceSlotsPerSide);
        tag.putInt(ACTIVE_SPEED_TAG, activeSpeedLevel);
        tag.putInt(ACTIVE_BATCH_TAG, activeBatchLevel);
        tag.putBoolean(XP_STORAGE_UPGRADE_TAG, xpStorageUpgrade);
        tag.putBoolean(XP_STORAGE_ACTIVE_TAG, xpStorageActive);
        tag.putDouble(PENDING_EXPERIENCE_TAG, pendingExperience);
        tag.putDouble(STORED_EXPERIENCE_TAG, storedExperience);
        if (isUpgradeRitualActive()) {
            tag.putInt(UPGRADE_RITUAL_TARGET_TIER_TAG, upgradeRitualTargetTier);
            tag.putInt(UPGRADE_RITUAL_STEP_TAG, upgradeRitualStep);
            tag.putInt(UPGRADE_RITUAL_TICKS_TAG, upgradeRitualTicks);
            if (upgradeRitualOwner != null) {
                tag.putUUID(UPGRADE_RITUAL_OWNER_TAG, upgradeRitualOwner);
            }
        }
    }

    public void dropStoredItems() {
        if (level == null || level.isClientSide || preserveBackpackOnBreak || upgradeReplacementInProgress) {
            return;
        }
        dropHandler(analyzeInput);
        dropHandler(storage);
        dropHandler(furnaceInputInventory);
        dropHandler(furnaceOutputInventory);
        for (int slot = 0; slot < getContainerSize(); slot++) {
            ItemStack stack = removeItemNoUpdate(slot);
            if (!stack.isEmpty()) {
                Block.popResource(level, worldPosition, stack);
            }
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, MateriaTableBlockEntity table) {
        if (level instanceof ServerLevel serverLevel
                && MateriaTableRitualService.tick(serverLevel, pos, state, table)) {
            return;
        }
        int tableTier = table.tableTier();
        if (tableTier >= 3) {
            table.moveInputInventoryToFurnace(level);
            table.validateSmeltEssenceState(level);
            table.tickCustomFurnace(level);
            table.moveFurnaceOutputToOutputInventory();
        } else if (table.smeltEssenceEnabled) {
            table.setSmeltEssenceEnabled(false);
        }
        if (tableTier >= 2) {
            table.tickBackpackAutopickup(level);
        }
    }

    private void tickBackpackAutopickup(Level level) {
        if (!(level instanceof ServerLevel serverLevel) || backpackOwner == null) {
            return;
        }
        autopickupTicks++;
        if (autopickupTicks < AUTOPICKUP_INTERVAL_TICKS) {
            return;
        }
        autopickupTicks = 0;

        ServerPlayer owner = serverLevel.getServer().getPlayerList().getPlayer(backpackOwner);
        if (owner == null) {
            return;
        }
        int autopickupLevel = effectiveBackpackSetting(owner, BackpackExtraUpgrade.AUTOPICKUP);
        int radius = BackpackExtraUpgrade.autopickupRange(autopickupLevel);
        if (radius <= 0) {
            return;
        }

        int filterSlots = BackpackExtraUpgrade.filterSlotCount(
                effectiveBackpackSetting(owner, BackpackExtraUpgrade.FILTER)
        );
        boolean hasFilter = PlayerMateriaProgress.hasConfiguredBackpackFilter(owner, filterSlots);
        boolean voidEnabled = effectiveBackpackSetting(owner, BackpackExtraUpgrade.VOID) > 0
                && effectiveBackpackLevel(owner, BackpackExtraUpgrade.FILTER) >= 1;
        AABB area = new AABB(worldPosition).inflate(radius);
        for (ItemEntity itemEntity : serverLevel.getEntitiesOfClass(ItemEntity.class, area, entity -> !entity.isRemoved())) {
            ItemStack dropped = itemEntity.getItem();
            if (dropped.isEmpty()) {
                continue;
            }
            if (voidEnabled && PlayerMateriaProgress.matchesBackpackFilter(owner, dropped, true, filterSlots)) {
                itemEntity.discard();
                continue;
            }
            if (hasFilter && !PlayerMateriaProgress.matchesBackpackFilter(owner, dropped, false, filterSlots)) {
                continue;
            }
            ItemStack remaining = insertIntoBackpack(dropped.copy());
            if (remaining.getCount() == dropped.getCount()) {
                continue;
            }
            if (remaining.isEmpty()) {
                itemEntity.discard();
            } else {
                itemEntity.setItem(remaining);
            }
        }
    }

    private ItemStack insertIntoBackpack(ItemStack stack) {
        ItemStack remaining = stack;
        for (int slot = 0; slot < unlockedStorageSlots && !remaining.isEmpty(); slot++) {
            remaining = storage.insertItem(slot, remaining, false);
        }
        return remaining;
    }

    private int backpackStacksizeLevel() {
        if (!(level instanceof ServerLevel serverLevel) || backpackOwner == null) {
            return 0;
        }
        ServerPlayer owner = serverLevel.getServer().getPlayerList().getPlayer(backpackOwner);
        return owner == null ? 0 : effectiveBackpackSetting(owner, BackpackExtraUpgrade.STACKSIZE);
    }
    private void tickCustomFurnace(Level level) {
        ItemStack input = getItem(INPUT_SLOT);
        Optional<RecipeHolder<SmeltingRecipe>> recipe = findRecipe(level, input);
        int cookTime = cookTimeForSpeed(activeSpeedLevel);
        dataAccess.set(AbstractFurnaceBlockEntity.DATA_COOKING_TOTAL_TIME, cookTime);

        int litTime = dataAccess.get(AbstractFurnaceBlockEntity.DATA_LIT_TIME);
        if (smeltEssenceEnabled && canUseSmeltEssence()) {
            litTime = AbstractFurnaceBlockEntity.BURN_TIME_STANDARD;
            dataAccess.set(AbstractFurnaceBlockEntity.DATA_LIT_DURATION, AbstractFurnaceBlockEntity.BURN_TIME_STANDARD);
            dataAccess.set(AbstractFurnaceBlockEntity.DATA_LIT_TIME, litTime);
        } else if (litTime > 0) {
            litTime--;
            dataAccess.set(AbstractFurnaceBlockEntity.DATA_LIT_TIME, litTime);
        }

        if (recipe.isEmpty()) {
            activeBatchPlan = null;
            resetCookProgress();
            return;
        }

        BatchPlan plan = getOrCreateBatchPlan(level, input, recipe.get());
        if (plan == null) {
            resetCookProgress();
            return;
        }

        if (litTime <= 0 && !ignite()) {
            resetCookProgress();
            return;
        }

        dataAccess.set(AbstractFurnaceBlockEntity.DATA_COOKING_PROGRESS, dataAccess.get(AbstractFurnaceBlockEntity.DATA_COOKING_PROGRESS) + 1);
        if (dataAccess.get(AbstractFurnaceBlockEntity.DATA_COOKING_PROGRESS) >= cookTime) {
            completeBatch(level, plan);
            resetCookProgress();
            activeBatchPlan = null;
        }
    }

    private boolean ignite() {
        if (smeltEssenceEnabled && canUseSmeltEssence()) {
            dataAccess.set(AbstractFurnaceBlockEntity.DATA_LIT_DURATION, AbstractFurnaceBlockEntity.BURN_TIME_STANDARD);
            dataAccess.set(AbstractFurnaceBlockEntity.DATA_LIT_TIME, AbstractFurnaceBlockEntity.BURN_TIME_STANDARD);
            return true;
        }
        ItemStack fuel = getItem(FUEL_SLOT);
        int burnTime = fuel.getBurnTime(RecipeType.SMELTING);
        if (burnTime <= 0) {
            return false;
        }
        dataAccess.set(AbstractFurnaceBlockEntity.DATA_LIT_DURATION, burnTime);
        dataAccess.set(AbstractFurnaceBlockEntity.DATA_LIT_TIME, burnTime);
        fuel.shrink(1);
        setItem(FUEL_SLOT, fuel.isEmpty() ? ItemStack.EMPTY : fuel);
        setChangedAndUpdate();
        return true;
    }

    private BatchPlan getOrCreateBatchPlan(Level level, ItemStack input, RecipeHolder<SmeltingRecipe> recipe) {
        if (activeBatchPlan != null
                && ItemStack.isSameItemSameComponents(activeBatchPlan.input(), input)
                && input.getCount() >= activeBatchPlan.consumed()
                && canAffordSmeltEssence(activeBatchPlan.consumed(), level)
                && canFitOutput(activeBatchPlan.output())) {
            return activeBatchPlan;
        }
        activeBatchPlan = createBatchPlan(level, input, recipe);
        return activeBatchPlan;
    }

    private BatchPlan createBatchPlan(Level level, ItemStack input, RecipeHolder<SmeltingRecipe> recipeHolder) {
        int batchAmount = batchAmount(activeBatchLevel);
        int maxAmount = Math.min(batchAmount, input.getCount());
        if (smeltEssenceEnabled) {
            maxAmount = Math.min(maxAmount, affordableSmeltEssenceItems(level));
        }

        for (int amount = maxAmount; amount >= 1; amount--) {
            ItemStack totalOutput = ItemStack.EMPTY;
            for (int index = 0; index < amount; index++) {
                ItemStack produced = recipeHolder.value().assemble(
                        new SingleRecipeInput(input.copyWithCount(1)),
                        level.registryAccess()
                );
                if (produced.isEmpty()) {
                    return null;
                }
                if (totalOutput.isEmpty()) {
                    totalOutput = produced.copy();
                } else if (ItemStack.isSameItemSameComponents(totalOutput, produced)) {
                    totalOutput.grow(produced.getCount());
                } else {
                    return null;
                }
            }
            if (canFitOutput(totalOutput)) {
                double experience = recipeHolder.value().getExperience() * amount;
                return new BatchPlan(input.copyWithCount(1), totalOutput, amount, experience);
            }
        }
        return null;
    }

    private boolean completeBatch(Level level, BatchPlan plan) {
        ItemStack input = getItem(INPUT_SLOT);
        if (input.isEmpty()
                || input.getCount() < plan.consumed()
                || !ItemStack.isSameItemSameComponents(input, plan.input())
                || !canFitOutput(plan.output())) {
            return false;
        }

        ServerPlayer essenceOwner = null;
        long essenceCost = 0L;
        if (smeltEssenceEnabled) {
            essenceOwner = resolveSmeltEssenceOwner(level);
            essenceCost = (long) MateriaConfig.smeltEssenceCostPerItem() * plan.consumed();
            if (essenceOwner == null || PlayerEssence.get(essenceOwner) < essenceCost) {
                setSmeltEssenceEnabled(false);
                if (essenceOwner != null) {
                    PlayerEssence.syncOpenMenu(essenceOwner);
                }
                return false;
            }
        }

        ItemStack remaining = insertIntoOutputTargets(plan.output().copy(), false);
        if (!remaining.isEmpty()) {
            return false;
        }
        input.shrink(plan.consumed());
        setItem(INPUT_SLOT, input.isEmpty() ? ItemStack.EMPTY : input);
        if (xpStorageActive && xpStorageUpgrade) {
            storedExperience += plan.earnedExperience();
        } else {
            pendingExperience += plan.earnedExperience();
        }
        if (essenceOwner != null) {
            PlayerEssence.trySpend(essenceOwner, essenceCost);
            if (PlayerEssence.get(essenceOwner) < MateriaConfig.smeltEssenceCostPerItem()) {
                setSmeltEssenceEnabled(false);
            }
            PlayerEssence.syncOpenMenu(essenceOwner);
        }
        setChangedAndUpdate();
        return true;
    }

    private boolean canFitOutput(ItemStack output) {
        return insertIntoOutputTargets(output, true).isEmpty();
    }

    private ItemStack insertIntoOutputTargets(ItemStack stack, boolean simulate) {
        ItemStack remaining = stack.copy();
        ItemStack buffer = getItem(RESULT_SLOT);
        if (buffer.isEmpty()) {
            int inserted = Math.min(remaining.getCount(), Math.min(getMaxStackSize(), remaining.getMaxStackSize()));
            if (inserted > 0) {
                if (!simulate) {
                    setItem(RESULT_SLOT, remaining.copyWithCount(inserted));
                }
                remaining.shrink(inserted);
            }
        } else if (ItemStack.isSameItemSameComponents(buffer, remaining)) {
            int space = Math.min(getMaxStackSize(), buffer.getMaxStackSize()) - buffer.getCount();
            int inserted = Math.min(space, remaining.getCount());
            if (inserted > 0) {
                if (!simulate) {
                    buffer.grow(inserted);
                    setItem(RESULT_SLOT, buffer);
                }
                remaining.shrink(inserted);
            }
        }
        for (int slot = 0; slot < unlockedFurnaceSlotsPerSide && !remaining.isEmpty(); slot++) {
            remaining = furnaceOutputInventory.insertItem(slot, remaining, simulate);
        }
        return remaining;
    }

    private void moveInputInventoryToFurnace(Level level) {
        ItemStack furnaceInput = getItem(INPUT_SLOT);
        for (int slot = 0; slot < unlockedFurnaceSlotsPerSide; slot++) {
            ItemStack stack = furnaceInputInventory.getStackInSlot(slot);
            if (stack.isEmpty() || findRecipe(level, stack).isEmpty()) {
                continue;
            }
            if (!furnaceInput.isEmpty() && !ItemStack.isSameItemSameComponents(furnaceInput, stack)) {
                continue;
            }
            int limit = Math.min(getMaxStackSize(), stack.getMaxStackSize());
            int space = furnaceInput.isEmpty() ? limit : limit - furnaceInput.getCount();
            if (space <= 0) {
                continue;
            }
            int moved = Math.min(space, stack.getCount());
            if (furnaceInput.isEmpty()) {
                setItem(INPUT_SLOT, stack.copyWithCount(moved));
            } else {
                furnaceInput.grow(moved);
                setItem(INPUT_SLOT, furnaceInput);
            }
            stack.shrink(moved);
            furnaceInputInventory.setStackInSlot(slot, stack.isEmpty() ? ItemStack.EMPTY : stack);
            activeBatchPlan = null;
            setChangedAndUpdate();
            return;
        }
    }

    private void moveFurnaceOutputToOutputInventory() {
        ItemStack output = getItem(RESULT_SLOT);
        if (output.isEmpty()) {
            return;
        }
        ItemStack remaining = output.copy();
        for (int slot = 0; slot < unlockedFurnaceSlotsPerSide && !remaining.isEmpty(); slot++) {
            remaining = furnaceOutputInventory.insertItem(slot, remaining, false);
        }
        if (remaining.getCount() != output.getCount()) {
            setItem(RESULT_SLOT, remaining);
            setChangedAndUpdate();
        }
    }

    private void validateSmeltEssenceState(Level level) {
        if (!smeltEssenceEnabled) {
            return;
        }
        ServerPlayer owner = resolveSmeltEssenceOwner(level);
        if (owner == null || !canUseSmeltEssence()) {
            setSmeltEssenceEnabled(false);
            return;
        }
        if (PlayerEssence.get(owner) < MateriaConfig.smeltEssenceCostPerItem()) {
            setSmeltEssenceEnabled(false);
            PlayerEssence.syncOpenMenu(owner);
        }
    }

    private ServerPlayer resolveSmeltEssenceOwner(Level level) {
        if (!(level instanceof ServerLevel serverLevel) || smeltEssenceOwner == null) {
            return null;
        }
        return serverLevel.getServer().getPlayerList().getPlayer(smeltEssenceOwner);
    }

    private int affordableSmeltEssenceItems(Level level) {
        if (!smeltEssenceEnabled) {
            return Integer.MAX_VALUE;
        }
        ServerPlayer owner = resolveSmeltEssenceOwner(level);
        if (owner == null) {
            return 0;
        }
        int costPerItem = MateriaConfig.smeltEssenceCostPerItem();
        if (costPerItem <= 0) {
            return Integer.MAX_VALUE;
        }
        return (int) Math.min(Integer.MAX_VALUE, PlayerEssence.get(owner) / costPerItem);
    }

    private boolean canAffordSmeltEssence(int amount, Level level) {
        return !smeltEssenceEnabled || affordableSmeltEssenceItems(level) >= amount;
    }

    private void awardExperience(ServerLevel serverLevel, Player player, boolean stored) {
        double experience = stored ? storedExperience : pendingExperience;
        int claimable = Mth.floor((float) experience);
        if (claimable <= 0) {
            return;
        }
        if (stored) {
            storedExperience -= claimable;
        } else {
            pendingExperience -= claimable;
        }
        setChangedAndUpdate();
        ExperienceOrb.award(serverLevel, player.position(), claimable);
    }

    private Optional<RecipeHolder<SmeltingRecipe>> findRecipe(Level level, ItemStack input) {
        if (input.isEmpty()) {
            return Optional.empty();
        }
        return level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(input), level);
    }

    private static int cookTimeForSpeed(int speedLevel) {
        int clampedLevel = clamp(speedLevel, 0, 30);
        if (clampedLevel >= 30) {
            return Math.max(1, MateriaConfig.instantCookTimeTicks());
        }
        int base = MateriaConfig.baseCookTimeTicks();
        int level29 = MateriaConfig.level29CookTimeTicks();
        return Math.max(1, Math.round(base - ((base - level29) / 29.0F) * clampedLevel));
    }

    private static int batchAmount(int batchSmeltingLevel) {
        int clampedLevel = clamp(batchSmeltingLevel, 0, 8);
        return clampedLevel <= 0 ? 1 : clampedLevel * MateriaConfig.batchItemsPerLevel();
    }

    private void resetCookProgress() {
        if (dataAccess.get(AbstractFurnaceBlockEntity.DATA_COOKING_PROGRESS) != 0) {
            dataAccess.set(AbstractFurnaceBlockEntity.DATA_COOKING_PROGRESS, 0);
        }
    }

    private void setChangedAndUpdate() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    private ItemStackHandler handler(int slots) {
        return new ItemStackHandler(slots) {
            @Override
            protected void onContentsChanged(int slot) {
                setChangedAndUpdate();
            }
        };
    }

    private void dropHandler(ItemStackHandler handler) {
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            ItemStack stack = handler.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                Block.popResource(level, worldPosition, stack.copy());
                handler.setStackInSlot(slot, ItemStack.EMPTY);
            }
        }
    }

    private static void deserializeHandler(CompoundTag tag, String key, ItemStackHandler handler, HolderLookup.Provider registries) {
        if (tag.contains(key)) {
            handler.deserializeNBT(registries, tag.getCompound(key));
        }
    }

    private static void clearHandler(ItemStackHandler handler) {
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            handler.setStackInSlot(slot, ItemStack.EMPTY);
        }
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private record BatchPlan(ItemStack input, ItemStack output, int consumed, double earnedExperience) {
    }
}
