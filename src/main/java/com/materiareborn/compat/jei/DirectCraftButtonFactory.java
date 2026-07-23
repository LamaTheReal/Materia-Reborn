package com.materiareborn.compat.jei;

import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.gui.buttons.IIconButtonController;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.advanced.IRecipeButtonControllerFactory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import com.materiareborn.registry.ModItems;

public final class DirectCraftButtonFactory implements IRecipeButtonControllerFactory {
    private final IDrawable icon;

    public DirectCraftButtonFactory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModItems.MATERIA_TABLE.get()));
    }

    @Override
    public <T> IIconButtonController createButtonController(IRecipeLayoutDrawable<T> recipeLayout) {
        if (!RecipeTypes.CRAFTING.equals(recipeLayout.getRecipeCategory().getRecipeType())) {
            return null;
        }
        if (!(recipeLayout.getRecipe() instanceof RecipeHolder<?> holder)
                || !(holder.value() instanceof CraftingRecipe recipe)) {
            return null;
        }
        return new DirectCraftButtonController(icon, new RecipeHolder<>(holder.id(), recipe));
    }
}
