package com.materiareborn.client.screen.config;

import com.materiareborn.config.EssenceItemConfigFiles;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;

public final class MateriaConfigHomeScreen extends Screen {
    private final ModContainer modContainer;
    private final Screen parent;

    public MateriaConfigHomeScreen(ModContainer modContainer, Screen parent) {
        super(Component.literal("Materia Reborn Configuration"));
        this.modContainer = modContainer;
        this.parent = parent;
    }

    @Override
    protected void init() {
        int buttonWidth = 260;
        int x = (width - buttonWidth) / 2;
        int y = height / 2 - 58;
        addRenderableWidget(Button.builder(
                Component.literal("Gameplay, Slots & Upgrades"),
                button -> minecraft.setScreen(new ConfigurationScreen(modContainer, this))
        ).bounds(x, y, buttonWidth, 20).build());
        addRenderableWidget(Button.builder(
                Component.literal("Essence Item Values"),
                button -> minecraft.setScreen(new EssenceItemConfigScreen(this))
        ).bounds(x, y + 24, buttonWidth, 20).build());
        addRenderableWidget(Button.builder(
                Component.literal("Open Config Folder"),
                button -> Util.getPlatform().openFile(EssenceItemConfigFiles.configDirectory().toFile())
        ).bounds(x, y + 48, buttonWidth, 20).build());
        addRenderableWidget(Button.builder(
                Component.translatable("gui.done"),
                button -> onClose()
        ).bounds(x, y + 84, buttonWidth, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(font, title, width / 2, height / 2 - 94, 0xFFFFFF);
        graphics.drawCenteredString(
                font,
                Component.literal("Files: config/materia_reborn"),
                width / 2,
                height / 2 + 52,
                0xA0A0A0
        );
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }
}
