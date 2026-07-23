package com.materiareborn.compat.jei;

import com.materiareborn.registry.ModItems;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
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
import net.minecraft.world.item.ItemStack;

public final class EssenceCondensationJeiCategory implements IRecipeCategory<EssenceCondensationJeiRecipe> {
    private static final int WIDTH = 176;
    private static final int HEIGHT = 142;
    private static final int TEXT = 0xFF3F3F3F;
    private static final int MUTED_TEXT = 0xFF666666;
    private static final int ACCENT = 0xFF9D3DB8;
    private static final int DIVIDER = 0x559D3DB8;

    private final IDrawable icon;
    private final IDrawable arrow;

    public EssenceCondensationJeiCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemLike(ModItems.ESSENCE.get());
        this.arrow = guiHelper.getRecipeArrow();
    }

    @Override
    public RecipeType<EssenceCondensationJeiRecipe> getRecipeType() {
        return MateriaJeiPlugin.ESSENCE_CONDENSATION;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.materia_reborn.essence_condensation");
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
    public void setRecipe(IRecipeLayoutBuilder builder, EssenceCondensationJeiRecipe recipe, IFocusGroup focuses) {
        setTwoDimensionalRecipe(builder, recipe);
    }

    private static void setTwoDimensionalRecipe(IRecipeLayoutBuilder builder, EssenceCondensationJeiRecipe recipe) {
        ItemStack displayedDust = recipe.dust().copy();
        displayedDust.setCount(1);
        builder.addSlot(RecipeIngredientRole.INPUT, 10, 16).addItemStack(displayedDust);
        builder.addSlot(RecipeIngredientRole.INPUT, 46, 16).addItemStack(recipe.crystal());
        builder.addSlot(RecipeIngredientRole.INPUT, 28, 54)
                .addItemStack(recipe.cauldron())
                .addRichTooltipCallback((slot, tooltip) ->
                        tooltip.add(Component.translatable("jei.materia_reborn.full_water_cauldron.tooltip")));
        builder.addSlot(RecipeIngredientRole.INPUT, 28, 86)
                .addItemStack(recipe.buddingAmethyst())
                .addRichTooltipCallback((slot, tooltip) ->
                        tooltip.add(Component.translatable("jei.materia_reborn.budding_amethyst.tooltip")));
        builder.addSlot(RecipeIngredientRole.INPUT, 4, 116)
                .addItemStack(recipe.bottle())
                .addRichTooltipCallback((slot, tooltip) ->
                        tooltip.add(Component.translatable("jei.materia_reborn.glass_bottle.tooltip")));
        builder.addSlot(RecipeIngredientRole.OUTPUT, 149, 54).addItemStack(recipe.essence());
    }

    @Override
    public void draw(
            EssenceCondensationJeiRecipe recipe,
            IRecipeSlotsView recipeSlots,
            GuiGraphics graphics,
            double mouseX,
            double mouseY
    ) {
        drawTwoDimensionalRecipe(recipe, graphics);
    }

    @Override
    public void getTooltip(
            ITooltipBuilder tooltip,
            EssenceCondensationJeiRecipe recipe,
            IRecipeSlotsView recipeSlots,
            double mouseX,
            double mouseY
    ) {
        IRecipeCategory.super.getTooltip(tooltip, recipe, recipeSlots, mouseX, mouseY);
        if (mouseX < 62.0D || mouseX >= 111.0D || mouseY < 8.0D || mouseY >= 105.0D) {
            return;
        }
        tooltip.add(Component.literal("Y+2 = " + recipe.dust().getCount() + "x ")
                .append(recipe.dust().getHoverName())
                .append(" + " + recipe.crystal().getCount() + "x ")
                .append(recipe.crystal().getHoverName()));
        tooltip.add(Component.literal("Y+1 = ")
                .append(Component.translatable("jei.materia_reborn.full_water"))
                .append(" ")
                .append(Component.translatable("jei.materia_reborn.cauldron")));
        tooltip.add(Component.literal("Y+0 = ").append(recipe.buddingAmethyst().getHoverName()));
        tooltip.add(Component.translatable("jei.materia_reborn.right_click_full_cauldron"));
        tooltip.add(Component.translatable("jei.materia_reborn.with_glass_bottle"));
    }

    private void drawTwoDimensionalRecipe(EssenceCondensationJeiRecipe recipe, GuiGraphics graphics) {
        Font font = Minecraft.getInstance().font;
        graphics.drawString(font, Component.translatable("jei.materia_reborn.throw_into_cauldron"), 2, 3, TEXT, false);
        graphics.drawCenteredString(font, Component.literal(recipe.dust().getCount() + "x"), 19, 36, MUTED_TEXT);
        graphics.drawCenteredString(font, Component.literal(recipe.crystal().getCount() + "x"), 55, 36, MUTED_TEXT);

        drawDownArrow(graphics, 36, 43);
        graphics.drawString(font, Component.translatable("jei.materia_reborn.full_water"), 53, 53, TEXT, false);
        graphics.drawString(font, Component.translatable("jei.materia_reborn.cauldron"), 53, 63, TEXT, false);
        arrow.draw(graphics, 116, 54);
        graphics.drawCenteredString(font, Component.translatable("jei.materia_reborn.result"), 158, 43, TEXT);

        drawDownArrow(graphics, 36, 75);
        graphics.drawString(font, Component.translatable("jei.materia_reborn.budding_amethyst"), 53, 85, TEXT, false);
        graphics.drawString(font, Component.translatable("jei.materia_reborn.directly_below"), 53, 95, MUTED_TEXT, false);

        graphics.fill(0, 107, WIDTH, 108, DIVIDER);
        graphics.drawString(font, Component.translatable("jei.materia_reborn.right_click_full_cauldron"), 28, 110, TEXT, false);
        graphics.drawString(font, Component.translatable("jei.materia_reborn.with_glass_bottle"), 28, 120, TEXT, false);
        graphics.drawString(font, Component.translatable("jei.materia_reborn.wait_three_seconds"), 28, 130, ACCENT, false);
    }

    private static void drawDownArrow(GuiGraphics graphics, int x, int y) {
        graphics.fill(x, y, x + 2, y + 6, ACCENT);
        graphics.fill(x - 3, y + 4, x + 5, y + 6, ACCENT);
        graphics.fill(x - 1, y + 6, x + 3, y + 8, ACCENT);
    }
}