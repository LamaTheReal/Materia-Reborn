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
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public final class EssenceDustJeiCategory implements IRecipeCategory<EssenceDustJeiRecipe> {
    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable arrow;

    public EssenceDustJeiCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(94, 24);
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
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, EssenceDustJeiRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 1, 4).addItemStack(recipe.crystal());
        builder.addSlot(RecipeIngredientRole.INPUT, 22, 4).addItemStack(recipe.flint());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 75, 4).addItemStack(recipe.dust());
    }

    @Override
    public void draw(EssenceDustJeiRecipe recipe, IRecipeSlotsView recipeSlots, GuiGraphics graphics, double mouseX, double mouseY) {
        arrow.draw(graphics, 47, 3);
    }
}