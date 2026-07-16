package com.materiareborn.client.screen.config;

import com.materiareborn.config.ConfiguredEssenceItem;
import com.materiareborn.config.EssenceItemConfigFiles;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public final class EssenceItemConfigScreen extends Screen {
    private static final int ITEMS_PER_PAGE = 10;
    private static final int VALUE_FIELD_WIDTH = 104;

    private final Screen parent;
    private final List<ConfiguredEssenceItem> allItems;
    private final List<ConfiguredEssenceItem> filteredItems = new ArrayList<>();
    private final List<Button> itemButtons = new ArrayList<>();
    private EditBox searchBox;
    private EditBox analysisBox;
    private EditBox baseBox;
    private EditBox sellBox;
    private EditBox buyBox;
    private Button enabledButton;
    private Button saveButton;
    private Button previousButton;
    private Button nextButton;
    private int page;
    private ConfiguredEssenceItem selected;
    private String status = "";
    private int statusColor = 0xA0A0A0;

    public EssenceItemConfigScreen(Screen parent) {
        super(Component.literal("Essence Item Values"));
        this.parent = parent;
        this.allItems = new ArrayList<>(EssenceItemConfigFiles.loadEditableItems());
        this.filteredItems.addAll(allItems);
    }

    @Override
    protected void init() {
        int left = width / 2 - 250;
        int top = 38;

        searchBox = new EditBox(font, left, top, 300, 20, Component.literal("Search items"));
        searchBox.setHint(Component.literal("Search by item, category or tier..."));
        searchBox.setResponder(this::filterItems);
        addRenderableWidget(searchBox);

        for (int row = 0; row < ITEMS_PER_PAGE; row++) {
            int capturedRow = row;
            Button button = Button.builder(Component.empty(), ignored -> selectRow(capturedRow))
                    .bounds(left, top + 26 + row * 22, 300, 20)
                    .build();
            itemButtons.add(button);
            addRenderableWidget(button);
        }

        previousButton = addRenderableWidget(Button.builder(Component.literal("<"), ignored -> changePage(-1))
                .bounds(left, top + 250, 30, 20).build());
        nextButton = addRenderableWidget(Button.builder(Component.literal(">"), ignored -> changePage(1))
                .bounds(left + 270, top + 250, 30, 20).build());

        int editorX = left + 322;
        enabledButton = addRenderableWidget(Button.builder(Component.empty(), ignored -> toggleEnabled())
                .bounds(editorX, top + 36, 178, 20).build());
        analysisBox = addNumberBox(editorX, top + 82, "Analyze");
        baseBox = addNumberBox(editorX, top + 122, "Base");
        sellBox = addNumberBox(editorX, top + 162, "Sell");
        buyBox = addNumberBox(editorX, top + 202, "Buy");
        saveButton = addRenderableWidget(Button.builder(Component.literal("Save Item"), ignored -> saveSelected())
                .bounds(editorX, top + 242, 178, 20).build());

        addRenderableWidget(Button.builder(Component.translatable("gui.done"), ignored -> onClose())
                .bounds(width / 2 - 100, height - 28, 200, 20).build());
        refreshButtons();
        refreshEditor();
    }

    private EditBox addNumberBox(int x, int y, String label) {
        EditBox box = new EditBox(font, x, y, VALUE_FIELD_WIDTH, 20, Component.literal(label));
        box.setFilter(value -> value.isEmpty() || value.chars().allMatch(Character::isDigit));
        addRenderableWidget(box);
        return box;
    }

    private void filterItems(String query) {
        String needle = query.trim().toLowerCase(Locale.ROOT);
        filteredItems.clear();
        for (ConfiguredEssenceItem item : allItems) {
            if (needle.isEmpty()
                    || item.id().toLowerCase(Locale.ROOT).contains(needle)
                    || item.category().toLowerCase(Locale.ROOT).contains(needle)
                    || item.tier().toLowerCase(Locale.ROOT).contains(needle)
                    || Integer.toString(item.tableLevel()).equals(needle)) {
                filteredItems.add(item);
            }
        }
        page = 0;
        selected = null;
        status = "";
        refreshButtons();
        refreshEditor();
    }

    private void selectRow(int row) {
        int index = page * ITEMS_PER_PAGE + row;
        if (index >= 0 && index < filteredItems.size()) {
            selected = filteredItems.get(index);
            status = "";
            refreshEditor();
        }
    }

    private void changePage(int direction) {
        page = Math.max(0, Math.min(maxPage(), page + direction));
        selected = null;
        status = "";
        refreshButtons();
        refreshEditor();
    }

    private void toggleEnabled() {
        if (selected == null) {
            return;
        }
        ConfiguredEssenceItem previous = selected;
        selected = selected.withValues(
                !selected.enabled(),
                selected.analysis(),
                selected.baseValue(),
                selected.sellValue(),
                selected.purchaseCost()
        );
        replaceItem(previous, selected);
        refreshButtons();
        refreshEditor();
    }

    private void saveSelected() {
        if (selected == null) {
            return;
        }
        try {
            int analysis = parsePositiveInt(analysisBox.getValue());
            long base = parsePositiveLong(baseBox.getValue());
            long sell = parsePositiveLong(sellBox.getValue());
            long buy = parsePositiveLong(buyBox.getValue());
            ConfiguredEssenceItem changed = selected.withValues(selected.enabled(), analysis, base, sell, buy);
            EssenceItemConfigFiles.save(changed);
            replaceItem(selected, changed);
            selected = changed;
            status = "Saved. Restart Minecraft to apply item catalog changes.";
            statusColor = 0x55FF55;
            refreshButtons();
            refreshEditor();
        } catch (RuntimeException exception) {
            status = "Every numeric value must be a whole number greater than 0.";
            statusColor = 0xFF5555;
        }
    }

    private void replaceItem(ConfiguredEssenceItem oldItem, ConfiguredEssenceItem newItem) {
        int allIndex = allItems.indexOf(oldItem);
        if (allIndex >= 0) {
            allItems.set(allIndex, newItem);
        }
        int filteredIndex = filteredItems.indexOf(oldItem);
        if (filteredIndex >= 0) {
            filteredItems.set(filteredIndex, newItem);
        }
    }

    private static int parsePositiveInt(String value) {
        int parsed = Integer.parseInt(value);
        if (parsed <= 0) {
            throw new NumberFormatException();
        }
        return parsed;
    }

    private static long parsePositiveLong(String value) {
        long parsed = Long.parseLong(value);
        if (parsed <= 0L) {
            throw new NumberFormatException();
        }
        return parsed;
    }

    private void refreshButtons() {
        if (itemButtons.isEmpty()) {
            return;
        }
        int start = page * ITEMS_PER_PAGE;
        for (int row = 0; row < itemButtons.size(); row++) {
            int index = start + row;
            Button button = itemButtons.get(row);
            button.visible = index < filteredItems.size();
            button.active = button.visible;
            if (button.visible) {
                ConfiguredEssenceItem item = filteredItems.get(index);
                String state = item.enabled() ? "" : " [LOCKED]";
                button.setMessage(Component.literal("T" + item.tableLevel() + "  " + item.id() + state));
            }
        }
        previousButton.active = page > 0;
        nextButton.active = page < maxPage();
    }

    private void refreshEditor() {
        if (enabledButton == null) {
            return;
        }
        boolean available = selected != null;
        enabledButton.active = available;
        saveButton.active = available;
        analysisBox.setEditable(available);
        baseBox.setEditable(available);
        sellBox.setEditable(available);
        buyBox.setEditable(available);
        if (!available) {
            enabledButton.setMessage(Component.literal("Select an item"));
            analysisBox.setValue("");
            baseBox.setValue("");
            sellBox.setValue("");
            buyBox.setValue("");
            return;
        }
        enabledButton.setMessage(Component.literal(selected.enabled() ? "Enabled: ON" : "Enabled: OFF"));
        analysisBox.setValue(Integer.toString(selected.analysis()));
        baseBox.setValue(Long.toString(selected.baseValue()));
        sellBox.setValue(Long.toString(selected.sellValue()));
        buyBox.setValue(Long.toString(selected.purchaseCost()));
    }

    private int maxPage() {
        return Math.max(0, (filteredItems.size() - 1) / ITEMS_PER_PAGE);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        int left = width / 2 - 250;
        int top = 38;
        int editorX = left + 322;
        graphics.drawCenteredString(font, title, width / 2, 15, 0xFFFFFF);
        graphics.drawString(font, "Page " + (page + 1) + " / " + (maxPage() + 1), left + 112, top + 256, 0xA0A0A0, false);
        graphics.drawString(font, "Enabled / Item Lock", editorX, top + 24, 0xFFFFFF, false);
        drawValueLabel(graphics, editorX, top + 82, "Analyze");
        drawValueLabel(graphics, editorX, top + 122, "Default");
        drawValueLabel(graphics, editorX, top + 162, "Sell");
        drawValueLabel(graphics, editorX, top + 202, "Buy");
        if (selected != null) {
            graphics.drawString(font, selected.id(), editorX, top + 6, selected.enabled() ? 0x55FFFF : 0xFF5555, false);
            renderSelectedItem(graphics, selected, left + 482, top + 2);
        }
        if (!status.isEmpty()) {
            graphics.drawCenteredString(font, status, width / 2, height - 42, statusColor);
        }
    }

    private void drawValueLabel(GuiGraphics graphics, int editorX, int fieldY, String label) {
        graphics.drawString(font, label, editorX + VALUE_FIELD_WIDTH + 8, fieldY + 6, 0xFFFFFF, false);
    }

    private static void renderSelectedItem(GuiGraphics graphics, ConfiguredEssenceItem item, int x, int y) {
        String baseId = item.id();
        int componentStart = baseId.indexOf('[');
        if (componentStart >= 0) {
            baseId = baseId.substring(0, componentStart);
        }
        ResourceLocation location = ResourceLocation.tryParse(baseId);
        if (location != null && BuiltInRegistries.ITEM.containsKey(location)) {
            graphics.renderItem(new ItemStack(BuiltInRegistries.ITEM.get(location)), x, y);
        }
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }
}
