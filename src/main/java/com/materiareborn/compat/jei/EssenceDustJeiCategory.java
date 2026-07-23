package com.materiareborn.compat.jei;

import com.materiareborn.registry.ModItems;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public final class EssenceDustJeiCategory implements IRecipeCategory<EssenceDustJeiRecipe> {
    private static final int WIDTH = 176;
    private static final int HEIGHT = 112;
    private static final int TEXT = 0xFF3F3F3F;
    private static final int MUTED_TEXT = 0xFF666666;
    private static final int ACCENT = 0xFF9D3DB8;

    private final IDrawable icon;
    private final IDrawable arrow;

    public EssenceDustJeiCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemLike(ModItems.ESSENCE_DUST.get());
        this.arrow = guiHelper.getRecipeArrow();
    }

    @Override
    public RecipeType<EssenceDustJeiRecipe> getRecipeType() {
        return MateriaJeiPlugin.ESSENCE_DUST_CREATION;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.materia_reborn.essence_dust_creation");
    }

    @Override
    public int getWidth() {
        return WIDTH;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, EssenceDustJeiRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 25, 16).addItemStack(recipe.flint());
        builder.addSlot(RecipeIngredientRole.INPUT, 82, 16).addItemStack(recipe.crystal());
        builder.addSlot(RecipeIngredientRole.INPUT, 25, 42).addItemStack(recipe.crystal());
        builder.addSlot(RecipeIngredientRole.INPUT, 82, 42).addItemStack(recipe.flint());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 151, 29).addItemStack(recipe.dust());
    }

    @Override
    public void draw(EssenceDustJeiRecipe recipe, IRecipeSlotsView recipeSlots, GuiGraphics graphics, double mouseX, double mouseY) {
        Font font = Minecraft.getInstance().font;
        graphics.drawCenteredString(font, Component.translatable("jei.materia_reborn.main_hand"), 34, 3, TEXT);
        graphics.drawCenteredString(font, Component.translatable("jei.materia_reborn.off_hand"), 91, 3, TEXT);
        graphics.drawCenteredString(font, Component.translatable("jei.materia_reborn.result"), 160, 3, TEXT);

        graphics.drawString(font, Component.literal("1"), 14, 21, MUTED_TEXT, false);
        graphics.drawString(font, Component.literal("2"), 14, 47, MUTED_TEXT, false);
        drawSwapHands(graphics, 55, 21);
        drawSwapHands(graphics, 55, 47);
        arrow.draw(graphics, 119, 29);

        graphics.drawCenteredString(
                font,
                Component.translatable("jei.materia_reborn.either_hand_order"),
                WIDTH / 2,
                66,
                MUTED_TEXT
        );
        graphics.drawCenteredString(
                font,
                Component.translatable("jei.materia_reborn.right_click_air"),
                WIDTH / 2,
                78,
                ACCENT
        );
        graphics.drawCenteredString(
                font,
                Component.translatable("jei.materia_reborn.refining_duration"),
                WIDTH / 2,
                90,
                MUTED_TEXT
        );
        graphics.drawCenteredString(
                font,
                Component.translatable("jei.materia_reborn.refining_batch"),
                WIDTH / 2,
                100,
                MUTED_TEXT
        );
    }

    private static void drawSwapHands(GuiGraphics graphics, int x, int y) {
        graphics.fill(x, y + 1, x + 13, y + 2, ACCENT);
        graphics.fill(x + 10, y - 1, x + 12, y + 4, ACCENT);
        graphics.fill(x + 12, y, x + 14, y + 3, ACCENT);

        graphics.fill(x + 1, y + 6, x + 14, y + 7, ACCENT);
        graphics.fill(x + 2, y + 4, x + 4, y + 9, ACCENT);
        graphics.fill(x, y + 5, x + 2, y + 8, ACCENT);
    }
}
