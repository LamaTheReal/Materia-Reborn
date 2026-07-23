package com.materiareborn.compat.jei;

import com.materiareborn.registry.ModItems;
import java.util.List;
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
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemStack;

public final class LiquidEssenceJeiCategory implements IRecipeCategory<LiquidEssenceJeiRecipe> {
    private static final int WIDTH = 176;
    private static final int HEIGHT = 142;
    private static final int TEXT = 0xFF3F3F3F;
    private static final int MUTED_TEXT = 0xFF666666;
    private static final int ACCENT = 0xFF9D3DB8;
    private static final int DIVIDER = 0x559D3DB8;

    private final IDrawable icon;
    private final IDrawable arrow;

    public LiquidEssenceJeiCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemLike(ModItems.LIQUID_ESSENCE.get());
        this.arrow = guiHelper.getRecipeArrow();
    }

    @Override
    public RecipeType<LiquidEssenceJeiRecipe> getRecipeType() {
        return MateriaJeiPlugin.LIQUID_ESSENCE_BREWING;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.materia_reborn.liquid_essence_brewing");
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
    public void setRecipe(IRecipeLayoutBuilder builder, LiquidEssenceJeiRecipe recipe, IFocusGroup focuses) {
        ItemStack displayedEssence = recipe.essence().copy();
        displayedEssence.setCount(1);
        ItemStack displayedCrystal = recipe.crystal().copy();
        displayedCrystal.setCount(1);

        builder.addSlot(RecipeIngredientRole.INPUT, 10, 16).addItemStack(displayedEssence);
        builder.addSlot(RecipeIngredientRole.INPUT, 46, 16).addItemStack(displayedCrystal);
        builder.addSlot(RecipeIngredientRole.CATALYST, 28, 54)
                .addItemStack(recipe.waterSource())
                .addRichTooltipCallback((slot, tooltip) ->
                        tooltip.add(Component.translatable("jei.materia_reborn.water_source.tooltip")));
        builder.addSlot(RecipeIngredientRole.OUTPUT, 149, 54).addItemStack(recipe.liquidEssence());
    }

    @Override
    public void draw(
            LiquidEssenceJeiRecipe recipe,
            IRecipeSlotsView recipeSlots,
            GuiGraphics graphics,
            double mouseX,
            double mouseY
    ) {
        Font font = Minecraft.getInstance().font;
        graphics.drawString(
                font,
                Component.translatable("jei.materia_reborn.throw_into_water"),
                2,
                3,
                TEXT,
                false
        );
        graphics.drawCenteredString(font, Component.literal(recipe.essence().getCount() + "x"), 19, 36, MUTED_TEXT);
        graphics.drawCenteredString(font, Component.literal(recipe.crystal().getCount() + "x"), 55, 36, MUTED_TEXT);

        drawDownArrow(graphics, 36, 43);
        graphics.drawString(
                font,
                Component.translatable("jei.materia_reborn.water_source"),
                53,
                58,
                TEXT,
                false
        );
        arrow.draw(graphics, 116, 54);
        graphics.drawCenteredString(font, Component.translatable("jei.materia_reborn.result"), 158, 43, TEXT);

        graphics.fill(0, 80, WIDTH, 81, DIVIDER);
        int textY = drawCenteredWrapped(
                graphics,
                font,
                Component.translatable("jei.materia_reborn.liquid_brewing_duration"),
                85,
                ACCENT
        );
        textY = drawCenteredWrapped(
                graphics,
                font,
                Component.translatable("jei.materia_reborn.liquid_lifetime"),
                textY,
                TEXT
        );
        textY = drawCenteredWrapped(
                graphics,
                font,
                Component.translatable("jei.materia_reborn.liquid_stabilized_lifetime"),
                textY,
                MUTED_TEXT
        );
        drawCenteredWrapped(
                graphics,
                font,
                Component.translatable("jei.materia_reborn.liquid_exposure"),
                textY,
                ACCENT
        );
    }

    private static int drawCenteredWrapped(
            GuiGraphics graphics,
            Font font,
            Component text,
            int y,
            int color
    ) {
        List<FormattedCharSequence> lines = font.split(text, WIDTH - 8);
        for (FormattedCharSequence line : lines) {
            graphics.drawString(font, line, (WIDTH - font.width(line)) / 2, y, color, false);
            y += 10;
        }
        return y;
    }

    private static void drawDownArrow(GuiGraphics graphics, int x, int y) {
        graphics.fill(x, y, x + 2, y + 6, ACCENT);
        graphics.fill(x - 3, y + 4, x + 5, y + 6, ACCENT);
        graphics.fill(x - 1, y + 6, x + 3, y + 8, ACCENT);
    }
}
