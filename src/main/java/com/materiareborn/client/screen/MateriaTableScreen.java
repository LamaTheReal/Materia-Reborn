package com.materiareborn.client.screen;

import com.materiareborn.api.essence.EssenceAmount;
import com.materiareborn.config.MateriaConfig;
import com.materiareborn.core.ModConstants;
import com.materiareborn.fluid.LiquidEssenceRecipe;
import com.materiareborn.menu.MateriaTableMenu;
import com.materiareborn.progression.BackpackExtraUpgrade;
import com.materiareborn.progression.FurnaceExtraUpgrade;
import com.materiareborn.progression.MateriaTableProgression;
import com.materiareborn.progression.PlayerMateriaProgress;
import com.materiareborn.registry.ModFluids;
import com.materiareborn.registry.ModItems;
import com.materiareborn.ritual.MateriaTableUpgrade;
import com.materiareborn.menu.MateriaTableTab;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class MateriaTableScreen extends AbstractContainerScreen<MateriaTableMenu> {
    private static final ResourceLocation PANEL_TILE = ResourceLocation.parse(
            ModConstants.MOD_ID + ":textures/gui/materia_table/panel_tile.png"
    );
    private static final ResourceLocation ESSENCE_ICON = ResourceLocation.parse(
            ModConstants.MOD_ID + ":textures/gui/materia_table/essence_icon.png"
    );
    private static final ResourceLocation BACKPACK_ICON = ResourceLocation.parse(
            ModConstants.MOD_ID + ":textures/gui/materia_table/backpack_icon.png"
    );

    private static final int SCREEN_WIDTH = 430;
    private static final int SCREEN_HEIGHT = 396;
    private static final int HEADER_HEIGHT = 68;
    private static final int TAB_Y = 46;
    private static final int TAB_HEIGHT = 20;
    private static final int TAB_WIDTH = 76;
    private static final int TAB_GAP = 6;
    private static final int TAB_START_X = 95;
    private static final int RITUAL_BUILD_X = 5;
    private static final int RITUAL_BUILD_WIDTH = 85;
    private static final int RITUAL_PREVIEW_PANEL_X = -280;
    private static final int RITUAL_PREVIEW_PANEL_Y = 46;
    private static final int RITUAL_PREVIEW_PANEL_WIDTH = 274;
    private static final int RITUAL_PREVIEW_PANEL_HEIGHT = 210;
    private static final int RITUAL_PREVIEW_ROW_X = RITUAL_PREVIEW_PANEL_X + 10;
    private static final int RITUAL_PREVIEW_ROW_Y = RITUAL_PREVIEW_PANEL_Y + 35;
    private static final int RITUAL_PREVIEW_BUILD_X = RITUAL_PREVIEW_PANEL_X + 8;
    private static final int RITUAL_PREVIEW_BUILD_Y = RITUAL_PREVIEW_PANEL_Y + 184;
    private static final int RITUAL_PREVIEW_BUILD_WIDTH = RITUAL_PREVIEW_PANEL_WIDTH - 16;
    private static final int RITUAL_PREVIEW_BUILD_HEIGHT = 20;
    private static final int NAV_SIZE = 24;
    private static final int NAV_LEFT_X = 16;
    private static final int NAV_RIGHT_X = SCREEN_WIDTH - 40;
    private static final int NAV_Y = 12;

    private static final int MAIN_PANEL_Y = 78;
    private static final int SIDEBAR_X = 16;
    private static final int SIDEBAR_WIDTH = 110;
    private static final int CONTENT_X = 140;
    private static final int CONTENT_WIDTH = 274;
    private static final int PANEL_HEIGHT = 118;

    private static final int PLAYER_LABEL_Y = 286;
    private static final int EXTRAS_PANEL_X = -280;
    private static final int EXTRAS_PANEL_Y = 280;
    private static final int BACKPACK_EXTRAS_PANEL_Y = 256;
    private static final int BACKPACK_EXTRAS_PANEL_HEIGHT = 106;
    private static final int BACKPACK_FILTER_PANEL_Y = 280;
    private static final int BACKPACK_FILTER_PANEL_HEIGHT = 112;
    private static final int BACKPACK_FILTER_GHOST_X = EXTRAS_PANEL_X + 8;
    private static final int BACKPACK_FILTER_GHOST_Y = BACKPACK_FILTER_PANEL_Y + 30;
    private static final int BACKPACK_FILTER_GHOST_COLUMNS = 9;
    private static final int BACKPACK_EXTRAS_ROW_Y = BACKPACK_EXTRAS_PANEL_Y + 6;
    private static final int EXTRAS_PANEL_WIDTH = 274;
    private static final int EXTRAS_PANEL_HEIGHT = 74;
    private static final int EXTRAS_ROW_Y = EXTRAS_PANEL_Y + 6;
    private static final int EXTRAS_LABEL_X = EXTRAS_PANEL_X + 8;
    private static final int EXTRAS_LEVEL_CENTER_X = EXTRAS_PANEL_X + 135;
    private static final int EXTRAS_BUY_X = EXTRAS_PANEL_X + 161;
    private static final int EXTRAS_SLIDER_X = EXTRAS_PANEL_X + 116;
    private static final int EXTRAS_SLIDER_WIDTH = 42;
    private static final int EXTRAS_COST_X = EXTRAS_PANEL_X + 209;
    private static final int XP_BUTTON_WIDTH = 48;
    private static final int ESSENCE_CATALOG_X = 268;
    private static final int ESSENCE_CATALOG_Y = 126;
    private static final int ESSENCE_CATALOG_COLUMNS = 6;
    private static final int ESSENCE_CATALOG_ROWS = 6;
    private static final int ESSENCE_CATALOG_SPACING = 24;
    private static final int ESSENCE_CATALOG_PAGE_SIZE = ESSENCE_CATALOG_COLUMNS * ESSENCE_CATALOG_ROWS;
    private static final int ESSENCE_CATALOG_PAGE_Y = 264;
    private static final int ESSENCE_CATALOG_PREVIOUS_X = 268;
    private static final int ESSENCE_CATALOG_NEXT_X = 390;
    private static final int ESSENCE_CATALOG_PAGE_BUTTON_WIDTH = 18;
    private static final int ESSENCE_CATALOG_PAGE_BUTTON_HEIGHT = 14;
    private static final int ESSENCE_CATALOG_GRID_WIDTH = 140;
    private static final int ESSENCE_SEARCH_WIDTH = 94;
    private static final int ESSENCE_FILTER_BUTTON_X = 366;
    private static final int ESSENCE_FILTER_BUTTON_Y = 98;
    private static final int ESSENCE_FILTER_BUTTON_WIDTH = 42;
    private static final int ESSENCE_FILTER_BUTTON_HEIGHT = 22;
    private static final int ESSENCE_FILTER_PANEL_X = SCREEN_WIDTH + 8;
    private static final int ESSENCE_FILTER_PANEL_Y = MAIN_PANEL_Y;
    private static final int ESSENCE_FILTER_PANEL_WIDTH = 130;
    private static final int ESSENCE_FILTER_PANEL_HEIGHT = 78;
    private static final int ESSENCE_FILTER_GRID_X = ESSENCE_FILTER_PANEL_X + 6;
    private static final int ESSENCE_FILTER_GRID_Y = ESSENCE_FILTER_PANEL_Y + 24;
    private static final int ESSENCE_FILTER_COLUMNS = 5;
    private static final int ESSENCE_FILTER_OPTION_SIZE = 22;
    private static final int ESSENCE_FILTER_OPTION_GAP = 2;
    private static final int ESSENCE_FILTER_RESET_X = ESSENCE_FILTER_PANEL_X + ESSENCE_FILTER_PANEL_WIDTH - 19;
    private static final int ESSENCE_FILTER_RESET_Y = ESSENCE_FILTER_PANEL_Y + 5;
    private static final int ESSENCE_FILTER_RESET_SIZE = 14;

    private static final int OUTER_BORDER = 0xFF4B2658;
    private static final int HEADER_DIVIDER = 0x884B2658;
    private static final int SCREEN_OVERLAY = 0xE50A0910;
    private static final int PANEL_FILL = 0xCC0C0B12;
    private static final int PANEL_BORDER = 0xFF2C2434;
    private static final int SLOT_BORDER = 0xFF39333F;
    private static final int SLOT_FILL = 0xFF111017;
    private static final int TEXT_PRIMARY = 0xFFE8E3EA;
    private static final int TEXT_SECONDARY = 0xFFAEA7B2;
    private static final int TEXT_DIM = 0xFF7E7785;
    private static final int ACCENT = 0xFFBE66E7;
    private static final int ACCENT_BORDER = 0xFFB155D4;
    private static final int DISABLED_FILL = 0x9920262F;
    private static final int POSITIVE = 0xFF32D66B;
    private static final int WARNING = 0xFFFFC84D;
    private static final int ESSENCE_BLUE = 0xFF38D7FF;
    private static final int FURNACE_FLAME = 0xFFFF9238;
    private static final int DANGER = 0xFFE54D5E;

    private boolean essenceSearchFocused;
    private boolean backpackSearchFocused;
    private boolean ritualPreviewOpen;
    private boolean extrasDropdownOpen;
    private boolean backpackExtrasDropdownOpen;
    private boolean essenceFilterDropdownOpen;
    private boolean creativeFiltersInitialized;
    private FurnaceExtraUpgrade draggedFurnaceExtra;
    private BackpackExtraUpgrade draggedBackpackExtra;
    private String essenceSearch = "";
    private int inspectedEssenceCatalogIndex = -1;
    private int essenceCatalogPage;
    private EssenceCreativeFilter activeEssenceFilter;
    private final Map<EssenceCreativeFilter, List<Integer>> essenceCreativeFilterCache =
            new EnumMap<>(EssenceCreativeFilter.class);
    private String backpackSearch = "";

    private static final List<ItemStack> PREVIEW_ITEMS = List.of(
            stack(Items.DIAMOND, 16),
            stack(Items.EMERALD, 32),
            stack(Items.LAPIS_LAZULI, 64),
            stack(Items.REDSTONE, 48),
            stack(Items.QUARTZ, 24),
            stack(Items.GOLD_INGOT, 32),
            stack(Items.IRON_INGOT, 64),
            stack(Items.COPPER_INGOT, 48),
            stack(Items.COAL, 64),
            stack(Items.GLOWSTONE_DUST, 32),
            stack(Items.AMETHYST_SHARD, 16),
            stack(Items.ENDER_PEARL, 16),
            stack(Items.BLAZE_ROD, 64),
            stack(Items.SLIME_BALL, 32),
            stack(Items.PRISMARINE_CRYSTALS, 16),
            stack(Items.BONE, 64),
            stack(Items.STRING, 64),
            stack(Items.LEATHER, 32),
            stack(Blocks.OBSIDIAN, 16),
            stack(Blocks.NETHERRACK, 32),
            stack(Blocks.SOUL_SAND, 16),
            stack(Blocks.END_STONE, 32),
            stack(Blocks.AMETHYST_CLUSTER, 8),
            stack(Blocks.CRYING_OBSIDIAN, 8),
            stack(Items.NETHER_STAR, 1)
    );

    public MateriaTableScreen(MateriaTableMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        imageWidth = SCREEN_WIDTH;
        imageHeight = SCREEN_HEIGHT;
        inventoryLabelX = 148;
        inventoryLabelY = PLAYER_LABEL_Y;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
        renderEssenceCatalogTooltip(graphics, mouseX, mouseY);
        renderEssenceFilterTooltip(graphics, mouseX, mouseY);
        renderLockedTabTooltip(graphics, mouseX, mouseY);
    }

    private void renderEssenceCatalogTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        if (!menu.isEssenceTab()) {
            return;
        }
        int index = essenceCatalogIndexAt(mouseX, mouseY);
        if (index >= 0 && menu.isEssenceItemUnlocked(index)) {
            graphics.renderTooltip(font, menu.essenceCatalogStack(index), mouseX, mouseY);
        }
    }
    private void renderEssenceFilterTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        if (!menu.isEssenceTab() || !essenceFilterDropdownOpen) {
            return;
        }
        EssenceCreativeFilter filter = essenceFilterAt(mouseX, mouseY);
        if (filter != null) {
            graphics.renderTooltip(font, filter.displayName(), mouseX, mouseY);
            return;
        }
        if (isInside(
                mouseX,
                mouseY,
                leftPos + ESSENCE_FILTER_RESET_X,
                topPos + ESSENCE_FILTER_RESET_Y,
                ESSENCE_FILTER_RESET_SIZE,
                ESSENCE_FILTER_RESET_SIZE
        )) {
            graphics.renderTooltip(
                    font,
                    Component.translatable("screen.materia_reborn.materia_table.filter.clear"),
                    mouseX,
                    mouseY
            );
        }
    }

    private void renderLockedTabTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        for (MateriaTableTab tab : MateriaTableTab.values()) {
            if (!menu.isTabUnlocked(tab) && isInsideTab(mouseX, mouseY, tab)) {
                graphics.renderTooltip(
                        font,
                        Component.translatable(
                                "screen.materia_reborn.materia_table.requires_level",
                                tab.requiredTableTier()
                        ),
                        mouseX,
                        mouseY
                );
                return;
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 1 && menu.isEssenceTab()) {
            int catalogIndex = essenceCatalogIndexAt(mouseX, mouseY);
            if (catalogIndex >= 0 && menu.isEssenceItemUnlocked(catalogIndex)) {
                inspectedEssenceCatalogIndex = catalogIndex;
                sendMenuButton(MateriaTableMenu.essenceItemPurchaseButtonId(
                        catalogIndex,
                        hasShiftDown(),
                        true
                ));
                return true;
            }
        }
        if (button == 0) {
            if (menu.isEssenceTab() && essenceFilterDropdownOpen) {
                EssenceCreativeFilter clickedFilter = essenceFilterAt(mouseX, mouseY);
                if (clickedFilter != null) {
                    activeEssenceFilter = activeEssenceFilter == clickedFilter ? null : clickedFilter;
                    essenceCatalogPage = 0;
                    inspectedEssenceCatalogIndex = -1;
                    return true;
                }
                if (isInside(
                        mouseX,
                        mouseY,
                        leftPos + ESSENCE_FILTER_RESET_X,
                        topPos + ESSENCE_FILTER_RESET_Y,
                        ESSENCE_FILTER_RESET_SIZE,
                        ESSENCE_FILTER_RESET_SIZE
                )) {
                    activeEssenceFilter = null;
                    essenceCatalogPage = 0;
                    inspectedEssenceCatalogIndex = -1;
                    return true;
                }
                if (isInside(
                        mouseX,
                        mouseY,
                        leftPos + ESSENCE_FILTER_PANEL_X,
                        topPos + ESSENCE_FILTER_PANEL_Y,
                        ESSENCE_FILTER_PANEL_WIDTH,
                        ESSENCE_FILTER_PANEL_HEIGHT
                )) {
                    return true;
                }
                if (!isInside(
                        mouseX,
                        mouseY,
                        leftPos + ESSENCE_FILTER_BUTTON_X,
                        topPos + ESSENCE_FILTER_BUTTON_Y,
                        ESSENCE_FILTER_BUTTON_WIDTH,
                        ESSENCE_FILTER_BUTTON_HEIGHT
                )) {
                    essenceFilterDropdownOpen = false;
                }
            }
            if (ritualPreviewOpen && isInside(
                    mouseX,
                    mouseY,
                    leftPos + RITUAL_PREVIEW_PANEL_X,
                    topPos + RITUAL_PREVIEW_PANEL_Y,
                    RITUAL_PREVIEW_PANEL_WIDTH,
                    RITUAL_PREVIEW_PANEL_HEIGHT
            )) {
                if (menu.canPrepareUpgradeRitual() && isInside(
                        mouseX,
                        mouseY,
                        leftPos + RITUAL_PREVIEW_BUILD_X,
                        topPos + RITUAL_PREVIEW_BUILD_Y,
                        RITUAL_PREVIEW_BUILD_WIDTH,
                        RITUAL_PREVIEW_BUILD_HEIGHT
                )) {
                    sendMenuButton(MateriaTableMenu.ritualBuildButtonId());
                    ritualPreviewOpen = false;
                }
                return true;
            }
            if (isInside(mouseX, mouseY, leftPos + NAV_LEFT_X, topPos + NAV_Y, NAV_SIZE, NAV_SIZE)) {
                sendMenuButton(MateriaTableMenu.previousButtonId());
                return true;
            }
            if (isInside(mouseX, mouseY, leftPos + NAV_RIGHT_X, topPos + NAV_Y, NAV_SIZE, NAV_SIZE)) {
                sendMenuButton(MateriaTableMenu.nextButtonId());
                return true;
            }
            if (isInside(
                    mouseX,
                    mouseY,
                    leftPos + RITUAL_BUILD_X,
                    topPos + TAB_Y,
                    RITUAL_BUILD_WIDTH,
                    TAB_HEIGHT
            )) {
                if (menu.canPrepareUpgradeRitual()) {
                    ritualPreviewOpen = !ritualPreviewOpen;
                    extrasDropdownOpen = false;
                    backpackExtrasDropdownOpen = false;
                    if (ritualPreviewOpen) {
                        sendMenuButton(MateriaTableMenu.ritualPreviewButtonId());
                    }
                }
                return true;
            }
            if (isInsideTab(mouseX, mouseY, MateriaTableTab.ESSENCE)) {
                if (menu.isTabUnlocked(MateriaTableTab.ESSENCE)) {
                    sendMenuButton(MateriaTableMenu.essenceButtonId());
                }
                return true;
            }
            if (isInsideTab(mouseX, mouseY, MateriaTableTab.BACKPACK)) {
                if (menu.isTabUnlocked(MateriaTableTab.BACKPACK)) {
                    sendMenuButton(MateriaTableMenu.backpackButtonId());
                }
                return true;
            }
            if (isInsideTab(mouseX, mouseY, MateriaTableTab.FURNACE)) {
                if (menu.isTabUnlocked(MateriaTableTab.FURNACE)) {
                    sendMenuButton(MateriaTableMenu.furnaceButtonId());
                }
                return true;
            }
            if (menu.isEssenceTab()) {
                if (isInside(
                        mouseX,
                        mouseY,
                        leftPos + ESSENCE_FILTER_BUTTON_X,
                        topPos + ESSENCE_FILTER_BUTTON_Y,
                        ESSENCE_FILTER_BUTTON_WIDTH,
                        ESSENCE_FILTER_BUTTON_HEIGHT
                )) {
                    ensureCreativeFiltersReady();
                    essenceFilterDropdownOpen = !essenceFilterDropdownOpen;
                    essenceSearchFocused = false;
                    backpackSearchFocused = false;
                    return true;
                }
                if (isInside(
                        mouseX,
                        mouseY,
                        leftPos + ESSENCE_CATALOG_X,
                        topPos + 98,
                        ESSENCE_SEARCH_WIDTH,
                        22
                )) {
                    essenceSearchFocused = true;
                    backpackSearchFocused = false;
                    return true;
                }
                essenceSearchFocused = false;
                if (isInside(
                        mouseX,
                        mouseY,
                        leftPos + ESSENCE_CATALOG_PREVIOUS_X,
                        topPos + ESSENCE_CATALOG_PAGE_Y,
                        ESSENCE_CATALOG_PAGE_BUTTON_WIDTH,
                        ESSENCE_CATALOG_PAGE_BUTTON_HEIGHT
                )) {
                    changeEssenceCatalogPage(-1);
                    return true;
                }
                if (isInside(
                        mouseX,
                        mouseY,
                        leftPos + ESSENCE_CATALOG_NEXT_X,
                        topPos + ESSENCE_CATALOG_PAGE_Y,
                        ESSENCE_CATALOG_PAGE_BUTTON_WIDTH,
                        ESSENCE_CATALOG_PAGE_BUTTON_HEIGHT
                )) {
                    changeEssenceCatalogPage(1);
                    return true;
                }
                if (isInside(mouseX, mouseY, leftPos + 22, topPos + 218, 98, 20)) {
                    if (menu.canAnalyzeEssenceItem()) {
                        sendMenuButton(MateriaTableMenu.analyzeItemButtonId());
                    }
                    return true;
                }
                if (isInside(mouseX, mouseY, leftPos + 22, topPos + 239, 98, 20)) {
                    if (menu.canUnlockEssenceItem()) {
                        sendMenuButton(MateriaTableMenu.unlockItemButtonId());
                    }
                    return true;
                }
                if (isInside(mouseX, mouseY, leftPos + 22, topPos + 260, 98, 20)) {
                    if (menu.canSellEssenceItem()) {
                        sendMenuButton(MateriaTableMenu.sellItemButtonId());
                    }
                    return true;
                }
                if (isInside(mouseX, mouseY, leftPos + 22, topPos + 281, 98, 20)) {
                    sendMenuButton(MateriaTableMenu.autoSellButtonId());
                    return true;
                }
                if (menu.canRemoveEssenceUnlock(inspectedEssenceCatalogIndex)
                        && isInside(mouseX, mouseY, leftPos + 140, topPos + 150, 108, 20)) {
                    sendMenuButton(MateriaTableMenu.essenceItemRemoveUnlockButtonId(
                            inspectedEssenceCatalogIndex
                    ));
                    inspectedEssenceCatalogIndex = -1;
                    return true;
                }
                int catalogIndex = essenceCatalogIndexAt(mouseX, mouseY);
                if (catalogIndex >= 0 && menu.isEssenceItemUnlocked(catalogIndex)) {
                    inspectedEssenceCatalogIndex = catalogIndex;
                    sendMenuButton(MateriaTableMenu.essenceItemPurchaseButtonId(
                            catalogIndex,
                            hasShiftDown(),
                            false
                    ));
                    return true;
                }
            }            if (menu.isBackpackTab()) {
                if (isInside(mouseX, mouseY, leftPos + 151, topPos + 84, 255, 22)) {
                    essenceSearchFocused = false;
                    backpackSearchFocused = true;
                    return true;
                }
                if (isInside(mouseX, mouseY, leftPos + 302 + 56, topPos + 260 - 4, 20, 20)) {
                    if (menu.getBackpackPage() > 0) {
                        sendMenuButton(5);
                        return true;
                    }
                }
                if (isInside(mouseX, mouseY, leftPos + 302 + 80, topPos + 260 - 4, 20, 20)) {
                    if (menu.getBackpackPage() < menu.maxBackpackPage()) {
                        sendMenuButton(6);
                        return true;
                    }
                }
            }
            if ((menu.isBackpackTab() || menu.isFurnaceTab()) && isInside(mouseX, mouseY, leftPos + 22, topPos + 232, 98, 20)) {
                sendMenuButton(hasShiftDown() ? MateriaTableMenu.upgradeTenButtonId() : MateriaTableMenu.upgradeButtonId());
                backpackSearchFocused = false;
                return true;
            }
            if (menu.isBackpackTab() && menu.canUseBackpackExtras() && isInside(mouseX, mouseY, leftPos + 22, topPos + 256, 98, 20)) {
                if (menu.isBackpackFilterOpen()) {
                    sendMenuButton(MateriaTableMenu.backpackFilterToggleButtonId());
                }
                backpackExtrasDropdownOpen = !backpackExtrasDropdownOpen;
                backpackSearchFocused = false;
                return true;
            }            if (menu.isBackpackTab() && menu.hasBackpackFilterUpgrade() && isInside(mouseX, mouseY, leftPos + 22, topPos + 280, 98, 20)) {
                sendMenuButton(MateriaTableMenu.backpackFilterToggleButtonId());
                backpackExtrasDropdownOpen = false;
                backpackSearchFocused = false;
                return true;
            }            if (menu.isFurnaceTab() && menu.canUseSmeltEssence() && isInside(mouseX, mouseY, leftPos + 22, topPos + 256, 98, 20)) {
                sendMenuButton(MateriaTableMenu.smeltEssenceButtonId());
                backpackSearchFocused = false;
                return true;
            }
            if (menu.isFurnaceTab() && menu.canUseFurnaceExtras() && isInside(mouseX, mouseY, leftPos + 22, topPos + 280, 98, 20)) {
                extrasDropdownOpen = !extrasDropdownOpen;
                backpackSearchFocused = false;
                return true;
            }
            if (menu.isFurnaceTab() && menu.hasXpStorage() && menu.storedExperience() > 0 && isInside(mouseX, mouseY, leftPos + xpButtonX(), topPos + 118, XP_BUTTON_WIDTH, 16)) {
                sendMenuButton(MateriaTableMenu.claimXpButtonId());
                return true;
            }
            if (menu.isBackpackTab() && menu.isBackpackFilterOpen()) {
                int filterButtonX = EXTRAS_PANEL_X + 180;
                if (isInside(mouseX, mouseY, leftPos + filterButtonX, topPos + BACKPACK_FILTER_PANEL_Y + 30, 88, 18)) {
                    sendMenuButton(MateriaTableMenu.backpackFilterResetButtonId());
                    return true;
                }
                if (menu.backpackExtraLevel(BackpackExtraUpgrade.FILTER) >= 3
                        && isInside(mouseX, mouseY, leftPos + filterButtonX, topPos + BACKPACK_FILTER_PANEL_Y + 52, 88, 18)) {
                    sendMenuButton(MateriaTableMenu.backpackFilterIgnoreNbtButtonId());
                    return true;
                }
                if (menu.backpackExtraLevel(BackpackExtraUpgrade.FILTER) >= 4
                        && isInside(mouseX, mouseY, leftPos + filterButtonX, topPos + BACKPACK_FILTER_PANEL_Y + 74, 88, 18)) {
                    sendMenuButton(MateriaTableMenu.backpackFilterIgnoreDamageButtonId());
                    return true;
                }
                for (int filterSlot = 0; filterSlot < PlayerMateriaProgress.MAX_ACTIVE_BACKPACK_FILTER_SLOTS; filterSlot++) {
                    int x = BACKPACK_FILTER_GHOST_X + (filterSlot % BACKPACK_FILTER_GHOST_COLUMNS) * 18;
                    int y = BACKPACK_FILTER_GHOST_Y + (filterSlot / BACKPACK_FILTER_GHOST_COLUMNS) * 18;
                    if (isInside(mouseX, mouseY, leftPos + x, topPos + y, 16, 16)) {
                        if (filterSlot < menu.backpackFilterSlotCount()) {
                            sendMenuButton(MateriaTableMenu.backpackFilterRemoveButtonId(filterSlot));
                        }
                        return true;
                    }
                }
                for (int slotIndex = 0; slotIndex < menu.slots.size(); slotIndex++) {
                    Slot slot = menu.slots.get(slotIndex);
                    if (menu.isPlayerInventorySlot(slotIndex) && slot.isActive() && isInside(mouseX, mouseY, leftPos + slot.x, topPos + slot.y, 16, 16)) {
                        sendMenuButton(MateriaTableMenu.backpackFilterAddFromPlayerButtonId(slotIndex));
                        return true;
                    }
                }
            }            if (menu.isBackpackTab() && backpackExtrasDropdownOpen && menu.canUseBackpackExtras()) {
                if (isInside(mouseX, mouseY, leftPos + EXTRAS_PANEL_X, topPos + BACKPACK_EXTRAS_PANEL_Y, EXTRAS_PANEL_WIDTH, BACKPACK_EXTRAS_PANEL_HEIGHT)) {
                    for (BackpackExtraUpgrade upgrade : BackpackExtraUpgrade.values()) {
                        int rowY = BACKPACK_EXTRAS_ROW_Y + upgrade.ordinal() * 16;
                        if (isInside(mouseX, mouseY, leftPos + EXTRAS_SLIDER_X, topPos + rowY + 3, EXTRAS_SLIDER_WIDTH, 10)) {
                            updateBackpackExtraSlider(upgrade, mouseX);
                            draggedBackpackExtra = upgrade;
                            return true;
                        }
                        if (isInside(mouseX, mouseY, leftPos + EXTRAS_BUY_X, topPos + rowY, 38, 14)) {
                            sendMenuButton(MateriaTableMenu.backpackExtraButtonId(upgrade));
                            return true;
                        }
                    }
                    return true;
                }
                backpackExtrasDropdownOpen = false;
            }            if (menu.isFurnaceTab() && extrasDropdownOpen && menu.canUseFurnaceExtras()) {
                if (isInside(mouseX, mouseY, leftPos + EXTRAS_PANEL_X, topPos + EXTRAS_PANEL_Y, EXTRAS_PANEL_WIDTH, EXTRAS_PANEL_HEIGHT)) {
                    for (FurnaceExtraUpgrade upgrade : FurnaceExtraUpgrade.values()) {
                        int rowY = EXTRAS_ROW_Y + upgrade.ordinal() * 16;
                        if (isInside(mouseX, mouseY, leftPos + EXTRAS_SLIDER_X, topPos + rowY + 3, EXTRAS_SLIDER_WIDTH, 10)) {
                            updateFurnaceExtraSlider(upgrade, mouseX);
                            draggedFurnaceExtra = upgrade;
                            return true;
                        }
                        if (isInside(mouseX, mouseY, leftPos + EXTRAS_BUY_X, topPos + rowY, 38, 14)) {
                            sendMenuButton(MateriaTableMenu.furnaceExtraButtonId(upgrade));
                            return true;
                        }
                    }
                    return true;
                }
                extrasDropdownOpen = false;
            }
        }
        essenceSearchFocused = false;
        backpackSearchFocused = false;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void updateBackpackExtraSlider(BackpackExtraUpgrade upgrade, double mouseX) {
        int purchasedLevel = menu.backpackExtraLevel(upgrade);
        if (purchasedLevel <= 0) {
            return;
        }
        double relative = (mouseX - leftPos - EXTRAS_SLIDER_X) / (double) (EXTRAS_SLIDER_WIDTH - 1);
        int setting = Mth.clamp((int) Math.round(relative * purchasedLevel), 0, purchasedLevel);
        sendMenuButton(MateriaTableMenu.backpackExtraSettingButtonId(upgrade, setting));
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (button == 0 && draggedFurnaceExtra != null) {
            updateFurnaceExtraSlider(draggedFurnaceExtra, mouseX);
            return true;
        }
        if (button == 0 && draggedBackpackExtra != null) {
            updateBackpackExtraSlider(draggedBackpackExtra, mouseX);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && draggedFurnaceExtra != null) {
            draggedFurnaceExtra = null;
            return true;
        }
        if (button == 0 && draggedBackpackExtra != null) {
            draggedBackpackExtra = null;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (menu.isEssenceTab()
                && scrollY != 0.0D
                && isInside(
                        mouseX,
                        mouseY,
                        leftPos + ESSENCE_CATALOG_X,
                        topPos + 98,
                        ESSENCE_CATALOG_GRID_WIDTH,
                        ESSENCE_CATALOG_PAGE_Y + ESSENCE_CATALOG_PAGE_BUTTON_HEIGHT - 98
                )) {
            changeEssenceCatalogPage(scrollY < 0.0D ? 1 : -1);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    private void updateFurnaceExtraSlider(FurnaceExtraUpgrade upgrade, double mouseX) {
        int purchasedLevel = menu.furnaceExtraLevel(upgrade);
        if (purchasedLevel <= 0) {
            return;
        }
        double relative = (mouseX - leftPos - EXTRAS_SLIDER_X) / (double) (EXTRAS_SLIDER_WIDTH - 1);
        int setting = Mth.clamp((int) Math.round(relative * purchasedLevel), 0, purchasedLevel);
        sendMenuButton(MateriaTableMenu.furnaceExtraSettingButtonId(upgrade, setting));
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (essenceSearchFocused && menu.isEssenceTab() && !Character.isISOControl(codePoint)) {
            if (essenceSearch.length() < 48) {
                essenceSearch += codePoint;
                essenceCatalogPage = 0;
                inspectedEssenceCatalogIndex = -1;
            }
            return true;
        }
        if (backpackSearchFocused && menu.isBackpackTab() && !Character.isISOControl(codePoint)) {
            if (backpackSearch.length() < 32) {
                backpackSearch += codePoint;
                menu.setBackpackSearchQuery(backpackSearch);
            }
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (essenceSearchFocused && menu.isEssenceTab()) {
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE && !essenceSearch.isEmpty()) {
                essenceSearch = essenceSearch.substring(0, essenceSearch.length() - 1);
                essenceCatalogPage = 0;
                inspectedEssenceCatalogIndex = -1;
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_ENTER) {
                essenceSearchFocused = false;
                return true;
            }
        }
        if (backpackSearchFocused && menu.isBackpackTab()) {
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE && !backpackSearch.isEmpty()) {
                backpackSearch = backpackSearch.substring(0, backpackSearch.length() - 1);
                menu.setBackpackSearchQuery(backpackSearch);
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_ENTER) {
                backpackSearchFocused = false;
                return true;
            }
        }
        if (minecraft != null && minecraft.options.keyInventory.matches(keyCode, scanCode)) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int left = leftPos;
        int top = topPos;

        graphics.blit(PANEL_TILE, left, top, 0, 0.0F, 0.0F, imageWidth, imageHeight, 256, 256);
        graphics.fill(left, top, left + imageWidth, top + imageHeight, SCREEN_OVERLAY);
        graphics.pose().pushPose();
        graphics.pose().translate((float) left, (float) top, 0.0F);
        drawMateriaPortalBackground(graphics, partialTick);
        drawFrame(graphics, 0, 0, imageWidth, imageHeight);
        graphics.hLine(0, imageWidth - 1, HEADER_HEIGHT, HEADER_DIVIDER);

        drawNavigation(graphics, mouseX, mouseY);
        drawHeader(graphics);
        drawTabs(graphics, mouseX, mouseY);
        
        drawPanel(graphics, 140, 284, 178, 96);

        if (menu.isEssenceTab()) {
            drawEssencePanels(graphics);
            drawEssenceTab(graphics, mouseX, mouseY);
        } else if (menu.isBackpackTab()) {
            drawBackpackPanels(graphics);
            drawBackpackTab(graphics, mouseX, mouseY);
        } else if (menu.isFurnaceTab()) {
            drawBackpackPanels(graphics);
            drawFurnaceTab(graphics, mouseX, mouseY);
        } else {
            drawCommonPanels(graphics);
        }

        if (menu.isBackpackTab() && menu.isBackpackFilterOpen()) {
            drawBackpackFilterPanel(graphics);
        }

        for (Slot slot : menu.slots) {
            if (slot.isActive()) {
                drawSlotBackground(graphics, slot.x, slot.y);
            }
        }
        if (menu.isBackpackTab() && backpackExtrasDropdownOpen && menu.canUseBackpackExtras()) {
            drawBackpackExtrasDropdown(graphics, mouseX, mouseY);
        }
        if (menu.isFurnaceTab() && extrasDropdownOpen && menu.canUseFurnaceExtras()) {
            drawFurnaceExtrasDropdown(graphics, mouseX, mouseY);
        }
        if (menu.isEssenceTab() && essenceFilterDropdownOpen) {
            drawEssenceFilterDropdown(graphics, mouseX, mouseY);
        }
        if (ritualPreviewOpen && menu.canPrepareUpgradeRitual()) {
            drawRitualPreviewPanel(graphics, mouseX, mouseY);
        }
        graphics.pose().popPose();
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, TEXT_SECONDARY, false);
    }

    private void drawHeader(GuiGraphics graphics) {
        int iconX = 45;
        int iconY = 20;
        int iconSize = 14;
        graphics.blit(ESSENCE_ICON, iconX, iconY, iconSize, iconSize, 0.0F, 0.0F, 32, 32, 32, 32);
        int textY = iconY + (iconSize - font.lineHeight) / 2;
        graphics.drawString(font, menu.formattedDisplayedEssence(), iconX + iconSize + 4, textY, ACCENT, false);
    }

    private void drawMateriaPortalBackground(GuiGraphics graphics, float partialTick) {
        float time = portalTime(partialTick);
        graphics.fill(2, 2, imageWidth - 2, imageHeight - 2, 0x55030208);

        for (int layer = 0; layer < 3; layer++) {
            int spacing = 42 + layer * 18;
            int alpha = 18 - layer * 4;
            int color = (alpha << 24) | (layer == 0 ? 0xB85AE6 : layer == 1 ? 0x6D2B8A : 0x2F8AFF);
            int offsetX = Mth.floor(time * (5.0F + layer * 2.5F)) % spacing;
            int offsetY = Mth.floor(time * (3.0F + layer * 1.5F)) % spacing;
            for (int x = -spacing + offsetX; x < imageWidth; x += spacing) {
                graphics.fill(x, 2, x + 1, imageHeight - 2, color);
            }
            for (int y = -spacing + offsetY; y < imageHeight; y += spacing) {
                graphics.fill(2, y, imageWidth - 2, y + 1, color);
            }
        }

        for (int band = 0; band < 7; band++) {
            float wave = Mth.sin(time * 0.32F + band * 1.37F);
            int y = 18 + band * 52 + Mth.floor(wave * 10.0F);
            int alpha = 18 + Mth.floor((wave + 1.0F) * 5.0F);
            graphics.fill(6, y, imageWidth - 6, y + 2, (alpha << 24) | 0x7F33AA);
        }

        for (int i = 0; i < 42; i++) {
            int seed = i * 73 + 19;
            int x = Math.floorMod(seed * 37 + Mth.floor(time * (7.0F + i % 5)), imageWidth - 18) + 9;
            int y = Math.floorMod(seed * 23 + Mth.floor(time * (3.0F + i % 7)), imageHeight - 18) + 9;
            float pulse = (Mth.sin(time * 0.9F + seed) + 1.0F) * 0.5F;
            int alpha = 32 + Mth.floor(pulse * 56.0F);
            int color = (alpha << 24) | (i % 4 == 0 ? 0xD66BFF : i % 4 == 1 ? 0x7C3FD9 : i % 4 == 2 ? 0x39D6FF : 0xB155D4);
            if (i % 6 == 0) {
                drawDiamondGlyph(graphics, x, y, color);
            } else {
                graphics.fill(x, y, x + 1, y + 1, color);
            }
        }

        int glow = 18 + Mth.floor((Mth.sin(time * 0.55F) + 1.0F) * 9.0F);
        graphics.fill(4, 4, imageWidth - 4, 18, (glow << 24) | 0x3E155A);
        graphics.fill(4, imageHeight - 18, imageWidth - 4, imageHeight - 4, (glow << 24) | 0x3E155A);
    }

    private float portalTime(float partialTick) {
        if (minecraft != null && minecraft.level != null) {
            return (minecraft.level.getGameTime() + partialTick) * 0.12F;
        }
        return (System.currentTimeMillis() % 120000L) / 420.0F;
    }

    private void drawTabs(GuiGraphics graphics, int mouseX, int mouseY) {
        drawRitualBuildButton(graphics, mouseX, mouseY);
        drawTabButton(graphics, MateriaTableTab.ESSENCE, 0, mouseX, mouseY);
        drawTabButton(graphics, MateriaTableTab.BACKPACK, 1, mouseX, mouseY);
        drawTabButton(graphics, MateriaTableTab.FURNACE, 2, mouseX, mouseY);
    }

    private void drawRitualBuildButton(GuiGraphics graphics, int mouseX, int mouseY) {
        boolean enabled = menu.canPrepareUpgradeRitual();
        boolean hovered = enabled && isInside(
                mouseX,
                mouseY,
                leftPos + RITUAL_BUILD_X,
                topPos + TAB_Y,
                RITUAL_BUILD_WIDTH,
                TAB_HEIGHT
        );
        drawButtonFrame(
                graphics,
                RITUAL_BUILD_X,
                TAB_Y,
                RITUAL_BUILD_WIDTH,
                TAB_HEIGHT,
                hovered,
                !enabled
        );
        graphics.drawCenteredString(
                font,
                Component.translatable(enabled
                        ? "screen.materia_reborn.materia_table.upgrade_table"
                        : "screen.materia_reborn.materia_table.max_level"),
                RITUAL_BUILD_X + RITUAL_BUILD_WIDTH / 2,
                TAB_Y + 6,
                enabled ? hovered ? POSITIVE : TEXT_PRIMARY : TEXT_DIM
        );
    }

    private void drawRitualPreviewPanel(GuiGraphics graphics, int mouseX, int mouseY) {
        MateriaTableUpgrade upgrade = menu.nextTableUpgrade().orElse(null);
        if (upgrade == null) {
            return;
        }

        drawPanel(
                graphics,
                RITUAL_PREVIEW_PANEL_X,
                RITUAL_PREVIEW_PANEL_Y,
                RITUAL_PREVIEW_PANEL_WIDTH,
                RITUAL_PREVIEW_PANEL_HEIGHT
        );
        graphics.drawCenteredString(
                font,
                Component.translatable("screen.materia_reborn.materia_table.ritual_preview"),
                RITUAL_PREVIEW_PANEL_X + RITUAL_PREVIEW_PANEL_WIDTH / 2,
                RITUAL_PREVIEW_PANEL_Y + 7,
                TEXT_PRIMARY
        );
        graphics.drawCenteredString(
                font,
                Component.translatable(
                        "screen.materia_reborn.materia_table.ritual_target",
                        upgrade.targetTier() - 1,
                        upgrade.targetTier()
                ),
                RITUAL_PREVIEW_PANEL_X + RITUAL_PREVIEW_PANEL_WIDTH / 2,
                RITUAL_PREVIEW_PANEL_Y + 20,
                ACCENT
        );

        int rowY = RITUAL_PREVIEW_ROW_Y;
        drawRitualRequirement(graphics, upgrade.blockA().asItem(), missingRitualBlockCount(upgrade, upgrade.blockA()), rowY, true);
        rowY += 18;
        drawRitualRequirement(graphics, upgrade.blockB().asItem(), missingRitualBlockCount(upgrade, upgrade.blockB()), rowY, true);
        rowY += 18;
        drawRitualRequirement(graphics, upgrade.blockC().asItem(), missingRitualBlockCount(upgrade, upgrade.blockC()), rowY, true);
        rowY += 18;

        int missingLiquids = missingRitualLiquidCount(upgrade);
        drawRitualRequirement(
                graphics,
                ModItems.ESSENCE.get(),
                missingLiquids * LiquidEssenceRecipe.ESSENCE_COUNT,
                rowY,
                true
        );
        rowY += 18;
        drawRitualRequirement(
                graphics,
                ModItems.ESSENCE_CRYSTAL.get(),
                missingLiquids * LiquidEssenceRecipe.CRYSTAL_COUNT,
                rowY,
                true
        );
        rowY += 18;
        drawRitualRequirement(graphics, Items.WATER_BUCKET, missingRitualWaterBucketCount(upgrade), rowY, true);
        rowY += 18;
        drawRitualRequirement(graphics, upgrade.core(), 1, rowY, false);
        rowY += 18;
        drawRitualActivationCost(graphics, upgrade.essenceCost(), rowY);

        boolean hovered = isInside(
                mouseX,
                mouseY,
                leftPos + RITUAL_PREVIEW_BUILD_X,
                topPos + RITUAL_PREVIEW_BUILD_Y,
                RITUAL_PREVIEW_BUILD_WIDTH,
                RITUAL_PREVIEW_BUILD_HEIGHT
        );
        drawButtonFrame(
                graphics,
                RITUAL_PREVIEW_BUILD_X,
                RITUAL_PREVIEW_BUILD_Y,
                RITUAL_PREVIEW_BUILD_WIDTH,
                RITUAL_PREVIEW_BUILD_HEIGHT,
                hovered,
                false
        );
        graphics.drawCenteredString(
                font,
                Component.translatable("screen.materia_reborn.materia_table.build_ritual"),
                RITUAL_PREVIEW_BUILD_X + RITUAL_PREVIEW_BUILD_WIDTH / 2,
                RITUAL_PREVIEW_BUILD_Y + 6,
                hovered ? POSITIVE : TEXT_PRIMARY
        );
    }

    private void drawRitualRequirement(GuiGraphics graphics, ItemLike itemLike, int count, int y, boolean creativeMaterial) {
        ItemStack stack = new ItemStack(itemLike);
        graphics.renderItem(stack, RITUAL_PREVIEW_ROW_X, y - 3);
        boolean suppliedByCreative = creativeMaterial
                && minecraft.player != null
                && minecraft.player.isCreative();
        int color = count <= 0 || suppliedByCreative
                ? POSITIVE
                : inventoryItemCount(stack.getItem()) >= count ? TEXT_PRIMARY : DANGER;
        graphics.drawString(
                font,
                Component.literal(count + "x ").append(stack.getHoverName()),
                RITUAL_PREVIEW_ROW_X + 20,
                y + 1,
                color,
                false
        );
    }

    private void drawRitualActivationCost(GuiGraphics graphics, long essenceCost, int y) {
        graphics.blit(
                ESSENCE_ICON,
                RITUAL_PREVIEW_ROW_X + 1,
                y - 2,
                14,
                14,
                0.0F,
                0.0F,
                32,
                32,
                32,
                32
        );
        int color = menu.displayedEssence().compareTo(EssenceAmount.of(essenceCost)) >= 0
                ? TEXT_PRIMARY
                : DANGER;
        graphics.drawString(
                font,
                Component.translatable(
                        "screen.materia_reborn.materia_table.ritual_activation_cost",
                        essenceCost
                ),
                RITUAL_PREVIEW_ROW_X + 20,
                y + 1,
                color,
                false
        );
    }

    private int missingRitualBlockCount(MateriaTableUpgrade upgrade, Block expected) {
        int count = 0;
        for (int index = 0; index < upgrade.blockCount(); index++) {
            if (upgrade.expectedBlock(index) == expected
                    && (minecraft.level == null
                    || !minecraft.level.getBlockState(upgrade.ritualBlockPos(menu.tableBlockPos(), index)).is(expected))) {
                count++;
            }
        }
        return count;
    }

    private int missingRitualLiquidCount(MateriaTableUpgrade upgrade) {
        int count = 0;
        for (int index = 0; index < upgrade.blockCount(); index++) {
            if (!isRitualLiquidPresent(upgrade, index)) {
                count++;
            }
        }
        return count;
    }

    private int missingRitualWaterBucketCount(MateriaTableUpgrade upgrade) {
        if (minecraft.level == null) {
            return upgrade.blockCount();
        }
        int count = 0;
        for (int index = 0; index < upgrade.blockCount(); index++) {
            if (isRitualLiquidPresent(upgrade, index)) {
                continue;
            }
            BlockPos pos = upgrade.ritualEssencePos(menu.tableBlockPos(), index);
            if (!minecraft.level.getBlockState(pos).is(Blocks.WATER)
                    || !minecraft.level.getFluidState(pos).isSource()) {
                count++;
            }
        }
        return count;
    }

    private boolean isRitualLiquidPresent(MateriaTableUpgrade upgrade, int index) {
        if (minecraft.level == null) {
            return false;
        }
        BlockPos pos = upgrade.ritualEssencePos(menu.tableBlockPos(), index);
        return minecraft.level.getFluidState(pos).is(ModFluids.LIQUID_ESSENCE.get())
                && minecraft.level.getFluidState(pos).isSource();
    }

    private int inventoryItemCount(Item item) {
        if (minecraft.player == null) {
            return 0;
        }
        int count = 0;
        Inventory inventory = minecraft.player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.is(item)) {
                count += stack.getCount();
            }
        }
        return count;
    }
    private void drawNavigation(GuiGraphics graphics, int mouseX, int mouseY) {
        drawButtonFrame(
                graphics,
                NAV_LEFT_X,
                NAV_Y,
                NAV_SIZE,
                NAV_SIZE,
                isInside(mouseX, mouseY, leftPos + NAV_LEFT_X, topPos + NAV_Y, NAV_SIZE, NAV_SIZE),
                false
        );
        drawButtonFrame(
                graphics,
                NAV_RIGHT_X,
                NAV_Y,
                NAV_SIZE,
                NAV_SIZE,
                isInside(mouseX, mouseY, leftPos + NAV_RIGHT_X, topPos + NAV_Y, NAV_SIZE, NAV_SIZE),
                false
        );
        graphics.drawCenteredString(font, "<", NAV_LEFT_X + NAV_SIZE / 2, NAV_Y + 8, TEXT_PRIMARY);
        graphics.drawCenteredString(font, ">", NAV_RIGHT_X + NAV_SIZE / 2, NAV_Y + 8, TEXT_PRIMARY);
    }

    private void drawCommonPanels(GuiGraphics graphics) {
        drawPanel(graphics, SIDEBAR_X, MAIN_PANEL_Y, SIDEBAR_WIDTH, PANEL_HEIGHT);
        drawPanel(graphics, SIDEBAR_X, MAIN_PANEL_Y + PANEL_HEIGHT + 10, SIDEBAR_WIDTH, 86);
        drawPanel(graphics, CONTENT_X, MAIN_PANEL_Y, CONTENT_WIDTH, 116);
        drawPanel(graphics, CONTENT_X, MAIN_PANEL_Y + 126, CONTENT_WIDTH, 68);
    }

    private void drawEssencePanels(GuiGraphics graphics) {
        drawPanel(graphics, SIDEBAR_X, MAIN_PANEL_Y, SIDEBAR_WIDTH, 76);
        drawPanel(graphics, SIDEBAR_X, 160, SIDEBAR_WIDTH, 50);

        drawPanel(graphics, 134, MAIN_PANEL_Y, 120, 100);
        drawPanel(graphics, 134, 184, 120, 96);

        drawPanel(graphics, 262, MAIN_PANEL_Y, 152, 202);
    }

    private void drawBackpackPanels(GuiGraphics graphics) {
        drawPanel(graphics, SIDEBAR_X, MAIN_PANEL_Y, SIDEBAR_WIDTH, 76);
        drawPanel(graphics, SIDEBAR_X, 160, SIDEBAR_WIDTH, 64);

        drawPanel(graphics, CONTENT_X, MAIN_PANEL_Y, CONTENT_WIDTH, 202);
    }

    private void drawSearchIcon(GuiGraphics graphics, int x, int y, int color) {
        graphics.fill(x + 2, y, x + 5, y + 1, color);
        graphics.fill(x + 1, y + 1, x + 2, y + 4, color);
        graphics.fill(x + 5, y + 1, x + 6, y + 4, color);
        graphics.fill(x + 2, y + 4, x + 5, y + 5, color);
        graphics.fill(x + 4, y + 4, x + 5, y + 5, color);
        graphics.fill(x + 5, y + 5, x + 6, y + 6, color);
        graphics.fill(x + 6, y + 6, x + 7, y + 7, color);
    }

    private void drawLockIcon(GuiGraphics graphics, int x, int y, int color) {
        graphics.fill(x + 2, y, x + 5, y + 1, color);
        graphics.fill(x + 1, y + 1, x + 2, y + 3, color);
        graphics.fill(x + 5, y + 1, x + 6, y + 3, color);
        graphics.fill(x, y + 3, x + 7, y + 8, color);
        graphics.fill(x + 3, y + 5, x + 4, y + 6, 0xFF000000);
    }

    private void drawEssenceTab(GuiGraphics graphics, int mouseX, int mouseY) {
        Slot analyzeSlot = menu.slots.get(0);
        int inputIndex = menu.inputEssenceItemIndex();
        int hoveredCatalogIndex = essenceCatalogIndexAt(mouseX, mouseY);
        int carriedCatalogIndex = menu.carriedEssenceItemIndex();
        boolean hoveredUnlocked = hoveredCatalogIndex >= 0
                && menu.isEssenceItemUnlocked(hoveredCatalogIndex);
        boolean carriedUnlocked = carriedCatalogIndex >= 0
                && menu.isEssenceItemUnlocked(carriedCatalogIndex);
        if (hoveredUnlocked) {
            inspectedEssenceCatalogIndex = hoveredCatalogIndex;
        } else if (carriedUnlocked) {
            inspectedEssenceCatalogIndex = carriedCatalogIndex;
        } else if (!isInside(
                mouseX,
                mouseY,
                leftPos + 134,
                topPos + MAIN_PANEL_Y,
                ESSENCE_CATALOG_X - 134,
                100
        )) {
            inspectedEssenceCatalogIndex = -1;
        }
        if (!menu.canRemoveEssenceUnlock(inspectedEssenceCatalogIndex)) {
            inspectedEssenceCatalogIndex = -1;
        }

        ItemStack displayedStack = inspectedEssenceCatalogIndex >= 0
                ? menu.essenceCatalogStack(inspectedEssenceCatalogIndex)
                : analyzeSlot.getItem();
        if (!displayedStack.isEmpty()) {
            graphics.pose().pushPose();
            graphics.pose().translate(55, 90, 0.0F);
            graphics.pose().scale(2.0F, 2.0F, 1.0F);
            graphics.renderFakeItem(displayedStack, 0, 0);
            graphics.pose().popPose();
        }

        Component statusValue;
        int statusColor;
        if (inspectedEssenceCatalogIndex >= 0) {
            statusValue = Component.translatable("screen.materia_reborn.materia_table.status.unlocked");
            statusColor = POSITIVE;
        } else if (inputIndex < 0) {
            boolean analyzedSelection = menu.canUnlockEssenceItem();
            statusValue = Component.translatable(analyzedSelection
                    ? "screen.materia_reborn.materia_table.status.analyzed"
                    : "screen.materia_reborn.materia_table.status.no_item");
            statusColor = analyzedSelection ? POSITIVE : TEXT_DIM;
        } else if (menu.isEssenceItemUnlocked(inputIndex)) {
            statusValue = Component.translatable("screen.materia_reborn.materia_table.status.unlocked");
            statusColor = POSITIVE;
        } else {
            int progress = menu.essenceAnalysisProgress(inputIndex);
            int required = menu.essenceAnalysisRequired(inputIndex);
            if (progress >= required) {
                statusValue = Component.translatable("screen.materia_reborn.materia_table.status.analyzed");
                statusColor = POSITIVE;
            } else {
                statusValue = Component.translatable(
                        "screen.materia_reborn.materia_table.status.progress",
                        progress,
                        required
                );
                statusColor = ESSENCE_BLUE;
            }
        }
        graphics.drawCenteredString(
                font,
                statusValue,
                SIDEBAR_X + SIDEBAR_WIDTH / 2,
                136,
                statusColor
        );

        graphics.drawCenteredString(
                font,
                Component.translatable("screen.materia_reborn.materia_table.input_item"),
                SIDEBAR_X + SIDEBAR_WIDTH / 2,
                166,
                TEXT_PRIMARY
        );

        boolean analyzeEnabled = menu.canAnalyzeEssenceItem();
        boolean unlockEnabled = menu.canUnlockEssenceItem();
        boolean sellEnabled = menu.canSellEssenceItem();
        boolean analyzeHovered = isInside(mouseX, mouseY, leftPos + 22, topPos + 218, 98, 20);
        boolean unlockHovered = isInside(mouseX, mouseY, leftPos + 22, topPos + 239, 98, 20);
        boolean sellHovered = isInside(mouseX, mouseY, leftPos + 22, topPos + 260, 98, 20);
        boolean autoSellHovered = isInside(mouseX, mouseY, leftPos + 22, topPos + 281, 98, 20);
        drawActionButton(
                graphics,
                22,
                218,
                98,
                20,
                Component.translatable("screen.materia_reborn.materia_table.analyze"),
                analyzeEnabled && analyzeHovered,
                !analyzeEnabled
        );
        drawActionButton(
                graphics,
                22,
                239,
                98,
                20,
                Component.translatable("screen.materia_reborn.materia_table.unlock"),
                unlockEnabled && unlockHovered,
                !unlockEnabled
        );
        drawActionButton(
                graphics,
                22,
                260,
                98,
                20,
                Component.translatable("screen.materia_reborn.materia_table.sell"),
                sellEnabled && sellHovered,
                !sellEnabled
        );
        drawActionButton(
                graphics,
                22,
                281,
                98,
                20,
                Component.translatable(menu.isAutoSellEnabled()
                        ? "screen.materia_reborn.materia_table.auto_sell.on"
                        : "screen.materia_reborn.materia_table.auto_sell.off"),
                autoSellHovered || menu.isAutoSellEnabled(),
                false
        );

        graphics.drawCenteredString(
                font,
                Component.translatable("screen.materia_reborn.materia_table.info"),
                194,
                84,
                TEXT_PRIMARY
        );
        boolean showingCatalogInfo = inspectedEssenceCatalogIndex >= 0;
        int infoIndex = showingCatalogInfo ? inspectedEssenceCatalogIndex : inputIndex;
        if (infoIndex >= 0) {
            int sellValueY = showingCatalogInfo ? 107 : 98;
            int purchaseCostY = showingCatalogInfo ? 122 : 113;
            graphics.drawString(
                    font,
                    Component.translatable("screen.materia_reborn.materia_table.sell_value"),
                    140,
                    sellValueY,
                    TEXT_SECONDARY,
                    false
            );
            graphics.drawString(
                    font,
                    Component.translatable("screen.materia_reborn.materia_table.purchase_cost"),
                    140,
                    purchaseCostY,
                    TEXT_SECONDARY,
                    false
            );
            String sellValue = MateriaTableMenu.formatEssenceValue(menu.essenceItemSellValue(infoIndex));
            String purchaseCost = MateriaTableMenu.formatEssenceValue(menu.essenceItemPurchaseCost(infoIndex));
            drawEssenceAmount(graphics, 210, sellValueY, sellValue, WARNING);
            drawEssenceAmount(graphics, 210, purchaseCostY, purchaseCost, ESSENCE_BLUE);

            if (showingCatalogInfo) {
                boolean removeHovered = isInside(
                        mouseX,
                        mouseY,
                        leftPos + 140,
                        topPos + 150,
                        108,
                        20
                );
                drawActionButton(
                        graphics,
                        140,
                        150,
                        108,
                        20,
                        Component.translatable("screen.materia_reborn.materia_table.remove_unlock"),
                        removeHovered,
                        false
                );
            } else {
                graphics.drawString(
                        font,
                        Component.translatable("screen.materia_reborn.materia_table.unlock_requirement"),
                        140,
                        133,
                        TEXT_SECONDARY,
                        false
                );
                Component requirement = Component.literal(
                        menu.essenceAnalysisProgress(inputIndex)
                                + " / "
                                + menu.essenceAnalysisRequired(inputIndex)
                                + " "
                ).append(menu.essenceCatalogStack(inputIndex).getHoverName());
                graphics.drawCenteredString(font, requirement, 194, 146, ESSENCE_BLUE);
            }
        }

        graphics.drawCenteredString(
                font,
                Component.translatable("screen.materia_reborn.materia_table.crafting"),
                194,
                190,
                TEXT_PRIMARY
        );
        drawCraftingPlaceholder(graphics, 146, 206);

        graphics.drawCenteredString(
                font,
                Component.translatable("screen.materia_reborn.materia_table.essence_items"),
                338,
                84,
                TEXT_PRIMARY
        );
        drawSearchBar(
                graphics,
                ESSENCE_CATALOG_X,
                ESSENCE_FILTER_BUTTON_Y,
                ESSENCE_SEARCH_WIDTH,
                Component.translatable("screen.materia_reborn.materia_table.search"),
                essenceSearch,
                essenceSearchFocused
        );
        boolean filterHovered = isInside(
                mouseX,
                mouseY,
                leftPos + ESSENCE_FILTER_BUTTON_X,
                topPos + ESSENCE_FILTER_BUTTON_Y,
                ESSENCE_FILTER_BUTTON_WIDTH,
                ESSENCE_FILTER_BUTTON_HEIGHT
        );
        drawActionButton(
                graphics,
                ESSENCE_FILTER_BUTTON_X,
                ESSENCE_FILTER_BUTTON_Y,
                ESSENCE_FILTER_BUTTON_WIDTH,
                ESSENCE_FILTER_BUTTON_HEIGHT,
                Component.translatable("screen.materia_reborn.materia_table.filter"),
                filterHovered || essenceFilterDropdownOpen || activeEssenceFilter != null,
                false
        );
        drawEssenceCatalog(graphics, mouseX, mouseY);
    }

    private void drawEssenceCatalog(GuiGraphics graphics, int mouseX, int mouseY) {
        List<Integer> matches = matchingEssenceCatalogIndices();
        clampEssenceCatalogPage(matches.size());
        int hoveredCatalogIndex = essenceCatalogIndexAt(mouseX, mouseY, matches);
        for (int visualSlot = 0; visualSlot < ESSENCE_CATALOG_PAGE_SIZE; visualSlot++) {
            int x = ESSENCE_CATALOG_X
                    + (visualSlot % ESSENCE_CATALOG_COLUMNS) * ESSENCE_CATALOG_SPACING;
            int y = ESSENCE_CATALOG_Y
                    + (visualSlot / ESSENCE_CATALOG_COLUMNS) * ESSENCE_CATALOG_SPACING;
            int catalogIndex = essenceCatalogIndexForVisualSlot(visualSlot, matches);
            if (catalogIndex == hoveredCatalogIndex && catalogIndex >= 0) {
                graphics.fill(
                        x - 2,
                        y - 2,
                        x + 18,
                        y + 18,
                        menu.canPurchaseEssenceItem(catalogIndex) ? 0xFF2A6830 : DANGER
                );
            }
            drawSlotBackground(graphics, x, y);
            if (catalogIndex >= 0) {
                ItemStack stack = menu.essenceCatalogStack(catalogIndex);
                graphics.renderFakeItem(stack, x, y);
                graphics.renderItemDecorations(font, stack, x, y);
            } else {
                drawLockIcon(graphics, x + 5, y + 4, 0xFF352F3C);
            }
        }
        drawEssenceCatalogPageNavigation(graphics, mouseX, mouseY, matches.size());
    }

    private int essenceCatalogIndexAt(double mouseX, double mouseY) {
        List<Integer> matches = matchingEssenceCatalogIndices();
        clampEssenceCatalogPage(matches.size());
        return essenceCatalogIndexAt(mouseX, mouseY, matches);
    }

    private int essenceCatalogIndexAt(double mouseX, double mouseY, List<Integer> matches) {
        for (int visualSlot = 0; visualSlot < ESSENCE_CATALOG_PAGE_SIZE; visualSlot++) {
            int x = ESSENCE_CATALOG_X
                    + (visualSlot % ESSENCE_CATALOG_COLUMNS) * ESSENCE_CATALOG_SPACING;
            int y = ESSENCE_CATALOG_Y
                    + (visualSlot / ESSENCE_CATALOG_COLUMNS) * ESSENCE_CATALOG_SPACING;
            if (isInside(mouseX, mouseY, leftPos + x, topPos + y, 16, 16)) {
                return essenceCatalogIndexForVisualSlot(visualSlot, matches);
            }
        }
        return -1;
    }

    private int essenceCatalogIndexForVisualSlot(int visualSlot, List<Integer> matches) {
        int position = essenceCatalogPage * ESSENCE_CATALOG_PAGE_SIZE + visualSlot;
        return position >= 0 && position < matches.size() ? matches.get(position) : -1;
    }

    private void drawEssenceFilterDropdown(GuiGraphics graphics, int mouseX, int mouseY) {
        ensureCreativeFiltersReady();
        drawPanel(
                graphics,
                ESSENCE_FILTER_PANEL_X,
                ESSENCE_FILTER_PANEL_Y,
                ESSENCE_FILTER_PANEL_WIDTH,
                ESSENCE_FILTER_PANEL_HEIGHT
        );
        graphics.drawString(
                font,
                Component.translatable("screen.materia_reborn.materia_table.filter"),
                ESSENCE_FILTER_PANEL_X + 7,
                ESSENCE_FILTER_PANEL_Y + 8,
                TEXT_PRIMARY,
                false
        );

        boolean canReset = activeEssenceFilter != null;
        boolean resetHovered = canReset && isInside(
                mouseX,
                mouseY,
                leftPos + ESSENCE_FILTER_RESET_X,
                topPos + ESSENCE_FILTER_RESET_Y,
                ESSENCE_FILTER_RESET_SIZE,
                ESSENCE_FILTER_RESET_SIZE
        );
        drawButtonFrame(
                graphics,
                ESSENCE_FILTER_RESET_X,
                ESSENCE_FILTER_RESET_Y,
                ESSENCE_FILTER_RESET_SIZE,
                ESSENCE_FILTER_RESET_SIZE,
                resetHovered,
                !canReset
        );
        graphics.drawCenteredString(
                font,
                "x",
                ESSENCE_FILTER_RESET_X + ESSENCE_FILTER_RESET_SIZE / 2,
                ESSENCE_FILTER_RESET_Y + 3,
                canReset ? TEXT_PRIMARY : TEXT_DIM
        );

        EssenceCreativeFilter[] filters = EssenceCreativeFilter.values();
        for (int index = 0; index < filters.length; index++) {
            EssenceCreativeFilter filter = filters[index];
            int x = essenceFilterOptionX(index);
            int y = essenceFilterOptionY(index);
            boolean hovered = isInside(
                    mouseX,
                    mouseY,
                    leftPos + x,
                    topPos + y,
                    ESSENCE_FILTER_OPTION_SIZE,
                    ESSENCE_FILTER_OPTION_SIZE
            );
            drawButtonFrame(
                    graphics,
                    x,
                    y,
                    ESSENCE_FILTER_OPTION_SIZE,
                    ESSENCE_FILTER_OPTION_SIZE,
                    hovered || filter == activeEssenceFilter,
                    false
            );
            graphics.renderFakeItem(filter.icon(), x + 3, y + 3);
        }
    }

    private EssenceCreativeFilter essenceFilterAt(double mouseX, double mouseY) {
        EssenceCreativeFilter[] filters = EssenceCreativeFilter.values();
        for (int index = 0; index < filters.length; index++) {
            if (isInside(
                    mouseX,
                    mouseY,
                    leftPos + essenceFilterOptionX(index),
                    topPos + essenceFilterOptionY(index),
                    ESSENCE_FILTER_OPTION_SIZE,
                    ESSENCE_FILTER_OPTION_SIZE
            )) {
                return filters[index];
            }
        }
        return null;
    }

    private int essenceFilterOptionX(int index) {
        return ESSENCE_FILTER_GRID_X
                + (index % ESSENCE_FILTER_COLUMNS)
                * (ESSENCE_FILTER_OPTION_SIZE + ESSENCE_FILTER_OPTION_GAP);
    }

    private int essenceFilterOptionY(int index) {
        return ESSENCE_FILTER_GRID_Y
                + (index / ESSENCE_FILTER_COLUMNS)
                * (ESSENCE_FILTER_OPTION_SIZE + ESSENCE_FILTER_OPTION_GAP);
    }

    private void ensureCreativeFiltersReady() {
        if (creativeFiltersInitialized
                || minecraft == null
                || minecraft.level == null) {
            return;
        }
        CreativeModeTabs.tryRebuildTabContents(
                minecraft.level.enabledFeatures(),
                false,
                minecraft.level.registryAccess()
        );
        essenceCreativeFilterCache.clear();
        creativeFiltersInitialized = true;
    }

    private List<Integer> essenceCatalogIndicesForFilter(EssenceCreativeFilter filter) {
        ensureCreativeFiltersReady();
        return essenceCreativeFilterCache.computeIfAbsent(
                filter,
                this::buildEssenceCatalogIndicesForFilter
        );
    }

    private List<Integer> buildEssenceCatalogIndicesForFilter(EssenceCreativeFilter filter) {
        Map<Item, Integer> creativeOrder = new IdentityHashMap<>();
        int creativeIndex = 0;
        for (ItemStack stack : filter.tab().getDisplayItems()) {
            creativeOrder.putIfAbsent(stack.getItem(), creativeIndex++);
        }

        List<Integer> catalogIndices = new ArrayList<>();
        for (int catalogIndex = 0; catalogIndex < menu.essenceCatalogSize(); catalogIndex++) {
            Item item = menu.essenceCatalogStack(catalogIndex).getItem();
            if (creativeOrder.containsKey(item)) {
                catalogIndices.add(catalogIndex);
            }
        }
        catalogIndices.sort((left, right) -> {
            Item leftItem = menu.essenceCatalogStack(left).getItem();
            Item rightItem = menu.essenceCatalogStack(right).getItem();
            int order = Integer.compare(
                    creativeOrder.get(leftItem),
                    creativeOrder.get(rightItem)
            );
            return order != 0 ? order : Integer.compare(left, right);
        });
        return List.copyOf(catalogIndices);
    }

    private List<Integer> matchingEssenceCatalogIndices() {
        String query = essenceSearch.strip().toLowerCase(Locale.ROOT);
        List<Integer> matches = new ArrayList<>();
        if (activeEssenceFilter == null) {
            for (int catalogIndex = 0; catalogIndex < menu.essenceCatalogSize(); catalogIndex++) {
                if (matchesEssenceCatalogEntry(catalogIndex, query)) {
                    matches.add(catalogIndex);
                }
            }
        } else {
            for (int catalogIndex : essenceCatalogIndicesForFilter(activeEssenceFilter)) {
                if (matchesEssenceCatalogEntry(catalogIndex, query)) {
                    matches.add(catalogIndex);
                }
            }
        }
        return matches;
    }

    private boolean matchesEssenceCatalogEntry(int catalogIndex, String query) {
        if (!menu.isEssenceItemAvailable(catalogIndex)
                || !menu.isEssenceItemUnlocked(catalogIndex)) {
            return false;
        }
        if (query.isEmpty()) {
            return true;
        }
        ItemStack stack = menu.essenceCatalogStack(catalogIndex);
        String displayName = stack.getHoverName().getString().toLowerCase(Locale.ROOT);
        String catalogId = menu.essenceCatalogId(catalogIndex)
                .toLowerCase(Locale.ROOT)
                .replace('_', ' ');
        return displayName.contains(query) || catalogId.contains(query);
    }

    private int essenceCatalogPageCount(int matchCount) {
        return Math.max(1, (matchCount + ESSENCE_CATALOG_PAGE_SIZE - 1) / ESSENCE_CATALOG_PAGE_SIZE);
    }

    private void clampEssenceCatalogPage(int matchCount) {
        essenceCatalogPage = Mth.clamp(essenceCatalogPage, 0, essenceCatalogPageCount(matchCount) - 1);
    }

    private boolean changeEssenceCatalogPage(int direction) {
        int matchCount = matchingEssenceCatalogIndices().size();
        int previous = essenceCatalogPage;
        essenceCatalogPage = Mth.clamp(
                essenceCatalogPage + direction,
                0,
                essenceCatalogPageCount(matchCount) - 1
        );
        if (previous != essenceCatalogPage) {
            inspectedEssenceCatalogIndex = -1;
            return true;
        }
        return false;
    }

    private void drawEssenceCatalogPageNavigation(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            int matchCount
    ) {
        int pageCount = essenceCatalogPageCount(matchCount);
        boolean previousEnabled = essenceCatalogPage > 0;
        boolean nextEnabled = essenceCatalogPage + 1 < pageCount;
        boolean previousHovered = previousEnabled && isInside(
                mouseX,
                mouseY,
                leftPos + ESSENCE_CATALOG_PREVIOUS_X,
                topPos + ESSENCE_CATALOG_PAGE_Y,
                ESSENCE_CATALOG_PAGE_BUTTON_WIDTH,
                ESSENCE_CATALOG_PAGE_BUTTON_HEIGHT
        );
        boolean nextHovered = nextEnabled && isInside(
                mouseX,
                mouseY,
                leftPos + ESSENCE_CATALOG_NEXT_X,
                topPos + ESSENCE_CATALOG_PAGE_Y,
                ESSENCE_CATALOG_PAGE_BUTTON_WIDTH,
                ESSENCE_CATALOG_PAGE_BUTTON_HEIGHT
        );
        drawButtonFrame(
                graphics,
                ESSENCE_CATALOG_PREVIOUS_X,
                ESSENCE_CATALOG_PAGE_Y,
                ESSENCE_CATALOG_PAGE_BUTTON_WIDTH,
                ESSENCE_CATALOG_PAGE_BUTTON_HEIGHT,
                previousHovered,
                !previousEnabled
        );
        drawButtonFrame(
                graphics,
                ESSENCE_CATALOG_NEXT_X,
                ESSENCE_CATALOG_PAGE_Y,
                ESSENCE_CATALOG_PAGE_BUTTON_WIDTH,
                ESSENCE_CATALOG_PAGE_BUTTON_HEIGHT,
                nextHovered,
                !nextEnabled
        );
        graphics.drawCenteredString(
                font,
                "<",
                ESSENCE_CATALOG_PREVIOUS_X + ESSENCE_CATALOG_PAGE_BUTTON_WIDTH / 2,
                ESSENCE_CATALOG_PAGE_Y + 3,
                previousEnabled ? TEXT_PRIMARY : TEXT_DIM
        );
        graphics.drawCenteredString(
                font,
                ">",
                ESSENCE_CATALOG_NEXT_X + ESSENCE_CATALOG_PAGE_BUTTON_WIDTH / 2,
                ESSENCE_CATALOG_PAGE_Y + 3,
                nextEnabled ? TEXT_PRIMARY : TEXT_DIM
        );
        String pageText = matchCount == 0
                ? "0 / 0"
                : (essenceCatalogPage + 1) + " / " + pageCount;
        graphics.drawCenteredString(font, pageText, 338, ESSENCE_CATALOG_PAGE_Y + 3, TEXT_SECONDARY);
    }
    private void drawBackpackTab(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.blit(BACKPACK_ICON, 53, 96, 36, 36, 0.0F, 0.0F, 64, 64, 64, 64);

        graphics.drawCenteredString(font, Component.translatable("container.materia_reborn.materia_table.backpack"), SIDEBAR_X + SIDEBAR_WIDTH / 2, 84, TEXT_PRIMARY);

        String slotsLabel = Component.translatable("screen.materia_reborn.materia_table.slots").getString() + ": ";
        String slotsVal = menu.unlockedStorageSlots() + " / "
                + MateriaTableProgression.maxStorageSlots(menu.tableTier());
        int wS1 = font.width(slotsLabel);
        int wS2 = font.width(slotsVal);
        int totalS = wS1 + wS2;
        int startS = SIDEBAR_X + (SIDEBAR_WIDTH - totalS) / 2;
        graphics.drawString(font, slotsLabel, startS, 136, TEXT_SECONDARY, false);
        graphics.drawString(font, slotsVal, startS + wS1, 136, ESSENCE_BLUE, false);

        Component upgradeLabel = Component.translatable("screen.materia_reborn.materia_table.upgrade_cost");
        graphics.drawString(font, upgradeLabel, 22, 171, TEXT_SECONDARY, false);
        boolean upgradeHovered = isInside(mouseX, mouseY, leftPos + 22, topPos + 232, 98, 20);
        boolean bulkUpgrade = upgradeHovered && hasShiftDown();
        drawUpgradeCost(graphics, 22 + font.width(upgradeLabel) + 8, 171, upgradeHovered, bulkUpgrade);

        drawUpgradeButton(graphics, 22, 232, 98, 20, upgradeHovered, bulkUpgrade);

        boolean extrasHovered = isInside(mouseX, mouseY, leftPos + 22, topPos + 256, 98, 20);
        if (menu.canUseBackpackExtras()) {
            drawActionButton(graphics, 22, 256, 98, 20, Component.translatable("screen.materia_reborn.materia_table.extras"), extrasHovered || backpackExtrasDropdownOpen, false);
        } else {
            drawActionButton(graphics, 22, 256, 98, 20, Component.literal("Unlocked at " + MateriaConfig.backpackExtrasUnlockSlots() + " slots"), false, true);
        }


        if (menu.hasBackpackFilterUpgrade()) {
            boolean filterHovered = isInside(mouseX, mouseY, leftPos + 22, topPos + 280, 98, 20);
            drawActionButton(graphics, 22, 280, 98, 20, Component.translatable("screen.materia_reborn.materia_table.filter"), filterHovered || menu.isBackpackFilterOpen(), false);
        }

        drawSearchBar(graphics, 151, 84, 255, Component.translatable("screen.materia_reborn.materia_table.search_items"), backpackSearch, backpackSearchFocused);
        drawBackpackGridPlaceholders(graphics);

        String pageLabel = "Page " + (menu.getBackpackPage() + 1) + " / " + menu.backpackPageCount();
        drawPageFooter(graphics, mouseX, mouseY, 302, 262, pageLabel);

        int totalItems = menu.storageItemCount();
        int capacity = menu.storageItemCapacity();
        double percent = capacity <= 0 ? 0.0D : (totalItems * 100.0D) / capacity;
        String capText = String.format(java.util.Locale.US, "Capacity: %.2f%%", percent);
        graphics.drawString(font, capText, 168, 248, TEXT_SECONDARY, false);
        drawProgressBar(graphics, 168, 260, 112, 8, Math.min(totalItems, capacity), capacity, ACCENT);
    }

    private void drawFurnaceTab(GuiGraphics graphics, int mouseX, int mouseY) {
        // --- Sidebar (Left Panel) ---
        graphics.drawCenteredString(font, Component.translatable("container.materia_reborn.materia_table.furnace"), SIDEBAR_X + SIDEBAR_WIDTH / 2, 84, TEXT_PRIMARY);
        graphics.pose().pushPose();
        graphics.pose().translate(55, 96, 0.0F);
        graphics.pose().scale(2.0F, 2.0F, 1.0F);
        graphics.renderFakeItem(stack(Blocks.FURNACE, 1), 0, 0);
        graphics.pose().popPose();
        
        String slotsLabel = Component.translatable("screen.materia_reborn.materia_table.slots").getString() + ": ";
        int totalFurnaceInventorySlots = MateriaTableProgression.maxFurnaceSlotsPerSide(menu.tableTier()) * 2;
        String slotsVal = menu.unlockedFurnaceSlotCount() + " / " + totalFurnaceInventorySlots;
        int wS1 = font.width(slotsLabel);
        int wS2 = font.width(slotsVal);
        int totalS = wS1 + wS2;
        int startS = SIDEBAR_X + (SIDEBAR_WIDTH - totalS) / 2;
        graphics.drawString(font, slotsLabel, startS, 136, TEXT_SECONDARY, false);
        graphics.drawString(font, slotsVal, startS + wS1, 136, ESSENCE_BLUE, false);

        Component upgradeLabel = Component.translatable("screen.materia_reborn.materia_table.upgrade_cost");
        graphics.drawString(font, upgradeLabel, 22, 171, TEXT_SECONDARY, false);
        boolean upgradeHovered = isInside(mouseX, mouseY, leftPos + 22, topPos + 232, 98, 20);
        boolean bulkUpgrade = upgradeHovered && hasShiftDown();
        drawUpgradeCost(graphics, 22 + font.width(upgradeLabel) + 8, 171, upgradeHovered, bulkUpgrade);

        drawUpgradeButton(graphics, 22, 232, 98, 20, upgradeHovered, bulkUpgrade);

        boolean smeltHovered = isInside(mouseX, mouseY, leftPos + 22, topPos + 256, 98, 20);
        if (menu.canUseSmeltEssence()) {
            drawActionButton(graphics, 22, 256, 98, 20, Component.translatable("screen.materia_reborn.materia_table.smelt_essence"), smeltHovered || menu.isSmeltEssenceEnabled(), false);
        } else {
            Component lockedLabel = MateriaConfig.smeltEssenceEnabled()
                    ? Component.literal("Unlocked at " + MateriaConfig.smeltEssenceUnlockSlots() + " slots")
                    : Component.literal("Disabled in config");
            drawActionButton(graphics, 22, 256, 98, 20, lockedLabel, false, true);
        }

        boolean extrasHovered = isInside(mouseX, mouseY, leftPos + 22, topPos + 280, 98, 20);
        if (menu.canUseFurnaceExtras()) {
            drawActionButton(graphics, 22, 280, 98, 20, Component.translatable("screen.materia_reborn.materia_table.extras"), extrasHovered || extrasDropdownOpen, false);
        } else {
            drawActionButton(graphics, 22, 280, 98, 20, Component.literal("Unlocked at " + MateriaConfig.furnaceExtrasUnlockSlots() + " slots"), false, true);
        }
        // --- Content Panel (Right Panel) ---
        String inputStr = Component.translatable("screen.materia_reborn.materia_table.input").getString();
        graphics.drawString(font, inputStr, 195 - font.width(inputStr), 104, TEXT_SECONDARY, false);

        String fuelStr = Component.translatable("screen.materia_reborn.materia_table.fuel").getString();
        graphics.drawString(font, fuelStr, 195 - font.width(fuelStr), 150, TEXT_SECONDARY, false);

        String outputStr = Component.translatable("screen.materia_reborn.materia_table.output").getString();
        graphics.drawString(font, outputStr, 294, 122, TEXT_SECONDARY, false);
        drawXpClaimButton(graphics, mouseX, mouseY);

        if (menu.isSmeltEssenceEnabled()) {
            drawFlameIndicator(graphics, 202, 121, 18, ACCENT);
            drawGlowingLockedSlot(graphics, 200, 146);
        } else {
            drawFlameIndicator(graphics, 202, 121, menu.litProgressPixels(18), FURNACE_FLAME);
        }
        graphics.drawCenteredString(font, menu.getFurnaceBurnTimeText(), 244, 111, TEXT_PRIMARY);

        graphics.drawCenteredString(font, Component.translatable("screen.materia_reborn.materia_table.input_inventory"), 206, 168, TEXT_PRIMARY);
        graphics.drawCenteredString(font, Component.translatable("screen.materia_reborn.materia_table.output_inventory"), 342, 168, TEXT_PRIMARY);
        drawFurnaceInventoryPlaceholders(graphics, 152, 186);
        drawFurnaceInventoryPlaceholders(graphics, 288, 186);
        drawInventoryCapacity(graphics, 152, 246, 108, menu.furnaceInputInventoryItemCount(), menu.furnaceInventoryItemCapacity());
        drawInventoryCapacity(graphics, 288, 246, 108, menu.furnaceOutputInventoryItemCount(), menu.furnaceInventoryItemCapacity());
    }

    private void drawBackpackFilterPanel(GuiGraphics graphics) {
        drawPanel(graphics, EXTRAS_PANEL_X, BACKPACK_FILTER_PANEL_Y, EXTRAS_PANEL_WIDTH, BACKPACK_FILTER_PANEL_HEIGHT);
        graphics.drawCenteredString(font, Component.translatable("screen.materia_reborn.materia_table.filter"), EXTRAS_PANEL_X + EXTRAS_PANEL_WIDTH / 2, BACKPACK_FILTER_PANEL_Y + 7, TEXT_PRIMARY);
        int unlockedSlots = menu.backpackFilterSlotCount();
        for (int slot = 0; slot < PlayerMateriaProgress.MAX_ACTIVE_BACKPACK_FILTER_SLOTS; slot++) {
            int x = BACKPACK_FILTER_GHOST_X + (slot % BACKPACK_FILTER_GHOST_COLUMNS) * 18;
            int y = BACKPACK_FILTER_GHOST_Y + (slot / BACKPACK_FILTER_GHOST_COLUMNS) * 18;
            if (slot >= unlockedSlots) {
                drawLockedSlot(graphics, x, y);
            } else {
                drawSlotBackground(graphics, x, y);
                ItemStack ghostItem = menu.backpackFilterGhostItem(slot);
                if (!ghostItem.isEmpty()) {
                    graphics.renderFakeItem(ghostItem, x, y);
                }
            }
        }

        int buttonX = EXTRAS_PANEL_X + 180;
        drawActionButton(graphics, buttonX, BACKPACK_FILTER_PANEL_Y + 30, 88, 18, Component.translatable("screen.materia_reborn.materia_table.filter.reset"), false, false);

        boolean nbtUnlocked = menu.backpackExtraLevel(BackpackExtraUpgrade.FILTER) >= 3;
        drawActionButton(
                graphics,
                buttonX,
                BACKPACK_FILTER_PANEL_Y + 52,
                88,
                18,
                Component.translatable(nbtUnlocked
                        ? "screen.materia_reborn.materia_table.filter.ignore_nbt"
                        : "screen.materia_reborn.materia_table.filter.unlocked_nbt"),
                nbtUnlocked && menu.isBackpackFilterIgnoringNbt(),
                !nbtUnlocked
        );

        boolean damageUnlocked = menu.backpackExtraLevel(BackpackExtraUpgrade.FILTER) >= 4;
        drawActionButton(
                graphics,
                buttonX,
                BACKPACK_FILTER_PANEL_Y + 74,
                88,
                18,
                Component.translatable(damageUnlocked
                        ? "screen.materia_reborn.materia_table.filter.ignore_damage"
                        : "screen.materia_reborn.materia_table.filter.unlocked_damage"),
                damageUnlocked && menu.isBackpackFilterIgnoringDamage(),
                !damageUnlocked
        );
    }
    private void drawBackpackExtrasDropdown(GuiGraphics graphics, int mouseX, int mouseY) {
        drawPanel(graphics, EXTRAS_PANEL_X, BACKPACK_EXTRAS_PANEL_Y, EXTRAS_PANEL_WIDTH, BACKPACK_EXTRAS_PANEL_HEIGHT);
        for (BackpackExtraUpgrade upgrade : BackpackExtraUpgrade.values()) {
            int rowY = BACKPACK_EXTRAS_ROW_Y + upgrade.ordinal() * 16;
            drawBackpackExtraRow(graphics, mouseX, mouseY, upgrade, rowY);
        }
    }

    private void drawBackpackExtraRow(GuiGraphics graphics, int mouseX, int mouseY, BackpackExtraUpgrade upgrade, int y) {
        int level = menu.backpackExtraLevel(upgrade);
        int maximumLevel = menu.backpackExtraMaxLevel(upgrade);
        int cost = menu.nextBackpackExtraCost(upgrade);
        boolean locked = maximumLevel <= 0;
        boolean maxed = !locked && level >= maximumLevel;
        boolean hovered = isInside(mouseX, mouseY, leftPos + EXTRAS_BUY_X, topPos + y, 38, 14);
        boolean canAfford = menu.canAffordBackpackExtra(upgrade);

        graphics.drawString(font, Component.translatable("screen.materia_reborn.materia_table.backpack_extra." + upgrade.id()), EXTRAS_LABEL_X, y + 3, TEXT_PRIMARY, false);
        drawBackpackExtraSlider(graphics, upgrade, y);

        Component buttonLabel = Component.translatable(locked
                ? "screen.materia_reborn.materia_table.locked"
                : maxed ? "screen.materia_reborn.materia_table.max"
                : "screen.materia_reborn.materia_table.buy");
        boolean disabled = locked || maxed;
        int border = disabled ? PANEL_BORDER : hovered && canAfford ? 0xFF2A6830 : hovered ? DANGER : PANEL_BORDER;
        int fill = disabled ? DISABLED_FILL : hovered && canAfford ? 0xAA17331B : hovered ? 0x66261218 : 0xCC17151E;
        int textColor = disabled ? TEXT_DIM : hovered && canAfford ? POSITIVE : hovered ? DANGER : TEXT_PRIMARY;
        graphics.fill(EXTRAS_BUY_X, y, EXTRAS_BUY_X + 38, y + 14, border);
        graphics.fill(EXTRAS_BUY_X + 1, y + 1, EXTRAS_BUY_X + 37, y + 13, fill);
        graphics.drawCenteredString(font, buttonLabel, EXTRAS_BUY_X + 19, y + 3, textColor);

        String costText = cost > 0 ? String.valueOf(cost) : "-";
        drawEssenceAmount(graphics, EXTRAS_COST_X, y + 3, costText, disabled ? TEXT_DIM : hovered && !canAfford ? DANGER : TEXT_SECONDARY);
    }

    private void drawBackpackExtraSlider(GuiGraphics graphics, BackpackExtraUpgrade upgrade, int y) {
        int purchasedLevel = menu.backpackExtraLevel(upgrade);
        int setting = menu.backpackExtraSetting(upgrade);
        int sliderY = y + 3;
        if (menu.backpackExtraMaxLevel(upgrade) <= 0) {
            graphics.fill(EXTRAS_SLIDER_X, sliderY, EXTRAS_SLIDER_X + EXTRAS_SLIDER_WIDTH, sliderY + 10, SLOT_BORDER);
            graphics.fill(EXTRAS_SLIDER_X + 1, sliderY + 1, EXTRAS_SLIDER_X + EXTRAS_SLIDER_WIDTH - 1, sliderY + 9, SLOT_FILL);
            graphics.drawCenteredString(
                    font,
                    Component.translatable("screen.materia_reborn.materia_table.locked"),
                    EXTRAS_SLIDER_X + EXTRAS_SLIDER_WIDTH / 2,
                    sliderY + 1,
                    TEXT_DIM
            );
            return;
        }
        int filledWidth = purchasedLevel <= 0 ? 0 : (EXTRAS_SLIDER_WIDTH - 2) * setting / purchasedLevel;
        int knobX = EXTRAS_SLIDER_X + (purchasedLevel <= 0 ? 0 : (EXTRAS_SLIDER_WIDTH - 4) * setting / purchasedLevel);
        String value = setting + "/" + menu.backpackExtraOverallMaxLevel(upgrade);

        graphics.fill(EXTRAS_SLIDER_X, sliderY, EXTRAS_SLIDER_X + EXTRAS_SLIDER_WIDTH, sliderY + 10, SLOT_BORDER);
        graphics.fill(EXTRAS_SLIDER_X + 1, sliderY + 1, EXTRAS_SLIDER_X + EXTRAS_SLIDER_WIDTH - 1, sliderY + 9, SLOT_FILL);
        if (filledWidth > 0) {
            graphics.fill(EXTRAS_SLIDER_X + 1, sliderY + 1, EXTRAS_SLIDER_X + 1 + filledWidth, sliderY + 9, ACCENT);
        }
        graphics.fill(knobX, sliderY, knobX + 4, sliderY + 10, ACCENT_BORDER);
        graphics.drawCenteredString(font, value, EXTRAS_SLIDER_X + EXTRAS_SLIDER_WIDTH / 2, sliderY + 1, TEXT_PRIMARY);
    }
    private void drawFurnaceExtrasDropdown(GuiGraphics graphics, int mouseX, int mouseY) {
        drawPanel(graphics, EXTRAS_PANEL_X, EXTRAS_PANEL_Y, EXTRAS_PANEL_WIDTH, EXTRAS_PANEL_HEIGHT);
        for (FurnaceExtraUpgrade upgrade : FurnaceExtraUpgrade.values()) {
            int rowY = EXTRAS_ROW_Y + upgrade.ordinal() * 16;
            drawFurnaceExtraRow(graphics, mouseX, mouseY, upgrade, rowY);
        }
    }

    private void drawFurnaceExtraRow(GuiGraphics graphics, int mouseX, int mouseY, FurnaceExtraUpgrade upgrade, int y) {
        int level = menu.furnaceExtraLevel(upgrade);
        int maximumLevel = menu.furnaceExtraMaxLevel(upgrade);
        int cost = menu.nextFurnaceExtraCost(upgrade);
        boolean configDisabled = !MateriaConfig.furnaceUpgradeEnabled(upgrade.id());
        boolean comingSoon = upgrade == FurnaceExtraUpgrade.ESSENCE_GENERATOR && configDisabled;
        boolean locked = !comingSoon && (configDisabled || maximumLevel <= 0);
        boolean maxed = !locked && !comingSoon && level >= maximumLevel;
        boolean purchasable = !comingSoon && !locked && !maxed;
        int buttonWidth = comingSoon ? 110 : 38;
        boolean hovered = isInside(mouseX, mouseY, leftPos + EXTRAS_BUY_X, topPos + y, buttonWidth, 14);
        boolean canAfford = menu.canAffordFurnaceExtra(upgrade);

        graphics.drawString(font, Component.translatable("screen.materia_reborn.materia_table.extra." + upgrade.id()), EXTRAS_LABEL_X, y + 3, TEXT_PRIMARY, false);
        drawFurnaceExtraSlider(graphics, upgrade, y);

        Component buttonLabel = Component.translatable(
                comingSoon
                        ? "screen.materia_reborn.materia_table.balancing"
                        : locked ? "screen.materia_reborn.materia_table.locked"
                        : maxed ? "screen.materia_reborn.materia_table.max" : "screen.materia_reborn.materia_table.buy"
        );
        boolean disabled = !purchasable;
        int border = disabled ? PANEL_BORDER : hovered && canAfford ? 0xFF2A6830 : hovered ? DANGER : PANEL_BORDER;
        int fill = disabled ? DISABLED_FILL : hovered && canAfford ? 0xAA17331B : hovered ? 0x66261218 : 0xCC17151E;
        int textColor = disabled ? TEXT_DIM : hovered && canAfford ? POSITIVE : hovered ? DANGER : TEXT_PRIMARY;
        graphics.fill(EXTRAS_BUY_X, y, EXTRAS_BUY_X + buttonWidth, y + 14, border);
        graphics.fill(EXTRAS_BUY_X + 1, y + 1, EXTRAS_BUY_X + buttonWidth - 1, y + 13, fill);
        graphics.drawCenteredString(font, buttonLabel, EXTRAS_BUY_X + buttonWidth / 2, y + 3, textColor);

        if (!comingSoon) {
            String costText = cost > 0 ? String.valueOf(cost) : "-";
            drawEssenceAmount(graphics, EXTRAS_COST_X, y + 3, costText, disabled ? TEXT_DIM : hovered && !canAfford ? DANGER : TEXT_SECONDARY);
        }
    }

    private void drawFurnaceExtraSlider(GuiGraphics graphics, FurnaceExtraUpgrade upgrade, int y) {
        int purchasedLevel = menu.furnaceExtraLevel(upgrade);
        int setting = menu.furnaceExtraSetting(upgrade);
        int sliderY = y + 3;
        if (!upgrade.isAvailableForPurchase() || menu.furnaceExtraMaxLevel(upgrade) <= 0) {
            graphics.fill(EXTRAS_SLIDER_X, sliderY, EXTRAS_SLIDER_X + EXTRAS_SLIDER_WIDTH, sliderY + 10, SLOT_BORDER);
            graphics.fill(EXTRAS_SLIDER_X + 1, sliderY + 1, EXTRAS_SLIDER_X + EXTRAS_SLIDER_WIDTH - 1, sliderY + 9, SLOT_FILL);
            graphics.drawCenteredString(
                    font,
                    Component.translatable("screen.materia_reborn.materia_table.locked"),
                    EXTRAS_SLIDER_X + EXTRAS_SLIDER_WIDTH / 2,
                    sliderY + 1,
                    TEXT_DIM
            );
            return;
        }
        int filledWidth = purchasedLevel <= 0 ? 0 : (EXTRAS_SLIDER_WIDTH - 2) * setting / purchasedLevel;
        int knobX = EXTRAS_SLIDER_X + (purchasedLevel <= 0 ? 0 : (EXTRAS_SLIDER_WIDTH - 4) * setting / purchasedLevel);
        String value = setting + "/" + menu.furnaceExtraOverallMaxLevel(upgrade);

        graphics.fill(EXTRAS_SLIDER_X, sliderY, EXTRAS_SLIDER_X + EXTRAS_SLIDER_WIDTH, sliderY + 10, SLOT_BORDER);
        graphics.fill(EXTRAS_SLIDER_X + 1, sliderY + 1, EXTRAS_SLIDER_X + EXTRAS_SLIDER_WIDTH - 1, sliderY + 9, SLOT_FILL);
        if (filledWidth > 0) {
            graphics.fill(EXTRAS_SLIDER_X + 1, sliderY + 1, EXTRAS_SLIDER_X + 1 + filledWidth, sliderY + 9, ACCENT);
        }
        graphics.fill(knobX, sliderY, knobX + 4, sliderY + 10, ACCENT_BORDER);
        graphics.drawCenteredString(font, value, EXTRAS_SLIDER_X + EXTRAS_SLIDER_WIDTH / 2, sliderY + 1, TEXT_PRIMARY);
    }
    private int xpButtonX() {
        String output = Component.translatable("screen.materia_reborn.materia_table.output").getString();
        return 294 + font.width(output) + 4;
    }

    private void drawXpClaimButton(GuiGraphics graphics, int mouseX, int mouseY) {
        if (menu.furnaceExtraLevel(FurnaceExtraUpgrade.XP_STORAGE) < 1) {
            return;
        }
        int x = xpButtonX();
        int storedExperience = menu.storedExperience();
        boolean enabled = menu.hasXpStorage() && storedExperience > 0;
        boolean hovered = enabled && isInside(mouseX, mouseY, leftPos + x, topPos + 118, XP_BUTTON_WIDTH, 16);
        drawButtonFrame(graphics, x, 118, XP_BUTTON_WIDTH, 16, hovered, !enabled);
        graphics.drawCenteredString(
                font,
                "XP " + storedExperience,
                x + XP_BUTTON_WIDTH / 2,
                122,
                enabled ? hovered ? POSITIVE : TEXT_PRIMARY : TEXT_DIM
        );
    }

    private void drawTabButton(GuiGraphics graphics, MateriaTableTab tab, int index, int mouseX, int mouseY) {
        int x = TAB_START_X + index * (TAB_WIDTH + TAB_GAP);
        boolean unlocked = menu.isTabUnlocked(tab);
        boolean active = unlocked && menu.activeTab() == tab;
        boolean hovered = isInside(mouseX, mouseY, leftPos + x, topPos + TAB_Y, TAB_WIDTH, TAB_HEIGHT);
        int border = active ? ACCENT_BORDER : PANEL_BORDER;
        int fill = active ? 0x8A341943 : unlocked && hovered ? 0xCC17151E : PANEL_FILL;
        graphics.fill(x, TAB_Y, x + TAB_WIDTH, TAB_Y + TAB_HEIGHT, border);
        graphics.fill(x + 1, TAB_Y + 1, x + TAB_WIDTH - 1, TAB_Y + TAB_HEIGHT - 1, fill);
        if (active) {
            graphics.fill(x + 1, TAB_Y, x + TAB_WIDTH - 1, TAB_Y + 2, ACCENT);
        }
        graphics.drawCenteredString(
                font,
                Component.translatable(tab.translationKey()),
                x + TAB_WIDTH / 2,
                TAB_Y + 6,
                !unlocked ? TEXT_DIM : active ? TEXT_PRIMARY : TEXT_SECONDARY
        );
        if (!unlocked) {
            drawLockIcon(graphics, x + TAB_WIDTH - 12, TAB_Y + 6, TEXT_DIM);
        }
    }

    private void drawPanel(GuiGraphics graphics, int x, int y, int width, int height) {
        graphics.fill(x, y, x + width, y + height, PANEL_BORDER);
        graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, PANEL_FILL);
    }

    private void drawFrame(GuiGraphics graphics, int x, int y, int width, int height) {
        graphics.fill(x, y, x + width, y + 2, OUTER_BORDER);
        graphics.fill(x, y + height - 2, x + width, y + height, OUTER_BORDER);
        graphics.fill(x, y, x + 2, y + height, OUTER_BORDER);
        graphics.fill(x + width - 2, y, x + width, y + height, OUTER_BORDER);
        drawCornerBevel(graphics, x + 2, y + 2, true, true);
        drawCornerBevel(graphics, x + width - 14, y + 2, false, true);
        drawCornerBevel(graphics, x + 2, y + height - 14, true, false);
        drawCornerBevel(graphics, x + width - 14, y + height - 14, false, false);
    }

    private void drawCornerBevel(GuiGraphics graphics, int x, int y, boolean left, boolean top) {
        if (left && top) {
            graphics.fill(x, y + 6, x + 6, y + 8, OUTER_BORDER);
            graphics.fill(x + 6, y, x + 8, y + 6, OUTER_BORDER);
        } else if (!left && top) {
            graphics.fill(x + 6, y + 6, x + 12, y + 8, OUTER_BORDER);
            graphics.fill(x + 4, y, x + 6, y + 6, OUTER_BORDER);
        } else if (left) {
            graphics.fill(x, y + 4, x + 6, y + 6, OUTER_BORDER);
            graphics.fill(x + 6, y + 6, x + 8, y + 12, OUTER_BORDER);
        } else {
            graphics.fill(x + 6, y + 4, x + 12, y + 6, OUTER_BORDER);
            graphics.fill(x + 4, y + 6, x + 6, y + 12, OUTER_BORDER);
        }
    }

    private void drawButtonFrame(
            GuiGraphics graphics,
            int x,
            int y,
            int width,
            int height,
            boolean hovered,
            boolean disabled
    ) {
        int border = disabled ? 0xFF2B2A31 : hovered ? ACCENT_BORDER : PANEL_BORDER;
        int fill = disabled ? DISABLED_FILL : hovered ? 0xCC1D1625 : 0xCC15131B;
        graphics.fill(x, y, x + width, y + height, border);
        graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, fill);
    }

    private void drawActionButton(
            GuiGraphics graphics,
            int x,
            int y,
            int width,
            int height,
            Component label,
            boolean highlight,
            boolean disabled
    ) {
        drawActionButton(graphics, x, y, width, height, label, highlight, disabled, false);
    }

    private void drawActionButton(
            GuiGraphics graphics,
            int x,
            int y,
            int width,
            int height,
            Component label,
            boolean highlight,
            boolean disabled,
            boolean danger
    ) {
        int border = danger ? DANGER : disabled ? PANEL_BORDER : highlight ? ACCENT_BORDER : PANEL_BORDER;
        int fill = danger ? 0x66261218 : disabled ? DISABLED_FILL : highlight ? 0xA2361944 : 0xCC17151E;
        String textStr = label.getString();
        boolean isSell = textStr.equals(Component.translatable("screen.materia_reborn.materia_table.sell").getString());
        if (isSell && highlight && !disabled) {
            fill = 0xAA17331B;
            border = 0xFF2A6830;
        }
        graphics.fill(x, y, x + width, y + height, border);
        graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, fill);

        int color = danger ? DANGER : disabled ? TEXT_DIM : isSell && highlight ? 0xFF52E46D : TEXT_PRIMARY;
        int textWidth = font.width(label);
        int iconWidth = isSell ? 8 : 7;
        int space = 4;
        int totalWidth = textWidth;
        boolean hasIcon = false;
        int iconType = 0; // 1: Search, 2: Lock, 3: Essence

        if (textStr.equals(Component.translatable("screen.materia_reborn.materia_table.analyze").getString())) {
            hasIcon = true;
            iconType = 1;
        } else if (textStr.equals(Component.translatable("screen.materia_reborn.materia_table.unlock").getString())) {
            hasIcon = true;
            iconType = 2;
        } else if (isSell) {
            hasIcon = true;
            iconType = 3;
        }

        if (hasIcon) {
            totalWidth += iconWidth + space;
        }

        int startX = x + (width - totalWidth) / 2;
        int textY = y + (height - 8) / 2;

        if (hasIcon) {
            int iconY = y + (height - 7) / 2;
            if (iconType == 1) {
                drawSearchIcon(graphics, startX, iconY, color);
            } else if (iconType == 2) {
                drawLockIcon(graphics, startX, iconY, color);
            } else if (iconType == 3) {
                drawEssenceMiniIcon(graphics, startX, iconY - 1);
            }
            graphics.drawString(font, label, startX + iconWidth + space, textY, color, false);
        } else {
            graphics.drawCenteredString(font, label, x + width / 2, textY, color);
        }
    }

    private void drawUpgradeButton(GuiGraphics graphics, int x, int y, int width, int height, boolean hovered, boolean bulkUpgrade) {
        int upgradeCount = bulkUpgrade ? 10 : 1;
        boolean canAfford = menu.canAffordNextUpgrade(upgradeCount);
        int border = hovered && canAfford ? 0xFF2A6830 : hovered ? DANGER : PANEL_BORDER;
        int fill = hovered && canAfford ? 0xAA17331B : hovered ? 0x66261218 : DISABLED_FILL;
        int color = hovered && canAfford ? POSITIVE : hovered ? DANGER : TEXT_DIM;
        Component label = bulkUpgrade
                ? Component.literal(Component.translatable("screen.materia_reborn.materia_table.upgrade").getString() + " x10")
                : Component.translatable("screen.materia_reborn.materia_table.upgrade");

        graphics.fill(x, y, x + width, y + height, border);
        graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, fill);
        graphics.drawCenteredString(font, label, x + width / 2, y + (height - 8) / 2, color);
    }

    private void drawSearchBar(GuiGraphics graphics, int x, int y, int width, Component label) {
        drawSearchBar(graphics, x, y, width, label, "", false);
    }

    private void drawSearchBar(GuiGraphics graphics, int x, int y, int width, Component label, String value, boolean focused) {
        graphics.fill(x, y, x + width, y + 22, PANEL_BORDER);
        graphics.fill(x + 1, y + 1, x + width - 1, y + 21, focused ? 0xFF17111D : SLOT_FILL);
        String text = value.isEmpty() ? label.getString() : value;
        graphics.drawString(font, text + (focused && (System.currentTimeMillis() / 500L) % 2L == 0L ? "_" : ""), x + 8, y + 7, value.isEmpty() ? TEXT_DIM : TEXT_PRIMARY, false);
        drawSearchIcon(graphics, x + width - 15, y + 7, TEXT_SECONDARY);
    }

    private void drawFilterButton(GuiGraphics graphics, int x, int y, int width, Component label) {
        graphics.fill(x, y, x + width, y + 22, PANEL_BORDER);
        graphics.fill(x + 1, y + 1, x + width - 1, y + 21, PANEL_FILL);
        graphics.drawCenteredString(font, label, x + width / 2, y + 7, TEXT_SECONDARY);
    }

    private void drawDropdown(GuiGraphics graphics, int x, int y, int width, Component label) {
        graphics.fill(x, y, x + width, y + 22, PANEL_BORDER);
        graphics.fill(x + 1, y + 1, x + width - 1, y + 21, PANEL_FILL);
        graphics.drawString(font, label, x + 7, y + 7, TEXT_SECONDARY, false);
    }

    private void drawProgressBar(
            GuiGraphics graphics,
            int x,
            int y,
            int width,
            int height,
            int value,
            int max,
            int color
    ) {
        graphics.fill(x, y, x + width, y + height, SLOT_BORDER);
        graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, SLOT_FILL);
        if (max > 0 && value > 0) {
            int fillWidth = Math.max(1, (width - 2) * value / max);
            graphics.fill(x + 1, y + 1, x + 1 + fillWidth, y + height - 1, color);
        }
    }

    private void drawInventoryCapacity(GuiGraphics graphics, int x, int y, int width, int itemCount, int capacity) {
        double percent = capacity <= 0 ? 0.0D : (itemCount * 100.0D) / capacity;
        String label = Component.translatable("screen.materia_reborn.materia_table.capacity").getString()
                + ": "
                + String.format(java.util.Locale.US, "%.2f%%", percent);
        graphics.drawCenteredString(font, label, x + width / 2, y, TEXT_SECONDARY);
        drawProgressBar(graphics, x, y + 14, width, 8, Math.min(itemCount, capacity), capacity, ACCENT);
    }

    private void drawFurnaceInventoryPlaceholders(GuiGraphics graphics, int x, int y) {
        for (int slot = 0; slot < 18; slot++) {
            int drawX = x + (slot % 6) * 18;
            int drawY = y + (slot / 6) * 18;
            drawSlotBackground(graphics, drawX, drawY);
            if (slot >= menu.unlockedFurnaceSlotsPerSide()) {
                drawLockIcon(graphics, drawX + 4, drawY + 4, TEXT_DIM);
            }
        }
    }

    private void drawBackpackGridPlaceholders(GuiGraphics graphics) {
        int pageStart = menu.getBackpackPage() * 78;
        for (int slot = 0; slot < 78; slot++) {
            int drawX = 148 + (slot % 13) * 20;
            int drawY = 112 + (slot / 13) * 20;
            int absoluteSlot = pageStart + slot;
            drawSlotBackground(graphics, drawX, drawY);
            if (absoluteSlot >= menu.unlockedStorageSlots()) {
                drawLockIcon(graphics, drawX + 4, drawY + 4, TEXT_DIM);
            }
        }
    }

    private void drawUpgradeCost(GuiGraphics graphics, int x, int y, boolean hovered, boolean bulkUpgrade) {
        int upgradeCount = bulkUpgrade ? 10 : 1;
        int costValue = menu.nextUpgradeCost(upgradeCount);
        String cost = costValue <= 0 ? "-" : String.valueOf(costValue);
        int color = !hovered ? TEXT_SECONDARY : menu.canAffordNextUpgrade(upgradeCount) ? POSITIVE : DANGER;
        drawEssenceAmount(graphics, x, y, cost, color);
    }

    private void drawEssenceAmount(GuiGraphics graphics, int x, int y, String amount, int color) {
        graphics.drawString(font, amount, x, y, color, false);
        drawEssenceMiniIcon(graphics, x + font.width(amount) + 3, y);
    }

    private void drawEssenceMiniIcon(GuiGraphics graphics, int x, int y) {
        graphics.blit(ESSENCE_ICON, x, y, 8, 8, 0.0F, 0.0F, 32, 32, 32, 32);
    }

    private void drawFurnaceArrow(GuiGraphics graphics, int x, int y) {
        graphics.fill(x, y + 6, x + 20, y + 8, TEXT_SECONDARY);
        graphics.fill(x + 18, y + 2, x + 20, y + 12, TEXT_SECONDARY);
        graphics.fill(x + 20, y + 4, x + 26, y + 10, TEXT_SECONDARY);
    }

    private void drawFlameIndicator(GuiGraphics graphics, int x, int y, int litHeight, int color) {
        graphics.fill(x - 1, y - 1, x + 13, y + 21, SLOT_BORDER);
        graphics.fill(x, y, x + 12, y + 20, 0xFF0B0A10);
        if (litHeight > 0) {
            int height = Math.min(18, litHeight);
            int fillTop = y + 19 - height;
            graphics.fill(x + 2, fillTop, x + 10, y + 19, color);
            graphics.fill(x + 3, fillTop, x + 9, y + 19, lighten(color));
        }
    }

    private static int lighten(int color) {
        int alpha = color & 0xFF000000;
        int red = Math.min(255, ((color >> 16) & 0xFF) + 28);
        int green = Math.min(255, ((color >> 8) & 0xFF) + 28);
        int blue = Math.min(255, (color & 0xFF) + 28);
        return alpha | (red << 16) | (green << 8) | blue;
    }

    private void drawCraftingPlaceholder(GuiGraphics graphics, int x, int y) {
        int arrowX = x + 58;
        int arrowY = y + 23;
        graphics.fill(arrowX, arrowY + 3, arrowX + 12, arrowY + 5, TEXT_SECONDARY);
        graphics.fill(arrowX + 10, arrowY + 1, arrowX + 12, arrowY + 7, TEXT_SECONDARY);
        graphics.fill(arrowX + 12, arrowY + 2, arrowX + 14, arrowY + 6, TEXT_SECONDARY);
        graphics.fill(arrowX + 14, arrowY + 3, arrowX + 16, arrowY + 5, TEXT_SECONDARY);
    }

    private void renderPreviewGrid(GuiGraphics graphics, int x, int y, int columns, int rows, List<ItemStack> items) {
        renderPreviewGrid(graphics, x, y, columns, rows, 36, 36, items);
    }

    private void renderPreviewGrid(GuiGraphics graphics, int x, int y, int columns, int rows, int colSpacing, int rowSpacing, List<ItemStack> items) {
        int totalSlots = columns * rows;
        for (int index = 0; index < totalSlots; index++) {
            int drawX = x + (index % columns) * colSpacing;
            int drawY = y + (index / columns) * rowSpacing;
            drawSlotBackground(graphics, drawX, drawY);
            if (index < items.size()) {
                graphics.renderFakeItem(items.get(index), drawX, drawY);
                graphics.renderItemDecorations(font, items.get(index), drawX, drawY);
            } else {
                drawLockIcon(graphics, drawX + 5, drawY + 4, 0xFF352F3C);
            }
        }
    }

    private void drawSidebarDetailLine(
            GuiGraphics graphics,
            int x,
            int y,
            String key,
            String value,
            int valueColor
    ) {
        graphics.drawString(font, Component.translatable(key), x, y, TEXT_SECONDARY, false);
        graphics.drawString(font, value, x + 72, y, valueColor, false);
    }

    private void drawPageFooter(GuiGraphics graphics, int mouseX, int mouseY, int x, int y, String label) {
        graphics.drawString(font, label, x, y, TEXT_SECONDARY, false);
        boolean prevDisabled = menu.getBackpackPage() == 0;
        boolean prevHovered = isInside(mouseX, mouseY, leftPos + x + 56, topPos + y - 6, 20, 20);
        drawButtonFrame(graphics, x + 56, y - 6, 20, 20, prevHovered && !prevDisabled, prevDisabled);

        boolean nextDisabled = menu.getBackpackPage() >= menu.maxBackpackPage();
        boolean nextHovered = isInside(mouseX, mouseY, leftPos + x + 80, topPos + y - 6, 20, 20);
        drawButtonFrame(graphics, x + 80, y - 6, 20, 20, nextHovered && !nextDisabled, nextDisabled);

        graphics.drawCenteredString(font, "<", x + 66, y + 1, prevDisabled ? TEXT_DIM : TEXT_PRIMARY);
        graphics.drawCenteredString(font, ">", x + 90, y + 1, nextDisabled ? TEXT_DIM : TEXT_PRIMARY);
    }

    private void drawDiamondGlyph(GuiGraphics graphics, int x, int y, int color) {
        graphics.fill(x + 3, y, x + 5, y + 2, color);
        graphics.fill(x + 1, y + 2, x + 7, y + 4, color);
        graphics.fill(x + 3, y + 4, x + 5, y + 6, color);
    }

    private static void drawSlotBackground(GuiGraphics graphics, int x, int y) {
        graphics.fill(x - 1, y - 1, x + 17, y + 17, SLOT_BORDER);
        graphics.fill(x, y, x + 16, y + 16, SLOT_FILL);
    }

    private void drawLockedSlot(GuiGraphics graphics, int x, int y) {
        drawSlotBackground(graphics, x, y);
        drawLockIcon(graphics, x + 4, y + 4, TEXT_DIM);
    }

    private void drawGlowingLockedSlot(GuiGraphics graphics, int x, int y) {
        graphics.fill(x - 2, y - 2, x + 18, y + 18, 0x55361944);
        graphics.fill(x - 1, y - 1, x + 17, y + 17, ACCENT_BORDER);
        graphics.fill(x, y, x + 16, y + 16, 0xFF15101B);
        drawLockIcon(graphics, x + 4, y + 4, ACCENT);
    }

    private void sendMenuButton(int buttonId) {
        if (minecraft != null && minecraft.player != null) {
            menu.clickMenuButton(minecraft.player, buttonId);
            if (minecraft.gameMode != null) {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, buttonId);
            }
        }
    }

    private boolean isInsideTab(double mouseX, double mouseY, MateriaTableTab tab) {
        int index = tab.id();
        int x = TAB_START_X + index * (TAB_WIDTH + TAB_GAP);
        return isInside(mouseX, mouseY, leftPos + x, topPos + TAB_Y, TAB_WIDTH, TAB_HEIGHT);
    }

    private static boolean isInside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    private static ItemStack stack(ItemLike item, int count) {
        ItemStack stack = new ItemStack(item);
        stack.setCount(count);
        return stack;
    }
}
