package com.materiareborn.menu;

import java.util.Optional;
import com.materiareborn.api.essence.EssenceAmount;
import com.materiareborn.block.MateriaTableBlock;
import com.materiareborn.blockentity.MateriaTableBlockEntity;
import com.materiareborn.config.MateriaConfig;
import com.materiareborn.essence.EssenceItemCatalog;
import com.materiareborn.essence.EssenceItemDefinition;
import com.materiareborn.essence.PlayerEssence;
import com.materiareborn.essence.PlayerEssenceKnowledge;
import com.materiareborn.progression.BackpackExtraUpgrade;
import com.materiareborn.progression.FurnaceExtraUpgrade;
import com.materiareborn.progression.MateriaTableProgression;
import com.materiareborn.progression.PlayerMateriaProgress;
import com.materiareborn.registry.ModMenuTypes;
import com.materiareborn.ritual.MateriaTableRitualBuilder;
import com.materiareborn.ritual.MateriaTableUpgrade;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultSlot;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

public final class MateriaTableMenu extends AbstractContainerMenu {
    private static final int ESSENCE_BUTTON_ID = 0;
    private static final int BACKPACK_BUTTON_ID = 1;
    private static final int FURNACE_BUTTON_ID = 2;
    private static final int PREVIOUS_BUTTON_ID = 3;
    private static final int NEXT_BUTTON_ID = 4;
    private static final int SMELT_ESSENCE_BUTTON_ID = 7;
    private static final int UPGRADE_BUTTON_ID = 8;
    private static final int UPGRADE_TEN_BUTTON_ID = 9;
    private static final int FURNACE_EXTRA_BUTTON_START_ID = 10;
    private static final int CLAIM_XP_BUTTON_ID = 15;
    private static final int FURNACE_EXTRA_SETTING_BUTTON_START_ID = 20;
    private static final int FURNACE_EXTRA_SETTING_BUTTON_STRIDE = 31;
    private static final int BACKPACK_EXTRA_BUTTON_START_ID = 200;
    private static final int BACKPACK_EXTRA_SETTING_BUTTON_START_ID = 220;
    private static final int BACKPACK_EXTRA_SETTING_BUTTON_STRIDE = 7;
    private static final int BACKPACK_FILTER_TOGGLE_BUTTON_ID = 16;
    private static final int BACKPACK_FILTER_ADD_FROM_PLAYER_BUTTON_START_ID = 2000;
    private static final int BACKPACK_FILTER_REMOVE_BUTTON_START_ID = 1100;
    private static final int BACKPACK_FILTER_RESET_BUTTON_ID = 1140;
    private static final int BACKPACK_FILTER_IGNORE_NBT_BUTTON_ID = 1141;
    private static final int BACKPACK_FILTER_IGNORE_DAMAGE_BUTTON_ID = 1142;
    private static final int RITUAL_BUILD_BUTTON_ID = 17;
    private static final int RITUAL_PREVIEW_BUTTON_ID = 18;
    private static final int ANALYZE_ITEM_BUTTON_ID = 2900;
    private static final int UNLOCK_ITEM_BUTTON_ID = 2901;
    private static final int SELL_ITEM_BUTTON_ID = 2902;
    private static final int AUTO_SELL_BUTTON_ID = 2903;
    private static final int ESSENCE_ITEM_PURCHASE_BUTTON_START_ID = 3000;
    private static final int ESSENCE_ITEM_PURCHASE_BUTTON_STRIDE = 4;
    private static final int ESSENCE_ITEM_REMOVE_UNLOCK_BUTTON_START_ID = 100_000;
    private static final int ESSENCE_UNLOCK_WORD_BITS = Integer.SIZE;
    private static final int ESSENCE_UNLOCK_WORD_COUNT =
            (EssenceItemCatalog.size() + ESSENCE_UNLOCK_WORD_BITS - 1) / ESSENCE_UNLOCK_WORD_BITS;

    private static final int ANALYZE_SLOT_COUNT = 1;
    private static final int STORAGE_SLOT_COUNT = 540;
    private static final int FURNACE_SLOT_COUNT = 3;
    private static final int FURNACE_INVENTORY_SLOT_COUNT = 18;
    private static final int FURNACE_ACTIVE_INVENTORY_SLOT_COUNT = 1;

    private static final int ANALYZE_START = 0;
    private static final int ANALYZE_END = ANALYZE_START + ANALYZE_SLOT_COUNT;
    private static final int ANALYZE_SLOT = ANALYZE_START;
    private static final int STORAGE_START = ANALYZE_END;
    private static final int STORAGE_END = STORAGE_START + STORAGE_SLOT_COUNT;
    private static final int FURNACE_START = STORAGE_END;
    private static final int FURNACE_INPUT_SLOT = FURNACE_START;
    private static final int FURNACE_FUEL_SLOT = FURNACE_START + 1;
    private static final int FURNACE_RESULT_SLOT = FURNACE_START + 2;
    private static final int FURNACE_END = FURNACE_START + FURNACE_SLOT_COUNT;
    private static final int FURNACE_INPUT_INVENTORY_START = FURNACE_END;
    private static final int FURNACE_INPUT_INVENTORY_END = FURNACE_INPUT_INVENTORY_START + FURNACE_INVENTORY_SLOT_COUNT;
    private static final int FURNACE_OUTPUT_INVENTORY_START = FURNACE_INPUT_INVENTORY_END;
    private static final int FURNACE_OUTPUT_INVENTORY_END = FURNACE_OUTPUT_INVENTORY_START + FURNACE_INVENTORY_SLOT_COUNT;
    private static final int PLAYER_INVENTORY_START = FURNACE_OUTPUT_INVENTORY_END;
    private static final int PLAYER_INVENTORY_END = PLAYER_INVENTORY_START + 27;
    private static final int PLAYER_HOTBAR_START = PLAYER_INVENTORY_END;
    private static final int PLAYER_HOTBAR_END = PLAYER_HOTBAR_START + 9;
    private static final int CRAFTING_START = PLAYER_HOTBAR_END;
    private static final int CRAFTING_END = CRAFTING_START + 9;
    private static final int CRAFTING_RESULT = CRAFTING_END;

    private static final int ANALYZE_SLOT_X = 62;
    private static final int ANALYZE_SLOT_Y = 176;
    private static final int STORAGE_GRID_X = 148;
    private static final int STORAGE_GRID_Y = 112;
    private static final int FURNACE_INPUT_X = 200;
    private static final int FURNACE_INPUT_Y = 100;
    private static final int FURNACE_FUEL_X = 200;
    private static final int FURNACE_FUEL_Y = 146;
    private static final int FURNACE_RESULT_X = 272;
    private static final int FURNACE_RESULT_Y = 118;
    private static final int FURNACE_INPUT_INVENTORY_X = 152;
    private static final int FURNACE_OUTPUT_INVENTORY_X = 288;
    private static final int FURNACE_INVENTORY_Y = 186;
    private static final int FURNACE_INVENTORY_COLUMNS = 6;
    private static final int PLAYER_INVENTORY_X = 148;
    private static final int PLAYER_INVENTORY_Y = 298;
    private static final int PLAYER_HOTBAR_Y = 356;
    private static final int FILTER_GHOST_X = -272;
    private static final int FILTER_GHOST_Y = 310;
    private static final int FILTER_GHOST_COLUMNS = 9;

    private final ContainerLevelAccess access;
    private final MateriaTableBlockEntity table;
    private final ContainerData furnaceData;
    private final Level level;
    private final Player player;
    private final CraftingContainer craftSlots = new TransientCraftingContainer(this, 3, 3);
    private final ResultContainer resultSlots = new ResultContainer();
    private final ItemStackHandler backpackFilterGhosts;

    private MateriaTableTab activeTab = MateriaTableTab.ESSENCE;
    private int backpackPage = 0;
    private boolean smeltEssenceEnabled = false;
    private boolean backpackFilterOpen;
    private boolean syncedBackpackFilterIgnoreNbt;
    private boolean syncedBackpackFilterIgnoreDamage;
    private boolean syncedAutoSellEnabled;
    private long displayedEssence;
    private int selectedEssenceItemIndex = -1;
    private int syncedSelectedEssenceAnalysis;
    private final int[] syncedEssenceUnlockWords = new int[ESSENCE_UNLOCK_WORD_COUNT];
    private int syncedUnlockedStorageSlots = MateriaConfig.initialBackpackSlots();
    private int syncedUnlockedFurnaceSlotsPerSide = MateriaConfig.initialFurnaceSlotsPerSide();
    private final int[] syncedFurnaceExtraLevels = new int[FurnaceExtraUpgrade.values().length];
    private final int[] syncedFurnaceExtraSettings = new int[FurnaceExtraUpgrade.values().length];
    private final int[] syncedBackpackExtraLevels = new int[BackpackExtraUpgrade.values().length];
    private final int[] syncedBackpackExtraSettings = new int[BackpackExtraUpgrade.values().length];
    private int syncedStoredExperience;
    private boolean syncedXpStorageAvailable;
    private String backpackSearchQuery = "";

    public static int storageStart() {
        return STORAGE_START;
    }

    public static int storageEnd() {
        return STORAGE_END;
    }

    public static int storageSlotCount() {
        return STORAGE_SLOT_COUNT;
    }

    public int getBackpackPage() {
        return this.backpackPage;
    }

    public int maxBackpackPage() {
        int maximumSlots = MateriaTableProgression.maxStorageSlots(tableTier());
        return maximumSlots <= 0 ? 0 : (maximumSlots - 1) / 78;
    }

    public int backpackPageCount() {
        return maxBackpackPage() + 1;
    }

    public MateriaTableMenu(
            int containerId,
            Inventory playerInventory,
            MateriaTableBlockEntity table,
            ContainerLevelAccess access
    ) {
        this(
                containerId,
                playerInventory,
                table.analyzeInput(),
                table.storage(),
                table.furnaceInputInventory(),
                table.furnaceOutputInventory(),
                table,
                table.furnaceData(),
                access,
                table
        );
    }

    private MateriaTableMenu(
            int containerId,
            Inventory playerInventory,
            IItemHandler analyzeInput,
            IItemHandler storage,
            IItemHandler furnaceInputInventory,
            IItemHandler furnaceOutputInventory,
            Container furnace,
            ContainerData furnaceData,
            ContainerLevelAccess access,
            MateriaTableBlockEntity table
    ) {
        super(ModMenuTypes.MATERIA_TABLE.get(), containerId);
        checkContainerSize(furnace, FURNACE_SLOT_COUNT);
        checkContainerDataCount(furnaceData, AbstractFurnaceBlockEntity.NUM_DATA_VALUES);

        this.access = access;
        this.table = table;
        this.furnaceData = furnaceData;
        this.level = playerInventory.player.level();
        this.player = playerInventory.player;
        this.smeltEssenceEnabled = table != null && table.isSmeltEssenceEnabled();
        this.backpackFilterGhosts = this.level.isClientSide
                ? new ItemStackHandler(PlayerMateriaProgress.MAX_BACKPACK_FILTER_SLOTS)
                : PlayerMateriaProgress.loadBackpackFilterGhosts(player);
        if (table != null && !this.level.isClientSide) {
            table.applyPlayerProgress(player);
            MateriaTableTab savedTab = MateriaTableTab.byId(PlayerMateriaProgress.activeTabId(player));
            this.activeTab = MateriaTableTab.byId(Math.min(savedTab.id(), tableTier() - 1));
        }
        if (!this.level.isClientSide) {
            this.selectedEssenceItemIndex = PlayerEssenceKnowledge.selectedItemIndex(player);
        }
        this.syncedUnlockedStorageSlots = PlayerMateriaProgress.storageSlots(player);
        this.syncedUnlockedFurnaceSlotsPerSide = PlayerMateriaProgress.furnaceSlotsPerSide(player);
        this.syncedStoredExperience = table == null ? 0 : table.storedExperienceWhole();
        this.syncedXpStorageAvailable = table != null && table.hasXpStorageUpgrade();

        addAnalyzeSlot(analyzeInput);
        addStorageSlots(storage);
        addFurnaceSlots(playerInventory, furnace);
        addFurnaceInventorySlots(furnaceInputInventory, furnaceOutputInventory);
        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);
        addCraftingSlots(playerInventory.player);
        addBackpackFilterGhostSlots();
        addDataSlot(createActiveTabDataSlot());
        addDataSlot(createBackpackFilterOpenDataSlot());
        addDataSlot(createBackpackFilterIgnoreNbtDataSlot());
        addDataSlot(createBackpackFilterIgnoreDamageDataSlot());
        addDataSlot(createSmeltEssenceDataSlot());
        addDataSlot(createEssenceLowDataSlot());
        addDataSlot(createEssenceHighDataSlot());
        addDataSlot(createAutoSellDataSlot());
        addDataSlot(createSelectedEssenceItemDataSlot());
        addDataSlot(createSelectedEssenceAnalysisDataSlot());
        for (int wordIndex = 0; wordIndex < ESSENCE_UNLOCK_WORD_COUNT; wordIndex++) {
            addDataSlot(createEssenceUnlockWordDataSlot(wordIndex));
        }
        addDataSlot(createUnlockedStorageSlotsDataSlot());
        addDataSlot(createUnlockedFurnaceSlotsDataSlot());
        for (FurnaceExtraUpgrade upgrade : FurnaceExtraUpgrade.values()) {
            addDataSlot(createFurnaceExtraDataSlot(upgrade));
            addDataSlot(createFurnaceExtraSettingDataSlot(upgrade));
        }
        for (BackpackExtraUpgrade upgrade : BackpackExtraUpgrade.values()) {
            addDataSlot(createBackpackExtraDataSlot(upgrade));
            addDataSlot(createBackpackExtraSettingDataSlot(upgrade));
        }
        addDataSlot(createStoredExperienceDataSlot());
        addDataSlot(createXpStorageAvailableDataSlot());
        addDataSlots(furnaceData);
    }

    public static MateriaTableMenu fromNetwork(
            int containerId,
            Inventory playerInventory,
            RegistryFriendlyByteBuf buffer
    ) {
        BlockPos pos = buffer.readBlockPos();
        Level level = playerInventory.player.level();
        BlockEntity blockEntity = level.getBlockEntity(pos);

        IItemHandler analyzeInput = new ItemStackHandler(ANALYZE_SLOT_COUNT);
        IItemHandler storage = new ItemStackHandler(STORAGE_SLOT_COUNT);
        IItemHandler furnaceInputInventory = new ItemStackHandler(FURNACE_INVENTORY_SLOT_COUNT);
        IItemHandler furnaceOutputInventory = new ItemStackHandler(FURNACE_INVENTORY_SLOT_COUNT);
        Container furnace = new SimpleContainer(FURNACE_SLOT_COUNT);
        ContainerData furnaceData = new SimpleContainerData(AbstractFurnaceBlockEntity.NUM_DATA_VALUES);

        if (blockEntity instanceof MateriaTableBlockEntity table) {
            analyzeInput = table.analyzeInput();
            storage = table.storage();
            furnaceInputInventory = table.furnaceInputInventory();
            furnaceOutputInventory = table.furnaceOutputInventory();
            furnace = table;
            furnaceData = table.furnaceData();
        }

        return new MateriaTableMenu(
                containerId,
                playerInventory,
                analyzeInput,
                storage,
                furnaceInputInventory,
                furnaceOutputInventory,
                furnace,
                furnaceData,
                ContainerLevelAccess.create(level, pos),
                blockEntity instanceof MateriaTableBlockEntity table ? table : null
        );
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) {
            return ItemStack.EMPTY;
        }

        Slot slot = slots.get(index);
        if (!slot.isActive() || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();

        if (isEssenceTab()) {
            if (!quickMoveEssenceTab(index, stack, original, slot)) {
                return ItemStack.EMPTY;
            }
        } else if (isBackpackTab()) {
            if (!quickMoveStorageTab(index, stack)) {
                return ItemStack.EMPTY;
            }
        } else if (isFurnaceTab()) {
            if (!quickMoveFurnaceTab(index, stack, original, slot)) {
                return ItemStack.EMPTY;
            }
        }

        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if (stack.getCount() == original.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTake(player, stack);
        return original;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        return switch (id) {
            case ESSENCE_BUTTON_ID -> setActiveTab(MateriaTableTab.ESSENCE);
            case BACKPACK_BUTTON_ID -> setActiveTab(MateriaTableTab.BACKPACK);
            case FURNACE_BUTTON_ID -> setActiveTab(MateriaTableTab.FURNACE);
            case PREVIOUS_BUTTON_ID -> setActiveTab(activeTab.previous());
            case NEXT_BUTTON_ID -> setActiveTab(activeTab.next());
            case RITUAL_BUILD_BUTTON_ID -> tryBuildUpgradeRitual(player);
            case RITUAL_PREVIEW_BUTTON_ID -> previewUpgradeRitual(player);
            case ANALYZE_ITEM_BUTTON_ID -> analyzeEssenceItem(player);
            case UNLOCK_ITEM_BUTTON_ID -> unlockEssenceItem(player);
            case SELL_ITEM_BUTTON_ID -> sellEssenceItem(player);
            case AUTO_SELL_BUTTON_ID -> toggleAutoSell(player);
            case SMELT_ESSENCE_BUTTON_ID -> toggleSmeltEssence();
            case UPGRADE_BUTTON_ID -> tryUpgrade(player, 1);
            case UPGRADE_TEN_BUTTON_ID -> tryUpgrade(player, 10);
            case CLAIM_XP_BUTTON_ID -> claimStoredExperience(player);
            case BACKPACK_FILTER_TOGGLE_BUTTON_ID -> toggleBackpackFilter();
            case BACKPACK_FILTER_RESET_BUTTON_ID -> resetBackpackFilterGhosts(player);
            case BACKPACK_FILTER_IGNORE_NBT_BUTTON_ID -> toggleBackpackFilterIgnoreNbt();
            case BACKPACK_FILTER_IGNORE_DAMAGE_BUTTON_ID -> toggleBackpackFilterIgnoreDamage();
            case 5 -> {
                if (backpackPage > 0) {
                    backpackPage--;
                    yield true;
                }
                yield false;
            }
            case 6 -> {
                if (backpackPage < maxBackpackPage()) {
                    backpackPage++;
                    yield true;
                }
                yield false;
            }
            default -> {
                int purchaseOffset = id - ESSENCE_ITEM_PURCHASE_BUTTON_START_ID;
                int purchaseRange = EssenceItemCatalog.size() * ESSENCE_ITEM_PURCHASE_BUTTON_STRIDE;
                if (purchaseOffset >= 0 && purchaseOffset < purchaseRange) {
                    int purchaseIndex = purchaseOffset / ESSENCE_ITEM_PURCHASE_BUTTON_STRIDE;
                    int purchaseMode = purchaseOffset % ESSENCE_ITEM_PURCHASE_BUTTON_STRIDE;
                    boolean bulkPurchase = (purchaseMode & 1) != 0;
                    boolean purchaseToCursor = (purchaseMode & 2) != 0;
                    yield purchaseEssenceItem(player, purchaseIndex, bulkPurchase, purchaseToCursor);
                }
                int removeUnlockIndex = id - ESSENCE_ITEM_REMOVE_UNLOCK_BUTTON_START_ID;
                if (removeUnlockIndex >= 0 && removeUnlockIndex < EssenceItemCatalog.size()) {
                    yield removeEssenceUnlock(player, removeUnlockIndex);
                }
                int removeFilterSlot = id - BACKPACK_FILTER_REMOVE_BUTTON_START_ID;
                if (removeFilterSlot >= 0 && removeFilterSlot < PlayerMateriaProgress.MAX_BACKPACK_FILTER_SLOTS) {
                    yield removeBackpackFilterGhost(player, removeFilterSlot);
                }
                int playerSlotIndex = id - BACKPACK_FILTER_ADD_FROM_PLAYER_BUTTON_START_ID;
                if (playerSlotIndex >= 0 && playerSlotIndex < slots.size()) {
                    yield addBackpackFilterGhost(player, playerSlotIndex);
                }
                int backpackSettingOffset = id - BACKPACK_EXTRA_SETTING_BUTTON_START_ID;
                int backpackSettingRange = BackpackExtraUpgrade.values().length * BACKPACK_EXTRA_SETTING_BUTTON_STRIDE;
                if (backpackSettingOffset >= 0 && backpackSettingOffset < backpackSettingRange) {
                    int extraIndex = backpackSettingOffset / BACKPACK_EXTRA_SETTING_BUTTON_STRIDE;
                    int settingLevel = backpackSettingOffset % BACKPACK_EXTRA_SETTING_BUTTON_STRIDE;
                    yield setBackpackExtraSetting(player, BackpackExtraUpgrade.values()[extraIndex], settingLevel);
                }
                int backpackExtraIndex = id - BACKPACK_EXTRA_BUTTON_START_ID;
                if (backpackExtraIndex >= 0 && backpackExtraIndex < BackpackExtraUpgrade.values().length) {
                    yield tryPurchaseBackpackExtra(player, BackpackExtraUpgrade.values()[backpackExtraIndex]);
                }
                int settingOffset = id - FURNACE_EXTRA_SETTING_BUTTON_START_ID;
                int settingRange = FurnaceExtraUpgrade.values().length * FURNACE_EXTRA_SETTING_BUTTON_STRIDE;
                if (settingOffset >= 0 && settingOffset < settingRange) {
                    int extraIndex = settingOffset / FURNACE_EXTRA_SETTING_BUTTON_STRIDE;
                    int settingLevel = settingOffset % FURNACE_EXTRA_SETTING_BUTTON_STRIDE;
                    yield setFurnaceExtraSetting(player, FurnaceExtraUpgrade.values()[extraIndex], settingLevel);
                }
                int extraIndex = id - FURNACE_EXTRA_BUTTON_START_ID;
                if (extraIndex >= 0 && extraIndex < FurnaceExtraUpgrade.values().length) {
                    yield tryPurchaseFurnaceExtra(player, FurnaceExtraUpgrade.values()[extraIndex]);
                }
                yield false;
            }
        };
    }

    @Override
    public void broadcastChanges() {
        autoSellUnlockedInput();
        super.broadcastChanges();
    }

    private void autoSellUnlockedInput() {
        if (player.level().isClientSide
                || activeTab != MateriaTableTab.ESSENCE
                || !PlayerEssenceKnowledge.autoSellEnabled(player)) {
            return;
        }
        Slot inputSlot = getSlot(ANALYZE_SLOT);
        ItemStack input = inputSlot.getItem();
        int index = EssenceItemCatalog.indexOf(input);
        if (!isEssenceItemAvailable(index)) {
            return;
        }
        EssenceItemDefinition definition = EssenceItemCatalog.get(index);
        if (!PlayerEssenceKnowledge.isUnlocked(player, definition)) {
            return;
        }

        selectEssenceItem(index);
        long earned = safeMultiply(definition.sellValue(), input.getCount());
        inputSlot.setByPlayer(ItemStack.EMPTY);
        PlayerEssence.add(player, earned);
        displayedEssence = PlayerEssence.get(player);
    }
    @Override
    public boolean canDragTo(Slot slot) {
        return slot.isActive() && super.canDragTo(slot);
    }

    @Override
    public boolean stillValid(Player player) {
        return access.evaluate((level, pos) ->
                level.getBlockState(pos).getBlock() instanceof MateriaTableBlock
                        && level.getBlockEntity(pos) == table
                        && player.distanceToSqr(
                                pos.getX() + 0.5D,
                                pos.getY() + 0.5D,
                                pos.getZ() + 0.5D
                        ) <= 64.0D,
                true
        );
    }

    public MateriaTableTab activeTab() {
        return activeTab;
    }

    public boolean isEssenceTab() {
        return activeTab == MateriaTableTab.ESSENCE;
    }

    public boolean isBackpackTab() {
        return activeTab == MateriaTableTab.BACKPACK;
    }

    public boolean isFurnaceTab() {
        return activeTab == MateriaTableTab.FURNACE;
    }

    public int tableTier() {
        return table == null
                ? 1
                : MateriaTableProgression.tableTier(table.getBlockState().getBlock());
    }

    public boolean isTabUnlocked(MateriaTableTab tab) {
        return tableTier() >= tab.requiredTableTier();
    }

    public boolean isSmeltEssenceEnabled() {
        if (!player.level().isClientSide && table != null) {
            return table.isSmeltEssenceEnabled();
        }
        return smeltEssenceEnabled;
    }

    public MateriaTableBlockEntity tableBlockEntity() {
        return table;
    }

    public boolean canUseSmeltEssence() {
        return isTabUnlocked(MateriaTableTab.FURNACE)
                && MateriaConfig.smeltEssenceEnabled()
                && unlockedFurnaceSlotCount() >= MateriaTableProgression.smeltEssenceUnlockedSlots();
    }

    public boolean canUseFurnaceExtras() {
        return MateriaTableProgression.areFurnaceExtrasUnlocked(tableTier(), unlockedFurnaceSlotsPerSide());
    }

    public boolean hasBackpackFilterUpgrade() {
        return backpackExtraLevel(BackpackExtraUpgrade.FILTER) > 0;
    }

    public boolean isBackpackFilterOpen() {
        return backpackFilterOpen;
    }

    public int backpackFilterSlotCount() {
        return Math.min(
                PlayerMateriaProgress.MAX_ACTIVE_BACKPACK_FILTER_SLOTS,
                BackpackExtraUpgrade.filterSlotCount(backpackExtraSetting(BackpackExtraUpgrade.FILTER))
        );
    }

    public static int backpackFilterToggleButtonId() {
        return BACKPACK_FILTER_TOGGLE_BUTTON_ID;
    }

    public ItemStack backpackFilterGhostItem(int filterSlot) {
        if (filterSlot < 0 || filterSlot >= PlayerMateriaProgress.MAX_BACKPACK_FILTER_SLOTS) {
            return ItemStack.EMPTY;
        }
        return backpackFilterGhosts.getStackInSlot(filterSlot);
    }
    public boolean isBackpackFilterIgnoringNbt() {
        return player.level().isClientSide
                ? syncedBackpackFilterIgnoreNbt
                : PlayerMateriaProgress.backpackFilterIgnoreNbt(player);
    }

    public boolean isBackpackFilterIgnoringDamage() {
        return player.level().isClientSide
                ? syncedBackpackFilterIgnoreDamage
                : PlayerMateriaProgress.backpackFilterIgnoreDamage(player);
    }

    public static int backpackFilterResetButtonId() {
        return BACKPACK_FILTER_RESET_BUTTON_ID;
    }

    public static int backpackFilterIgnoreNbtButtonId() {
        return BACKPACK_FILTER_IGNORE_NBT_BUTTON_ID;
    }

    public static int backpackFilterIgnoreDamageButtonId() {
        return BACKPACK_FILTER_IGNORE_DAMAGE_BUTTON_ID;
    }
    public static int backpackFilterRemoveButtonId(int filterSlot) {
        return BACKPACK_FILTER_REMOVE_BUTTON_START_ID + filterSlot;
    }

    public static int backpackFilterAddFromPlayerButtonId(int playerSlotIndex) {
        return BACKPACK_FILTER_ADD_FROM_PLAYER_BUTTON_START_ID + playerSlotIndex;
    }

    public boolean isPlayerInventorySlot(int slotIndex) {
        return isPlayerSlot(slotIndex);
    }
    public boolean canUseBackpackExtras() {
        return MateriaTableProgression.areBackpackExtrasUnlocked(tableTier(), unlockedStorageSlots());
    }

    public static int furnaceInputInventoryStart() {
        return FURNACE_INPUT_INVENTORY_START;
    }

    public static int furnaceInputInventoryEnd() {
        return FURNACE_INPUT_INVENTORY_END;
    }

    public static int furnaceOutputInventoryStart() {
        return FURNACE_OUTPUT_INVENTORY_START;
    }

    public static int furnaceOutputInventoryEnd() {
        return FURNACE_OUTPUT_INVENTORY_END;
    }

    public static int furnaceInventorySlotCount() {
        return FURNACE_INVENTORY_SLOT_COUNT;
    }

    public int furnaceInputInventoryItemCount() {
        return countItems(FURNACE_INPUT_INVENTORY_START, FURNACE_INPUT_INVENTORY_START + unlockedFurnaceSlotsPerSide());
    }

    public int furnaceOutputInventoryItemCount() {
        return countItems(FURNACE_OUTPUT_INVENTORY_START, FURNACE_OUTPUT_INVENTORY_START + unlockedFurnaceSlotsPerSide());
    }

    public int furnaceInputInventoryOccupiedSlots() {
        return countOccupiedSlots(FURNACE_INPUT_INVENTORY_START, FURNACE_INPUT_INVENTORY_START + unlockedFurnaceSlotsPerSide());
    }

    public int furnaceOutputInventoryOccupiedSlots() {
        return countOccupiedSlots(FURNACE_OUTPUT_INVENTORY_START, FURNACE_OUTPUT_INVENTORY_START + unlockedFurnaceSlotsPerSide());
    }

    public int furnaceInventoryItemCapacity() {
        return unlockedFurnaceSlotsPerSide() * 64;
    }

    public int unlockedStorageSlots() {
        return Math.min(
                syncedUnlockedStorageSlots,
                MateriaTableProgression.maxStorageSlots(tableTier())
        );
    }

    public int storageItemCount() {
        return countItems(STORAGE_START, STORAGE_START + unlockedStorageSlots());
    }

    public int backpackEffectiveStackLimit() {
        return BackpackExtraUpgrade.backpackStackLimit(backpackExtraSetting(BackpackExtraUpgrade.STACKSIZE));
    }

    public int storageItemCapacity() {
        return unlockedStorageSlots() * backpackEffectiveStackLimit();
    }

    public int unlockedFurnaceSlotsPerSide() {
        return Math.min(
                syncedUnlockedFurnaceSlotsPerSide,
                MateriaTableProgression.maxFurnaceSlotsPerSide(tableTier())
        );
    }

    public int unlockedFurnaceSlotCount() {
        return unlockedFurnaceSlotsPerSide() * 2;
    }

    public int nextUpgradeCost() {
        return nextUpgradeCost(1);
    }

    public int nextUpgradeCost(int upgradeCount) {
        int upgrades = clampedUpgradeCount(upgradeCount);
        if (upgrades <= 0) {
            return 0;
        }
        int currentUpgradeCount = switch (activeTab) {
            case BACKPACK -> Math.max(0, unlockedStorageSlots() - MateriaConfig.initialBackpackSlots());
            case FURNACE -> Math.max(0, unlockedFurnaceSlotsPerSide() - MateriaConfig.initialFurnaceSlotsPerSide());
            case ESSENCE -> 0;
        };
        long cost = 0;
        for (int step = 1; step <= upgrades; step++) {
            cost += upgradeCostIncrement(currentUpgradeCount + step);
        }
        return cost > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) cost;
    }

    private int clampedUpgradeCount(int requestedUpgradeCount) {
        int requested = Math.max(1, requestedUpgradeCount);
        int available = switch (activeTab) {
            case BACKPACK -> MateriaTableProgression.maxStorageSlots(tableTier()) - unlockedStorageSlots();
            case FURNACE -> MateriaTableProgression.maxFurnaceSlotsPerSide(tableTier()) - unlockedFurnaceSlotsPerSide();
            case ESSENCE -> 0;
        };
        return Math.min(requested, Math.max(0, available));
    }

    public boolean canAffordNextUpgrade() {
        return canAffordNextUpgrade(1);
    }

    public boolean canAffordNextUpgrade(int upgradeCount) {
        int cost = nextUpgradeCost(upgradeCount);
        return cost > 0 && currentEssence() >= cost;
    }

    public int nextSingleUpgradeCost() {
        int nextUpgrade = switch (activeTab) {
            case BACKPACK -> Math.max(1, unlockedStorageSlots() - MateriaConfig.initialBackpackSlots() + 1);
            case FURNACE -> Math.max(1, unlockedFurnaceSlotsPerSide() - MateriaConfig.initialFurnaceSlotsPerSide() + 1);
            case ESSENCE -> 1;
        };
        return upgradeCostIncrement(nextUpgrade);
    }

    public int backpackExtraMaxLevel(BackpackExtraUpgrade upgrade) {
        return MateriaTableProgression.maxBackpackExtraLevel(tableTier(), upgrade);
    }

    public int backpackExtraOverallMaxLevel(BackpackExtraUpgrade upgrade) {
        return upgrade.maxLevel();
    }

    public boolean isBackpackExtraLimitedByTableTier(BackpackExtraUpgrade upgrade) {
        return backpackExtraMaxLevel(upgrade) < backpackExtraOverallMaxLevel(upgrade);
    }

    public int backpackExtraLevel(BackpackExtraUpgrade upgrade) {
        if (!canUseBackpackExtras()) {
            return 0;
        }
        int purchasedLevel = player.level().isClientSide
                ? syncedBackpackExtraLevels[upgrade.ordinal()]
                : PlayerMateriaProgress.backpackExtraLevel(player, upgrade);
        return Math.min(purchasedLevel, backpackExtraMaxLevel(upgrade));
    }

    public int backpackExtraSetting(BackpackExtraUpgrade upgrade) {
        int setting = player.level().isClientSide
                ? syncedBackpackExtraSettings[upgrade.ordinal()]
                : PlayerMateriaProgress.backpackExtraSetting(player, upgrade);
        return Math.min(setting, backpackExtraLevel(upgrade));
    }

    public int nextBackpackExtraCost(BackpackExtraUpgrade upgrade) {
        int level = backpackExtraLevel(upgrade);
        return level >= backpackExtraMaxLevel(upgrade) ? 0 : upgrade.nextLevelCost(level);
    }

    public int backpackStackLimit(ItemStack stack) {
        return BackpackExtraUpgrade.backpackStackLimit(backpackExtraSetting(BackpackExtraUpgrade.STACKSIZE), stack);
    }

    public boolean canAffordBackpackExtra(BackpackExtraUpgrade upgrade) {
        int cost = nextBackpackExtraCost(upgrade);
        return cost > 0 && currentEssence() >= cost;
    }

    public static int backpackExtraButtonId(BackpackExtraUpgrade upgrade) {
        return BACKPACK_EXTRA_BUTTON_START_ID + upgrade.ordinal();
    }

    public static int backpackExtraSettingButtonId(BackpackExtraUpgrade upgrade, int level) {
        int clampedLevel = Mth.clamp(level, 0, BACKPACK_EXTRA_SETTING_BUTTON_STRIDE - 1);
        return BACKPACK_EXTRA_SETTING_BUTTON_START_ID + upgrade.ordinal() * BACKPACK_EXTRA_SETTING_BUTTON_STRIDE + clampedLevel;
    }
    public int furnaceExtraMaxLevel(FurnaceExtraUpgrade upgrade) {
        return MateriaTableProgression.maxFurnaceExtraLevel(tableTier(), upgrade);
    }

    public int furnaceExtraOverallMaxLevel(FurnaceExtraUpgrade upgrade) {
        return upgrade.maxLevel();
    }

    public boolean isFurnaceExtraLimitedByTableTier(FurnaceExtraUpgrade upgrade) {
        return furnaceExtraMaxLevel(upgrade) < furnaceExtraOverallMaxLevel(upgrade);
    }

    public int furnaceExtraLevel(FurnaceExtraUpgrade upgrade) {
        if (!canUseFurnaceExtras() || !upgrade.isAvailableForPurchase()) {
            return 0;
        }
        int purchasedLevel = player.level().isClientSide
                ? syncedFurnaceExtraLevels[upgrade.ordinal()]
                : PlayerMateriaProgress.furnaceExtraLevel(player, upgrade);
        return Math.min(purchasedLevel, furnaceExtraMaxLevel(upgrade));
    }

    public int furnaceExtraSetting(FurnaceExtraUpgrade upgrade) {
        int setting = player.level().isClientSide
                ? syncedFurnaceExtraSettings[upgrade.ordinal()]
                : PlayerMateriaProgress.furnaceExtraSetting(player, upgrade);
        return Math.min(setting, furnaceExtraLevel(upgrade));
    }

    public int nextFurnaceExtraCost(FurnaceExtraUpgrade upgrade) {
        int level = furnaceExtraLevel(upgrade);
        return level >= furnaceExtraMaxLevel(upgrade) ? 0 : upgrade.nextLevelCost(level);
    }

    public boolean canAffordFurnaceExtra(FurnaceExtraUpgrade upgrade) {
        int cost = nextFurnaceExtraCost(upgrade);
        return cost > 0 && currentEssence() >= cost;
    }

    public boolean hasXpStorage() {
        return furnaceExtraSetting(FurnaceExtraUpgrade.XP_STORAGE) > 0;
    }

    public int storedExperience() {
        return syncedStoredExperience;
    }

    public static int furnaceExtraButtonId(FurnaceExtraUpgrade upgrade) {
        return FURNACE_EXTRA_BUTTON_START_ID + upgrade.ordinal();
    }

    public static int claimXpButtonId() {
        return CLAIM_XP_BUTTON_ID;
    }
    public static int furnaceExtraSettingButtonId(FurnaceExtraUpgrade upgrade, int level) {
        int clampedLevel = Mth.clamp(level, 0, FURNACE_EXTRA_SETTING_BUTTON_STRIDE - 1);
        return FURNACE_EXTRA_SETTING_BUTTON_START_ID + upgrade.ordinal() * FURNACE_EXTRA_SETTING_BUTTON_STRIDE + clampedLevel;
    }

    public int activePage() {
        return activeTab.id() + 1;
    }

    public int pageCount() {
        return MateriaTableTab.count();
    }

    public EssenceAmount displayedEssence() {
        return EssenceAmount.of(currentEssence());
    }

    private long currentEssence() {
        return player.level().isClientSide ? displayedEssence : PlayerEssence.get(player);
    }

    public String formattedDisplayedEssence() {
        return displayedEssence().toString();
    }

    public int essenceCatalogSize() {
        return EssenceItemCatalog.size();
    }

    public String essenceCatalogId(int index) {
        return index >= 0 && index < EssenceItemCatalog.size()
                ? EssenceItemCatalog.get(index).catalogId()
                : "";
    }

    public ItemStack essenceCatalogStack(int index) {
        return index >= 0 && index < EssenceItemCatalog.size()
                ? EssenceItemCatalog.get(index).createStack(player.level().registryAccess())
                : ItemStack.EMPTY;
    }

    public long essenceItemSellValue(int index) {
        return isEssenceItemAvailable(index)
                ? EssenceItemCatalog.get(index).sellValue()
                : 0L;
    }

    public long essenceItemPurchaseCost(int index) {
        return isEssenceItemAvailable(index)
                ? EssenceItemCatalog.get(index).purchaseCost()
                : 0L;
    }

    public boolean isEssenceItemAvailable(int index) {
        return index >= 0
                && index < EssenceItemCatalog.size()
                && EssenceItemCatalog.get(index).tableLevel() <= tableTier();
    }

    public int essenceAnalysisProgress(int index) {
        if (!isEssenceItemAvailable(index)) {
            return 0;
        }
        if (!player.level().isClientSide) {
            return PlayerEssenceKnowledge.analysisProgress(player, EssenceItemCatalog.get(index));
        }
        return index == selectedEssenceItemIndex ? syncedSelectedEssenceAnalysis : 0;
    }

    public int essenceAnalysisRequired(int index) {
        return isEssenceItemAvailable(index)
                ? EssenceItemCatalog.get(index).requiredAnalysis()
                : 0;
    }

    public boolean isEssenceItemUnlocked(int index) {
        if (!isEssenceItemAvailable(index)) {
            return false;
        }
        if (!player.level().isClientSide) {
            return PlayerEssenceKnowledge.isUnlocked(player, EssenceItemCatalog.get(index));
        }
        int wordIndex = index / ESSENCE_UNLOCK_WORD_BITS;
        int bitMask = 1 << (index % ESSENCE_UNLOCK_WORD_BITS);
        return (syncedEssenceUnlockWords[wordIndex] & bitMask) != 0;
    }

    public int selectedEssenceItemIndex() {
        if (!player.level().isClientSide) {
            refreshSelectedEssenceItem();
        }
        return selectedEssenceItemIndex;
    }


    public int inputEssenceItemIndex() {
        int index = EssenceItemCatalog.indexOf(getSlot(ANALYZE_SLOT).getItem());
        return isEssenceItemAvailable(index) ? index : -1;
    }

    public int carriedEssenceItemIndex() {
        int index = EssenceItemCatalog.indexOf(getCarried());
        return isEssenceItemAvailable(index) ? index : -1;
    }

    public boolean canAnalyzeEssenceItem() {
        ItemStack input = getSlot(ANALYZE_SLOT).getItem();
        int index = EssenceItemCatalog.indexOf(input);
        return isEssenceTab()
                && isEssenceItemAvailable(index)
                && essenceAnalysisProgress(index) < essenceAnalysisRequired(index);
    }

    public boolean canUnlockEssenceItem() {
        int index = selectedEssenceItemIndex();
        return isEssenceTab()
                && isEssenceItemAvailable(index)
                && essenceAnalysisProgress(index) >= essenceAnalysisRequired(index)
                && !isEssenceItemUnlocked(index);
    }

    public boolean canSellEssenceItem() {
        return isEssenceTab()
                && isEssenceItemAvailable(EssenceItemCatalog.indexOf(getSlot(ANALYZE_SLOT).getItem()));
    }

    public boolean isAutoSellEnabled() {
        return player.level().isClientSide
                ? syncedAutoSellEnabled
                : PlayerEssenceKnowledge.autoSellEnabled(player);
    }

    public boolean canRemoveEssenceUnlock(int index) {
        return isEssenceTab() && isEssenceItemUnlocked(index);
    }

    public boolean canPurchaseEssenceItem(int index) {
        long value = essenceItemPurchaseCost(index);
        return isEssenceTab()
                && isEssenceItemUnlocked(index)
                && value > 0L
                && currentEssence() >= value;
    }
    public static String formatEssenceValue(long value) {
        return String.format(java.util.Locale.ROOT, "%,d", Math.max(0L, value));
    }

    public static int analyzeItemButtonId() {
        return ANALYZE_ITEM_BUTTON_ID;
    }

    public static int unlockItemButtonId() {
        return UNLOCK_ITEM_BUTTON_ID;
    }

    public static int sellItemButtonId() {
        return SELL_ITEM_BUTTON_ID;
    }

    public static int autoSellButtonId() {
        return AUTO_SELL_BUTTON_ID;
    }

    public static int essenceItemPurchaseButtonId(int index, boolean bulk, boolean toCursor) {
        int clampedIndex = Mth.clamp(index, 0, Math.max(0, EssenceItemCatalog.size() - 1));
        int mode = (bulk ? 1 : 0) | (toCursor ? 2 : 0);
        return ESSENCE_ITEM_PURCHASE_BUTTON_START_ID
                + clampedIndex * ESSENCE_ITEM_PURCHASE_BUTTON_STRIDE
                + mode;
    }

    public static int essenceItemRemoveUnlockButtonId(int index) {
        int clampedIndex = Mth.clamp(index, 0, Math.max(0, EssenceItemCatalog.size() - 1));
        return ESSENCE_ITEM_REMOVE_UNLOCK_BUTTON_START_ID + clampedIndex;
    }
    public boolean isAnalyzeTabVisible() {
        return isEssenceTab();
    }

    public static int essenceButtonId() {
        return ESSENCE_BUTTON_ID;
    }

    public static int backpackButtonId() {
        return BACKPACK_BUTTON_ID;
    }

    public static int furnaceButtonId() {
        return FURNACE_BUTTON_ID;
    }

    public static int smeltEssenceButtonId() {
        return SMELT_ESSENCE_BUTTON_ID;
    }

    public static int upgradeButtonId() {
        return UPGRADE_BUTTON_ID;
    }

    public static int upgradeTenButtonId() {
        return UPGRADE_TEN_BUTTON_ID;
    }

    public static int ritualBuildButtonId() {
        return RITUAL_BUILD_BUTTON_ID;
    }

    public static int ritualPreviewButtonId() {
        return RITUAL_PREVIEW_BUTTON_ID;
    }

    public Optional<MateriaTableUpgrade> nextTableUpgrade() {
        if (table == null) {
            return Optional.empty();
        }
        return MateriaTableUpgrade.forSource(table.getBlockState().getBlock());
    }

    public BlockPos tableBlockPos() {
        return table == null ? BlockPos.ZERO : table.getBlockPos();
    }

    public boolean canPrepareUpgradeRitual() {
        return nextTableUpgrade().isPresent();
    }

    public void setBackpackSearchQuery(String query) {
        backpackSearchQuery = query == null ? "" : query.toLowerCase(java.util.Locale.ROOT).trim();
    }

    public int litProgressPixels(int height) {
        int litDuration = furnaceData.get(AbstractFurnaceBlockEntity.DATA_LIT_DURATION);
        if (litDuration == 0) {
            litDuration = AbstractFurnaceBlockEntity.BURN_TIME_STANDARD;
        }
        return Mth.clamp(furnaceData.get(AbstractFurnaceBlockEntity.DATA_LIT_TIME) * height / litDuration, 0, height);
    }

    public int cookProgressPixels(int width) {
        int cookTime = furnaceData.get(AbstractFurnaceBlockEntity.DATA_COOKING_PROGRESS);
        int cookTimeTotal = furnaceData.get(AbstractFurnaceBlockEntity.DATA_COOKING_TOTAL_TIME);
        if (cookTime == 0 || cookTimeTotal == 0) {
            return 0;
        }
        return Mth.clamp(cookTime * width / cookTimeTotal, 0, width);
    }

    public String getFurnaceBurnTimeText() {
        ItemStack inputStack = getSlot(FURNACE_INPUT_SLOT).getItem();
        int inputCount = inputStack.isEmpty() ? 0 : inputStack.getCount();
        if (inputCount <= 0) {
            return "00:00";
        }
        int cookTime = furnaceData.get(AbstractFurnaceBlockEntity.DATA_COOKING_PROGRESS);
        int cookTimeTotal = furnaceData.get(AbstractFurnaceBlockEntity.DATA_COOKING_TOTAL_TIME);
        
        int totalTicks;
        if (cookTimeTotal > 0) {
            totalTicks = (cookTimeTotal - cookTime) + (inputCount - 1) * cookTimeTotal;
        } else {
            totalTicks = inputCount * 200;
        }
        
        int totalSeconds = totalTicks / 20;
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format(java.util.Locale.US, "%02d:%02d", minutes, seconds);
    }

    public static int previousButtonId() {
        return PREVIOUS_BUTTON_ID;
    }

    public static int nextButtonId() {
        return NEXT_BUTTON_ID;
    }

    private boolean quickMoveEssenceTab(int index, ItemStack stack, ItemStack original, Slot slot) {
        if (index == ANALYZE_SLOT) {
            return moveItemStackTo(stack, PLAYER_INVENTORY_START, PLAYER_HOTBAR_END, true);
        }
        if (index == CRAFTING_RESULT) {
            if (!moveItemStackTo(stack, PLAYER_INVENTORY_START, PLAYER_HOTBAR_END, true)) {
                return false;
            }
            slot.onQuickCraft(stack, original);
            return true;
        }
        if (index >= CRAFTING_START && index < CRAFTING_END) {
            return moveItemStackTo(stack, PLAYER_INVENTORY_START, PLAYER_HOTBAR_END, true);
        }
        if (isPlayerSlot(index)) {
            if (moveItemStackTo(stack, ANALYZE_START, ANALYZE_END, false)) {
                return true;
            }
            if (moveItemStackTo(stack, CRAFTING_START, CRAFTING_END, false)) {
                return true;
            }
            return false;
        }
        return false;
    }

    private boolean quickMoveStorageTab(int index, ItemStack stack) {
        if (isStorageSlot(index)) {
            return moveItemStackTo(stack, PLAYER_INVENTORY_START, PLAYER_HOTBAR_END, true);
        }
        if (isPlayerSlot(index)) {
            return moveItemStackTo(stack, STORAGE_START, STORAGE_END, false);
        }
        return false;
    }

    private boolean quickMoveFurnaceTab(int index, ItemStack stack, ItemStack original, Slot slot) {
        if (index == FURNACE_RESULT_SLOT) {
            if (!moveItemStackTo(stack, FURNACE_OUTPUT_INVENTORY_START, FURNACE_OUTPUT_INVENTORY_START + unlockedFurnaceSlotsPerSide(), false)
                    && !moveItemStackTo(stack, PLAYER_INVENTORY_START, PLAYER_HOTBAR_END, true)) {
                return false;
            }
            slot.onQuickCraft(stack, original);
            return true;
        }

        if (index == FURNACE_INPUT_SLOT || index == FURNACE_FUEL_SLOT) {
            return moveItemStackTo(stack, PLAYER_INVENTORY_START, PLAYER_HOTBAR_END, false);
        }

        if (isFurnaceInventorySlot(index)) {
            return moveItemStackTo(stack, PLAYER_INVENTORY_START, PLAYER_HOTBAR_END, true);
        }

        if (!isPlayerSlot(index)) {
            return false;
        }

        if (canSmelt(stack) && moveItemStackTo(stack, FURNACE_INPUT_SLOT, FURNACE_INPUT_SLOT + 1, false)) {
            return true;
        }
        if (!isSmeltEssenceEnabled() && isFuel(stack) && moveItemStackTo(stack, FURNACE_FUEL_SLOT, FURNACE_FUEL_SLOT + 1, false)) {
            return true;
        }
        if (moveItemStackTo(stack, FURNACE_INPUT_INVENTORY_START, FURNACE_INPUT_INVENTORY_START + unlockedFurnaceSlotsPerSide(), false)) {
            return true;
        }
        if (index >= PLAYER_INVENTORY_START && index < PLAYER_INVENTORY_END) {
            return moveItemStackTo(stack, PLAYER_HOTBAR_START, PLAYER_HOTBAR_END, false);
        }
        if (index >= PLAYER_HOTBAR_START && index < PLAYER_HOTBAR_END) {
            return moveItemStackTo(stack, PLAYER_INVENTORY_START, PLAYER_INVENTORY_END, false);
        }
        return false;
    }

    private boolean canSmelt(ItemStack stack) {
        return level.getRecipeManager()
                .getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(stack), level)
                .isPresent();
    }

    private boolean isFuel(ItemStack stack) {
        return stack.getBurnTime(RecipeType.SMELTING) > 0 || stack.is(Items.BUCKET);
    }

    private boolean setActiveTab(MateriaTableTab tab) {
        if (!isTabUnlocked(tab)) {
            return false;
        }
        activeTab = tab;
        if (!player.level().isClientSide) {
            PlayerMateriaProgress.setActiveTabId(player, tab.id());
        }
        broadcastChanges();
        return true;
    }

    private boolean previewUpgradeRitual(Player player) {
        if (player.level().isClientSide) {
            return true;
        }
        if (table == null || !(player instanceof ServerPlayer serverPlayer)) {
            return false;
        }
        MateriaTableRitualBuilder.preview(serverPlayer, table);
        return true;
    }

    private boolean tryBuildUpgradeRitual(Player player) {
        if (player.level().isClientSide) {
            return true;
        }
        if (table == null || !(player instanceof ServerPlayer serverPlayer)) {
            return false;
        }
        MateriaTableRitualBuilder.tryBuild(serverPlayer, table);
        return true;
    }
    private void refreshSelectedEssenceItem() {
        if (player.level().isClientSide) {
            return;
        }
        ItemStack input = getSlot(ANALYZE_SLOT).getItem();
        if (input.isEmpty()) {
            selectedEssenceItemIndex = PlayerEssenceKnowledge.selectedItemIndex(player);
            return;
        }
        selectEssenceItem(EssenceItemCatalog.indexOf(input));
    }

    private void selectEssenceItem(int index) {
        int selected = index >= 0 && index < EssenceItemCatalog.size() ? index : -1;
        if (selectedEssenceItemIndex == selected) {
            return;
        }
        selectedEssenceItemIndex = selected;
        if (!player.level().isClientSide) {
            PlayerEssenceKnowledge.setSelectedItem(player, selected);
        }
    }

    private boolean analyzeEssenceItem(Player player) {
        if (player.level().isClientSide) {
            return true;
        }
        if (activeTab != MateriaTableTab.ESSENCE) {
            return false;
        }
        Slot inputSlot = getSlot(ANALYZE_SLOT);
        ItemStack input = inputSlot.getItem();
        int index = EssenceItemCatalog.indexOf(input);
        if (!isEssenceItemAvailable(index)) {
            return false;
        }

        EssenceItemDefinition definition = EssenceItemCatalog.get(index);
        selectEssenceItem(index);
        int missing = definition.requiredAnalysis()
                - PlayerEssenceKnowledge.analysisProgress(player, definition);
        int consumed = Math.min(input.getCount(), Math.max(0, missing));
        if (consumed <= 0) {
            return false;
        }

        ItemStack remainder = input.copy();
        remainder.shrink(consumed);
        inputSlot.setByPlayer(remainder);
        PlayerEssenceKnowledge.addAnalysis(player, definition, consumed);
        broadcastChanges();
        return true;
    }

    private boolean unlockEssenceItem(Player player) {
        if (player.level().isClientSide) {
            return true;
        }
        if (activeTab != MateriaTableTab.ESSENCE) {
            return false;
        }
        refreshSelectedEssenceItem();
        if (!isEssenceItemAvailable(selectedEssenceItemIndex)) {
            return false;
        }
        boolean unlocked = PlayerEssenceKnowledge.unlock(
                player,
                EssenceItemCatalog.get(selectedEssenceItemIndex)
        );
        if (unlocked) {
            broadcastChanges();
        }
        return unlocked;
    }
    private boolean removeEssenceUnlock(Player player, int index) {
        if (player.level().isClientSide) {
            return true;
        }
        if (activeTab != MateriaTableTab.ESSENCE
                || !isEssenceItemAvailable(index)
                || !PlayerEssenceKnowledge.isUnlocked(player, EssenceItemCatalog.get(index))) {
            return false;
        }
        selectEssenceItem(index);
        boolean removed = PlayerEssenceKnowledge.removeUnlock(player, EssenceItemCatalog.get(index));
        if (removed) {
            broadcastChanges();
        }
        return removed;
    }

    private boolean toggleAutoSell(Player player) {
        if (player.level().isClientSide) {
            return true;
        }
        if (activeTab != MateriaTableTab.ESSENCE) {
            return false;
        }
        syncedAutoSellEnabled = PlayerEssenceKnowledge.toggleAutoSell(player);
        broadcastChanges();
        return true;
    }

    private boolean sellEssenceItem(Player player) {
        if (player.level().isClientSide) {
            return true;
        }
        if (activeTab != MateriaTableTab.ESSENCE) {
            return false;
        }
        Slot inputSlot = getSlot(ANALYZE_SLOT);
        ItemStack input = inputSlot.getItem();
        int index = EssenceItemCatalog.indexOf(input);
        if (!isEssenceItemAvailable(index) || input.isEmpty()) {
            return false;
        }

        EssenceItemDefinition definition = EssenceItemCatalog.get(index);
        selectEssenceItem(index);
        long earned = safeMultiply(definition.sellValue(), input.getCount());
        inputSlot.setByPlayer(ItemStack.EMPTY);
        PlayerEssence.add(player, earned);
        displayedEssence = PlayerEssence.get(player);
        broadcastChanges();
        return true;
    }

    private boolean purchaseEssenceItem(Player player, int index, boolean bulk, boolean toCursor) {
        if (player.level().isClientSide) {
            return true;
        }
        if (activeTab != MateriaTableTab.ESSENCE || !isEssenceItemAvailable(index)) {
            return false;
        }

        EssenceItemDefinition definition = EssenceItemCatalog.get(index);
        long unitCost = definition.purchaseCost();
        if (!PlayerEssenceKnowledge.isUnlocked(player, definition) || unitCost <= 0L) {
            return false;
        }

        ItemStack generated = definition.createStack(player.level().registryAccess());
        int requested = bulk ? 64 : 1;
        long affordable = PlayerEssence.get(player) / unitCost;
        int amount = (int) Math.min(requested, affordable);
        if (amount <= 0) {
            return false;
        }

        if (toCursor) {
            ItemStack carried = getCarried();
            if (!carried.isEmpty() && !ItemStack.isSameItemSameComponents(carried, generated)) {
                return false;
            }
            int carriedCount = carried.isEmpty() ? 0 : carried.getCount();
            amount = Math.min(amount, generated.getMaxStackSize() - carriedCount);
            if (amount <= 0 || !PlayerEssence.trySpend(player, safeMultiply(unitCost, amount))) {
                return false;
            }
            ItemStack result = carried.isEmpty() ? generated.copy() : carried.copy();
            result.setCount(carriedCount + amount);
            setCarried(result);
        } else {
            long reservedCost = safeMultiply(unitCost, amount);
            if (!PlayerEssence.trySpend(player, reservedCost)) {
                return false;
            }
            ItemStack insertion = generated.copyWithCount(amount);
            player.getInventory().add(insertion);
            int inserted = amount - insertion.getCount();
            if (inserted < amount) {
                PlayerEssence.add(player, safeMultiply(unitCost, amount - inserted));
            }
            if (inserted <= 0) {
                displayedEssence = PlayerEssence.get(player);
                broadcastChanges();
                return false;
            }
        }

        selectEssenceItem(index);
        displayedEssence = PlayerEssence.get(player);
        broadcastChanges();
        return true;
    }
    private static long safeMultiply(long value, int count) {
        return count > 0 && value > Long.MAX_VALUE / count ? Long.MAX_VALUE : value * count;
    }
    private boolean toggleSmeltEssence() {
        if (player.level().isClientSide) {
            return true;
        }
        if (!canUseSmeltEssence()) {
            return false;
        }
        boolean nextValue = !isSmeltEssenceEnabled();
        if (nextValue && PlayerEssence.get(player) < MateriaConfig.smeltEssenceCostPerItem()) {
            displayedEssence = PlayerEssence.get(player);
            broadcastChanges();
            return false;
        }
        smeltEssenceEnabled = nextValue;
        if (table != null) {
            table.setSmeltEssenceEnabled(smeltEssenceEnabled, smeltEssenceEnabled ? player : null);
        }
        displayedEssence = PlayerEssence.get(player);
        broadcastChanges();
        return true;
    }

    private boolean canConfigureBackpackFilter() {
        return activeTab == MateriaTableTab.BACKPACK
                && canUseBackpackExtras()
                && hasBackpackFilterUpgrade();
    }

    private boolean toggleBackpackFilter() {
        if (player.level().isClientSide || !canConfigureBackpackFilter()) {
            return false;
        }
        backpackFilterOpen = !backpackFilterOpen;
        broadcastChanges();
        return true;
    }

    private boolean addBackpackFilterGhost(Player player, int playerSlotIndex) {
        if (player.level().isClientSide
                || !canConfigureBackpackFilter()
                || !backpackFilterOpen
                || !isPlayerSlot(playerSlotIndex)) {
            return false;
        }
        ItemStack source = slots.get(playerSlotIndex).getItem();
        if (source.isEmpty()) {
            return false;
        }
        int activeSlots = backpackFilterSlotCount();
        for (int slot = 0; slot < activeSlots; slot++) {
            if (ItemStack.isSameItemSameComponents(backpackFilterGhosts.getStackInSlot(slot), source)) {
                return true;
            }
        }
        for (int slot = 0; slot < activeSlots; slot++) {
            if (backpackFilterGhosts.getStackInSlot(slot).isEmpty()) {
                backpackFilterGhosts.setStackInSlot(slot, source.copyWithCount(1));
                PlayerMateriaProgress.saveBackpackFilterGhosts(player, backpackFilterGhosts);
                broadcastChanges();
                return true;
            }
        }
        return false;
    }

    private boolean removeBackpackFilterGhost(Player player, int filterSlot) {
        if (player.level().isClientSide
                || !canConfigureBackpackFilter()
                || !backpackFilterOpen
                || filterSlot < 0 || filterSlot >= backpackFilterSlotCount()) {
            return false;
        }
        if (backpackFilterGhosts.getStackInSlot(filterSlot).isEmpty()) {
            return false;
        }
        backpackFilterGhosts.setStackInSlot(filterSlot, ItemStack.EMPTY);
        PlayerMateriaProgress.saveBackpackFilterGhosts(player, backpackFilterGhosts);
        broadcastChanges();
        return true;
    }
    private boolean resetBackpackFilterGhosts(Player player) {
        if (player.level().isClientSide || !canConfigureBackpackFilter() || !backpackFilterOpen) {
            return false;
        }
        for (int slot = 0; slot < PlayerMateriaProgress.MAX_BACKPACK_FILTER_SLOTS; slot++) {
            backpackFilterGhosts.setStackInSlot(slot, ItemStack.EMPTY);
        }
        PlayerMateriaProgress.saveBackpackFilterGhosts(player, backpackFilterGhosts);
        broadcastChanges();
        return true;
    }

    private boolean toggleBackpackFilterIgnoreNbt() {
        if (player.level().isClientSide
                || !canConfigureBackpackFilter()
                || backpackExtraLevel(BackpackExtraUpgrade.FILTER) < 3) {
            return false;
        }
        PlayerMateriaProgress.setBackpackFilterIgnoreNbt(player, !PlayerMateriaProgress.backpackFilterIgnoreNbt(player));
        syncedBackpackFilterIgnoreNbt = PlayerMateriaProgress.backpackFilterIgnoreNbt(player);
        broadcastChanges();
        return true;
    }

    private boolean toggleBackpackFilterIgnoreDamage() {
        if (player.level().isClientSide
                || !canConfigureBackpackFilter()
                || backpackExtraLevel(BackpackExtraUpgrade.FILTER) < 4) {
            return false;
        }
        PlayerMateriaProgress.setBackpackFilterIgnoreDamage(player, !PlayerMateriaProgress.backpackFilterIgnoreDamage(player));
        syncedBackpackFilterIgnoreDamage = PlayerMateriaProgress.backpackFilterIgnoreDamage(player);
        broadcastChanges();
        return true;
    }
    private boolean setBackpackExtraSetting(Player player, BackpackExtraUpgrade upgrade, int level) {
        if (player.level().isClientSide
                || activeTab != MateriaTableTab.BACKPACK
                || !canUseBackpackExtras()
                || backpackExtraMaxLevel(upgrade) <= 0
                || backpackExtraLevel(upgrade) <= 0) {
            return false;
        }
        PlayerMateriaProgress.setBackpackExtraSetting(
                player,
                upgrade,
                Math.min(level, backpackExtraMaxLevel(upgrade))
        );
        syncedBackpackExtraSettings[upgrade.ordinal()] = PlayerMateriaProgress.backpackExtraSetting(player, upgrade);
        broadcastChanges();
        return true;
    }

    private boolean tryPurchaseBackpackExtra(Player player, BackpackExtraUpgrade upgrade) {
        if (player.level().isClientSide || activeTab != MateriaTableTab.BACKPACK || !canUseBackpackExtras()) {
            return false;
        }
        if (backpackExtraMaxLevel(upgrade) <= 0
                || backpackExtraLevel(upgrade) >= backpackExtraMaxLevel(upgrade)) {
            return false;
        }
        if (!PlayerMateriaProgress.hasBackpackUpgradeDependencies(player, upgrade)) {
            player.displayClientMessage(net.minecraft.network.chat.Component.translatable(
                    "message.materia_reborn.upgrade_requires." + PlayerMateriaProgress.missingBackpackUpgradeDependency(player, upgrade)
            ), true);
            broadcastChanges();
            return false;
        }
        int cost = nextBackpackExtraCost(upgrade);
        if (cost <= 0 || !PlayerEssence.trySpend(player, cost)) {
            broadcastChanges();
            return false;
        }
        if (!PlayerMateriaProgress.unlockBackpackExtra(
                player,
                upgrade,
                backpackExtraMaxLevel(upgrade)
        )) {
            PlayerEssence.add(player, cost);
            broadcastChanges();
            return false;
        }
        syncedBackpackExtraLevels[upgrade.ordinal()] = PlayerMateriaProgress.backpackExtraLevel(player, upgrade);
        syncedBackpackExtraSettings[upgrade.ordinal()] = PlayerMateriaProgress.backpackExtraSetting(player, upgrade);
        displayedEssence = PlayerEssence.get(player);
        broadcastChanges();
        return true;
    }
    private boolean setFurnaceExtraSetting(Player player, FurnaceExtraUpgrade upgrade, int level) {
        if (player.level().isClientSide
                || activeTab != MateriaTableTab.FURNACE
                || !canUseFurnaceExtras()
                || !upgrade.isAvailableForPurchase()
                || furnaceExtraMaxLevel(upgrade) <= 0
                || furnaceExtraLevel(upgrade) <= 0) {
            return false;
        }
        PlayerMateriaProgress.setFurnaceExtraSetting(
                player,
                upgrade,
                Math.min(level, furnaceExtraMaxLevel(upgrade))
        );
        if (table != null) {
            table.applyPlayerProgress(player);
        }
        syncedFurnaceExtraSettings[upgrade.ordinal()] = PlayerMateriaProgress.furnaceExtraSetting(player, upgrade);
        broadcastChanges();
        return true;
    }
    private boolean tryPurchaseFurnaceExtra(Player player, FurnaceExtraUpgrade upgrade) {
        if (player.level().isClientSide || activeTab != MateriaTableTab.FURNACE || !canUseFurnaceExtras()) {
            return false;
        }
        if (!upgrade.isAvailableForPurchase()
                || furnaceExtraMaxLevel(upgrade) <= 0
                || furnaceExtraLevel(upgrade) >= furnaceExtraMaxLevel(upgrade)) {
            return false;
        }
        int cost = nextFurnaceExtraCost(upgrade);
        if (cost <= 0 || !PlayerEssence.trySpend(player, cost)) {
            broadcastChanges();
            return false;
        }
        if (!PlayerMateriaProgress.unlockFurnaceExtra(
                player,
                upgrade,
                furnaceExtraMaxLevel(upgrade)
        )) {
            PlayerEssence.add(player, cost);
            broadcastChanges();
            return false;
        }
        if (table != null) {
            table.applyPlayerProgress(player);
        }
        syncedFurnaceExtraLevels[upgrade.ordinal()] = PlayerMateriaProgress.furnaceExtraLevel(player, upgrade);
        syncedFurnaceExtraSettings[upgrade.ordinal()] = PlayerMateriaProgress.furnaceExtraSetting(player, upgrade);
        displayedEssence = PlayerEssence.get(player);
        broadcastChanges();
        return true;
    }

    private boolean claimStoredExperience(Player player) {
        if (player.level().isClientSide
                || activeTab != MateriaTableTab.FURNACE
                || !canUseFurnaceExtras()
                || !hasXpStorage()
                || table == null
                || !table.hasXpStorageUpgrade()) {
            return false;
        }
        boolean claimed = player instanceof net.minecraft.server.level.ServerPlayer serverPlayer && table.claimStoredExperience(serverPlayer);
        if (claimed) {
            syncedStoredExperience = table.storedExperienceWhole();
        }
        broadcastChanges();
        return claimed;
    }
    private boolean tryUpgrade(Player player, int requestedUpgradeCount) {
        if (player.level().isClientSide) {
            return true;
        }
        if (table == null
                || activeTab == MateriaTableTab.ESSENCE
                || !isTabUnlocked(activeTab)) {
            return false;
        }
        int upgrades = clampedUpgradeCount(requestedUpgradeCount);
        if (upgrades <= 0) {
            return false;
        }
        int cost = nextUpgradeCost(upgrades);
        if (!PlayerEssence.trySpend(player, cost)) {
            broadcastChanges();
            return false;
        }

        int completed = 0;
        for (int upgrade = 0; upgrade < upgrades; upgrade++) {
            boolean upgraded = activeTab == MateriaTableTab.BACKPACK
                    ? PlayerMateriaProgress.unlockStorageSlot(
                            player,
                            MateriaTableProgression.maxStorageSlots(tableTier())
                    )
                    : PlayerMateriaProgress.unlockFurnaceSlotPair(
                            player,
                            MateriaTableProgression.maxFurnaceSlotsPerSide(tableTier())
                    );
            if (!upgraded) {
                break;
            }
            completed++;
        }
        if (completed == 0) {
            PlayerEssence.add(player, cost);
            broadcastChanges();
            return false;
        }
        table.applyPlayerProgress(player);
        syncedUnlockedStorageSlots = PlayerMateriaProgress.storageSlots(player);
        syncedUnlockedFurnaceSlotsPerSide = PlayerMateriaProgress.furnaceSlotsPerSide(player);
        displayedEssence = PlayerEssence.get(player);
        broadcastChanges();
        return true;
    }

    private int upgradeCostIncrement(int upgradeNumber) {
        return switch (activeTab) {
            case BACKPACK -> MateriaConfig.backpackSlotUpgradeCost(upgradeNumber);
            case FURNACE -> MateriaConfig.furnaceSlotUpgradeCost(upgradeNumber);
            case ESSENCE -> 0;
        };
    }

    private boolean isStorageSlot(int index) {
        return index >= STORAGE_START && index < STORAGE_END;
    }

    private boolean isPlayerSlot(int index) {
        return index >= PLAYER_INVENTORY_START && index < PLAYER_HOTBAR_END;
    }

    private boolean isFurnaceInventorySlot(int index) {
        return index >= FURNACE_INPUT_INVENTORY_START && index < FURNACE_OUTPUT_INVENTORY_END;
    }

    private int countItems(int start, int end) {
        int count = 0;
        for (int slotIndex = start; slotIndex < end; slotIndex++) {
            ItemStack stack = getSlot(slotIndex).getItem();
            if (!stack.isEmpty()) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private int countOccupiedSlots(int start, int end) {
        int count = 0;
        for (int slotIndex = start; slotIndex < end; slotIndex++) {
            if (getSlot(slotIndex).hasItem()) {
                count++;
            }
        }
        return count;
    }

    private void addAnalyzeSlot(IItemHandler analyzeInput) {
        addSlot(new TabAwareItemHandlerSlot(
                analyzeInput,
                0,
                ANALYZE_SLOT_X,
                ANALYZE_SLOT_Y,
                this::isEssenceTab
        ));
    }

    private DataSlot createBackpackFilterIgnoreNbtDataSlot() {
        return new DataSlot() {
            @Override
            public int get() {
                return PlayerMateriaProgress.backpackFilterIgnoreNbt(player) ? 1 : 0;
            }

            @Override
            public void set(int value) {
                syncedBackpackFilterIgnoreNbt = value != 0;
            }
        };
    }

    private DataSlot createBackpackFilterIgnoreDamageDataSlot() {
        return new DataSlot() {
            @Override
            public int get() {
                return PlayerMateriaProgress.backpackFilterIgnoreDamage(player) ? 1 : 0;
            }

            @Override
            public void set(int value) {
                syncedBackpackFilterIgnoreDamage = value != 0;
            }
        };
    }
    private DataSlot createBackpackFilterOpenDataSlot() {
        return new DataSlot() {
            @Override
            public int get() {
                return backpackFilterOpen ? 1 : 0;
            }

            @Override
            public void set(int value) {
                backpackFilterOpen = value != 0;
            }
        };
    }
    private DataSlot createActiveTabDataSlot() {
        return new DataSlot() {
            @Override
            public int get() {
                return activeTab.id();
            }

            @Override
            public void set(int value) {
                activeTab = MateriaTableTab.byId(value);
            }
        };
    }

    private DataSlot createSmeltEssenceDataSlot() {
        return new DataSlot() {
            @Override
            public int get() {
                return table != null && table.isSmeltEssenceEnabled() ? 1 : 0;
            }

            @Override
            public void set(int value) {
                smeltEssenceEnabled = value != 0;
            }
        };
    }

    private DataSlot createAutoSellDataSlot() {
        return new DataSlot() {
            @Override
            public int get() {
                return PlayerEssenceKnowledge.autoSellEnabled(player) ? 1 : 0;
            }

            @Override
            public void set(int value) {
                syncedAutoSellEnabled = value != 0;
            }
        };
    }

    private DataSlot createEssenceLowDataSlot() {
        return new DataSlot() {
            @Override
            public int get() {
                return (int) PlayerEssence.get(player);
            }

            @Override
            public void set(int value) {
                displayedEssence = (displayedEssence & 0xFFFFFFFF00000000L) | Integer.toUnsignedLong(value);
            }
        };
    }

    private DataSlot createEssenceHighDataSlot() {
        return new DataSlot() {
            @Override
            public int get() {
                return (int) (PlayerEssence.get(player) >>> 32);
            }

            @Override
            public void set(int value) {
                displayedEssence = (Integer.toUnsignedLong(value) << 32) | (displayedEssence & 0xFFFFFFFFL);
            }
        };
    }

    private DataSlot createSelectedEssenceItemDataSlot() {
        return new DataSlot() {
            @Override
            public int get() {
                refreshSelectedEssenceItem();
                return selectedEssenceItemIndex;
            }

            @Override
            public void set(int value) {
                selectedEssenceItemIndex = value >= 0 && value < EssenceItemCatalog.size() ? value : -1;
            }
        };
    }

    private DataSlot createSelectedEssenceAnalysisDataSlot() {
        return new DataSlot() {
            @Override
            public int get() {
                refreshSelectedEssenceItem();
                return isEssenceItemAvailable(selectedEssenceItemIndex)
                        ? PlayerEssenceKnowledge.analysisProgress(
                                player,
                                EssenceItemCatalog.get(selectedEssenceItemIndex)
                        )
                        : 0;
            }

            @Override
            public void set(int value) {
                int maximum = isEssenceItemAvailable(selectedEssenceItemIndex)
                        ? EssenceItemCatalog.get(selectedEssenceItemIndex).requiredAnalysis()
                        : 0;
                syncedSelectedEssenceAnalysis = Mth.clamp(value, 0, maximum);
            }
        };
    }

    private DataSlot createEssenceUnlockWordDataSlot(int wordIndex) {
        return new DataSlot() {
            @Override
            public int get() {
                int firstIndex = wordIndex * ESSENCE_UNLOCK_WORD_BITS;
                int endIndex = Math.min(firstIndex + ESSENCE_UNLOCK_WORD_BITS, EssenceItemCatalog.size());
                return PlayerEssenceKnowledge.unlockedWord(player, firstIndex, endIndex);
            }

            @Override
            public void set(int value) {
                syncedEssenceUnlockWords[wordIndex] = value;
            }
        };
    }
    private DataSlot createUnlockedStorageSlotsDataSlot() {
        return new DataSlot() {
            @Override
            public int get() {
                return PlayerMateriaProgress.storageSlots(player);
            }

            @Override
            public void set(int value) {
                syncedUnlockedStorageSlots = value;
            }
        };
    }

    private DataSlot createUnlockedFurnaceSlotsDataSlot() {
        return new DataSlot() {
            @Override
            public int get() {
                return PlayerMateriaProgress.furnaceSlotsPerSide(player);
            }

            @Override
            public void set(int value) {
                syncedUnlockedFurnaceSlotsPerSide = value;
            }
        };
    }

    private DataSlot createBackpackExtraDataSlot(BackpackExtraUpgrade upgrade) {
        return new DataSlot() {
            @Override
            public int get() {
                return PlayerMateriaProgress.backpackExtraLevel(player, upgrade);
            }

            @Override
            public void set(int value) {
                syncedBackpackExtraLevels[upgrade.ordinal()] = Mth.clamp(value, 0, upgrade.maxLevel());
            }
        };
    }

    private DataSlot createBackpackExtraSettingDataSlot(BackpackExtraUpgrade upgrade) {
        return new DataSlot() {
            @Override
            public int get() {
                return PlayerMateriaProgress.backpackExtraSetting(player, upgrade);
            }

            @Override
            public void set(int value) {
                syncedBackpackExtraSettings[upgrade.ordinal()] = Mth.clamp(value, 0, upgrade.maxLevel());
            }
        };
    }
    private DataSlot createFurnaceExtraDataSlot(FurnaceExtraUpgrade upgrade) {
        return new DataSlot() {
            @Override
            public int get() {
                return PlayerMateriaProgress.furnaceExtraLevel(player, upgrade);
            }

            @Override
            public void set(int value) {
                syncedFurnaceExtraLevels[upgrade.ordinal()] = Mth.clamp(value, 0, upgrade.maxLevel());
            }
        };
    }

    private DataSlot createFurnaceExtraSettingDataSlot(FurnaceExtraUpgrade upgrade) {
        return new DataSlot() {
            @Override
            public int get() {
                return PlayerMateriaProgress.furnaceExtraSetting(player, upgrade);
            }

            @Override
            public void set(int value) {
                syncedFurnaceExtraSettings[upgrade.ordinal()] = Mth.clamp(value, 0, upgrade.maxLevel());
            }
        };
    }
    private DataSlot createStoredExperienceDataSlot() {
        return new DataSlot() {
            @Override
            public int get() {
                return table == null ? 0 : table.storedExperienceWhole();
            }

            @Override
            public void set(int value) {
                syncedStoredExperience = Math.max(0, value);
            }
        };
    }

    private DataSlot createXpStorageAvailableDataSlot() {
        return new DataSlot() {
            @Override
            public int get() {
                return table != null && table.hasXpStorageUpgrade() ? 1 : 0;
            }

            @Override
            public void set(int value) {
                syncedXpStorageAvailable = value != 0;
            }
        };
    }
    private void addBackpackFilterGhostSlots() {
        for (int slot = 0; slot < PlayerMateriaProgress.MAX_BACKPACK_FILTER_SLOTS; slot++) {
            int column = slot % FILTER_GHOST_COLUMNS;
            int row = slot / FILTER_GHOST_COLUMNS;
            final int slotIndex = slot;
            addSlot(new MateriaGhostFilterSlot(
                    backpackFilterGhosts,
                    slot,
                    FILTER_GHOST_X + column * 18,
                    FILTER_GHOST_Y + row * 18,
                    () -> isBackpackTab() && backpackFilterOpen && slotIndex < backpackFilterSlotCount()
            ));
        }
    }
    private void addStorageSlots(IItemHandler storage) {
        for (int i = 0; i < STORAGE_SLOT_COUNT; i++) {
            int pageIdx = i % 78;
            int col = pageIdx % 13;
            int row = pageIdx / 13;
            final int slotIndex = i;
            addSlot(new BackpackItemHandlerSlot(
                    storage,
                    i,
                    STORAGE_GRID_X + col * 20,
                    STORAGE_GRID_Y + row * 20,
                    () -> this.isBackpackTab()
                            && slotIndex < unlockedStorageSlots()
                            && slotIndex / 78 == this.backpackPage
                            && matchesBackpackSearch(storage, slotIndex),
                    this::backpackStackLimit
            ));
        }
    }

    private void addFurnaceSlots(Inventory playerInventory, Container furnace) {
        addSlot(new TabAwareSlot(furnace, 0, FURNACE_INPUT_X, FURNACE_INPUT_Y, this::isFurnaceTab));
        addSlot(new MateriaFuelSlot(furnace, 1, FURNACE_FUEL_X, FURNACE_FUEL_Y, () -> isFurnaceTab() && !isSmeltEssenceEnabled()));
        addSlot(new MateriaFurnaceResultSlot(
                playerInventory.player,
                furnace,
                2,
                FURNACE_RESULT_X,
                FURNACE_RESULT_Y,
                this::isFurnaceTab,
                takenPlayer -> { if (table != null) table.releasePendingExperience(takenPlayer); }
        ));
    }

    private void addFurnaceInventorySlots(IItemHandler inputInventory, IItemHandler outputInventory) {
        for (int slot = 0; slot < FURNACE_INVENTORY_SLOT_COUNT; slot++) {
            int column = slot % FURNACE_INVENTORY_COLUMNS;
            int row = slot / FURNACE_INVENTORY_COLUMNS;
            final int slotIndex = slot;
            addSlot(new TabAwareItemHandlerSlot(
                    inputInventory,
                    slot,
                    FURNACE_INPUT_INVENTORY_X + column * 18,
                    FURNACE_INVENTORY_Y + row * 18,
                    () -> isFurnaceTab() && slotIndex < unlockedFurnaceSlotsPerSide()
            ));
        }

        for (int slot = 0; slot < FURNACE_INVENTORY_SLOT_COUNT; slot++) {
            int column = slot % FURNACE_INVENTORY_COLUMNS;
            int row = slot / FURNACE_INVENTORY_COLUMNS;
            final int slotIndex = slot;
            addSlot(new MateriaFurnaceOutputInventorySlot(
                    outputInventory,
                    slot,
                    FURNACE_OUTPUT_INVENTORY_X + column * 18,
                    FURNACE_INVENTORY_Y + row * 18,
                    () -> isFurnaceTab() && slotIndex < unlockedFurnaceSlotsPerSide(),
                    takenPlayer -> { if (table != null) table.releasePendingExperience(takenPlayer); }
            ));
        }
    }

    private boolean matchesBackpackSearch(IItemHandler storage, int slotIndex) {
        if (backpackSearchQuery.isEmpty()) {
            return true;
        }
        ItemStack stack = storage.getStackInSlot(slotIndex);
        return !stack.isEmpty() && stack.getHoverName().getString().toLowerCase(java.util.Locale.ROOT).contains(backpackSearchQuery);
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(playerInventory, column + row * 9 + 9, PLAYER_INVENTORY_X + column * 18, PLAYER_INVENTORY_Y + row * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(playerInventory, column, PLAYER_INVENTORY_X + column * 18, PLAYER_HOTBAR_Y));
        }
    }

    private void addCraftingSlots(Player player) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                addSlot(new TabAwareSlot(
                        this.craftSlots,
                        column + row * 3,
                        146 + column * 18,
                        206 + row * 18,
                        this::isEssenceTab
                ));
            }
        }
        
        addSlot(new TabAwareResultSlot(
                player,
                this.craftSlots,
                this.resultSlots,
                0,
                223,
                224,
                this::isEssenceTab
        ));
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.access.execute((level, pos) -> {
            this.clearContainer(player, this.craftSlots);
        });
    }

    @Override
    public void slotsChanged(Container container) {
        if (container == this.craftSlots) {
            this.access.execute((level, pos) -> {
                if (!level.isClientSide) {
                    ItemStack result = ItemStack.EMPTY;
                    java.util.List<ItemStack> items = new java.util.ArrayList<>();
                    for (int i = 0; i < 9; i++) {
                        items.add(this.craftSlots.getItem(i));
                    }
                    net.minecraft.world.item.crafting.CraftingInput input = net.minecraft.world.item.crafting.CraftingInput.of(3, 3, items);
                    
                    Optional<RecipeHolder<CraftingRecipe>> optional = level.getRecipeManager()
                            .getRecipeFor(RecipeType.CRAFTING, input, level);
                    if (optional.isPresent()) {
                        result = optional.get().value().assemble(input, level.registryAccess());
                    }
                    this.resultSlots.setItem(0, result);
                    this.broadcastChanges();
                }
            });
        } else {
            super.slotsChanged(container);
        }
    }
}
