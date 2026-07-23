package com.materiareborn.compat.jei;

import com.materiareborn.core.ModConstants;
import com.materiareborn.essence.EssenceCondensationRecipe;
import com.materiareborn.fluid.LiquidEssenceRecipe;
import com.materiareborn.registry.ModItems;
import java.util.List;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IAdvancedRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

@JeiPlugin
public final class MateriaJeiPlugin implements IModPlugin {
    public static final RecipeType<EssenceDustJeiRecipe> ESSENCE_DUST_CREATION = RecipeType.create(
            ModConstants.MOD_ID,
            "essence_dust_creation",
            EssenceDustJeiRecipe.class
    );
    public static final RecipeType<EssenceCondensationJeiRecipe> ESSENCE_CONDENSATION = RecipeType.create(
            ModConstants.MOD_ID,
            "essence_condensation",
            EssenceCondensationJeiRecipe.class
    );

    public static final RecipeType<LiquidEssenceJeiRecipe> LIQUID_ESSENCE_BREWING = RecipeType.create(
            ModConstants.MOD_ID,
            "liquid_essence_brewing",
            LiquidEssenceJeiRecipe.class
    );

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(ModConstants.MOD_ID, "jei");
    }

    @Override
    public void registerAdvanced(IAdvancedRegistration registration) {
        registration.addRecipeButtonFactory(
                new DirectCraftButtonFactory(registration.getJeiHelpers().getGuiHelper())
        );
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(
                new EssenceDustJeiCategory(registration.getJeiHelpers().getGuiHelper()),
                new EssenceCondensationJeiCategory(registration.getJeiHelpers().getGuiHelper()),
                new LiquidEssenceJeiCategory(registration.getJeiHelpers().getGuiHelper())
        );
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(ESSENCE_DUST_CREATION, List.of(new EssenceDustJeiRecipe(
                new ItemStack(ModItems.ESSENCE_CRYSTAL.get()),
                new ItemStack(Items.FLINT),
                new ItemStack(ModItems.ESSENCE_DUST.get())
        )));
        registration.addRecipes(ESSENCE_CONDENSATION, List.of(new EssenceCondensationJeiRecipe(
                new ItemStack(ModItems.ESSENCE_DUST.get(), EssenceCondensationRecipe.DUST_COUNT),
                new ItemStack(ModItems.ESSENCE_CRYSTAL.get()),
                new ItemStack(Items.CAULDRON),
                new ItemStack(Blocks.BUDDING_AMETHYST),
                new ItemStack(Items.GLASS_BOTTLE),
                new ItemStack(ModItems.ESSENCE.get(), EssenceCondensationRecipe.OUTPUT_COUNT)
        )));
        registration.addRecipes(LIQUID_ESSENCE_BREWING, List.of(new LiquidEssenceJeiRecipe(
                new ItemStack(ModItems.ESSENCE.get(), LiquidEssenceRecipe.ESSENCE_COUNT),
                new ItemStack(ModItems.ESSENCE_CRYSTAL.get(), LiquidEssenceRecipe.CRYSTAL_COUNT),
                new ItemStack(Items.WATER_BUCKET),
                new ItemStack(ModItems.LIQUID_ESSENCE.get())
        )));
    }
}
