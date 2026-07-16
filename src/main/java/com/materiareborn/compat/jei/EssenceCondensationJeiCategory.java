package com.materiareborn.compat.jei;

import com.materiareborn.registry.ModItems;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;

public final class EssenceCondensationJeiCategory implements IRecipeCategory<EssenceCondensationJeiRecipe> {
    private final IDrawable background;
    private final IDrawable icon;

    public EssenceCondensationJeiCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(138, 24);
        this.icon = guiHelper.createDrawableItemLike(ModItems.ESSENCE.get());
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
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, EssenceCondensationJeiRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 1, 4).addItemStack(recipe.dust());
        builder.addSlot(RecipeIngredientRole.INPUT, 23, 4).addItemStack(recipe.crystal());
        builder.addSlot(RecipeIngredientRole.INPUT, 45, 4).addItemStack(recipe.cauldron());
        builder.addSlot(RecipeIngredientRole.INPUT, 67, 4).addItemStack(recipe.buddingAmethyst());
        builder.addSlot(RecipeIngredientRole.INPUT, 89, 4).addItemStack(recipe.bottle());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 119, 4).addItemStack(recipe.essence());
    }
}