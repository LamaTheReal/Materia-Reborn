package com.materiareborn.directcraft;

import com.materiareborn.essence.EssenceItemCatalog;
import com.materiareborn.essence.EssenceItemDefinition;
import com.materiareborn.menu.MateriaTableMenu;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapelessRecipe;

public final class DirectCraftPlanner {
    private static final int GRID_SIZE = 9;

    private DirectCraftPlanner() {
    }

    public static DirectCraftPlan evaluate(
            MateriaTableMenu menu,
            Player player,
            RecipeHolder<CraftingRecipe> recipeHolder
    ) {
        CraftingRecipe recipe = recipeHolder.value();
        List<Ingredient> grid = createIngredientGrid(recipe);
        if (grid == null || !recipe.canCraftInDimensions(3, 3)) {
            return DirectCraftPlan.unsupportedRecipe();
        }

        List<ItemStack> finalGrid = new ArrayList<>(GRID_SIZE);
        List<DirectCraftPlan.Purchase> purchases = new ArrayList<>();
        long totalCost = 0L;

        for (int slot = 0; slot < GRID_SIZE; slot++) {
            Ingredient ingredient = grid.get(slot);
            ItemStack existing = menu.craftingInputItem(slot);
            if (ingredient.isEmpty()) {
                if (!existing.isEmpty()) {
                    return DirectCraftPlan.blockedSlot(slot);
                }
                finalGrid.add(ItemStack.EMPTY);
                continue;
            }

            if (!existing.isEmpty()) {
                if (!ingredient.test(existing)) {
                    return DirectCraftPlan.blockedSlot(slot);
                }
                DirectCraftPlan validation = validateExistingItem(menu, existing);
                if (validation != null) {
                    return validation;
                }
                finalGrid.add(existing.copy());
                continue;
            }

            IngredientSelection selection = selectIngredient(menu, player, ingredient);
            if (selection.failure() != null) {
                return selection.failure();
            }

            ItemStack purchasedStack = selection.stack().copyWithCount(1);
            purchases.add(new DirectCraftPlan.Purchase(slot, selection.catalogIndex(), purchasedStack));
            finalGrid.add(purchasedStack);
            totalCost = safeAdd(totalCost, selection.cost());
        }

        CraftingInput input = CraftingInput.of(3, 3, finalGrid);
        if (!recipe.matches(input, player.level())) {
            return DirectCraftPlan.unsupportedRecipe();
        }

        long availableEssence = menu.displayedEssenceValue();
        if (availableEssence < totalCost) {
            return DirectCraftPlan.notEnoughEssence(totalCost, totalCost - availableEssence);
        }
        return DirectCraftPlan.ready(totalCost, purchases);
    }

    private static List<Ingredient> createIngredientGrid(CraftingRecipe recipe) {
        List<Ingredient> grid = new ArrayList<>(GRID_SIZE);
        for (int slot = 0; slot < GRID_SIZE; slot++) {
            grid.add(Ingredient.EMPTY);
        }

        if (recipe instanceof ShapedRecipe shaped) {
            List<Ingredient> ingredients = shaped.getIngredients();
            int width = shaped.getWidth();
            int height = shaped.getHeight();
            if (width < 1 || height < 1 || width > 3 || height > 3 || ingredients.size() != width * height) {
                return null;
            }
            for (int row = 0; row < height; row++) {
                for (int column = 0; column < width; column++) {
                    grid.set(row * 3 + column, ingredients.get(row * width + column));
                }
            }
            return grid;
        }

        if (recipe instanceof ShapelessRecipe shapeless) {
            List<Ingredient> ingredients = shapeless.getIngredients().stream()
                    .filter(ingredient -> !ingredient.isEmpty())
                    .toList();
            if (ingredients.isEmpty() || ingredients.size() > GRID_SIZE) {
                return null;
            }
            for (int slot = 0; slot < ingredients.size(); slot++) {
                grid.set(slot, ingredients.get(slot));
            }
            return grid;
        }
        return null;
    }

    private static DirectCraftPlan validateExistingItem(MateriaTableMenu menu, ItemStack stack) {
        int catalogIndex = EssenceItemCatalog.indexOf(stack);
        String itemId = itemId(stack);
        if (catalogIndex < 0) {
            return DirectCraftPlan.itemUnavailable(itemId);
        }

        EssenceItemDefinition definition = EssenceItemCatalog.get(catalogIndex);
        if (definition.tableLevel() > menu.tableTier()) {
            return DirectCraftPlan.requiresTableLevel(itemId, definition.tableLevel());
        }
        if (!menu.isEssenceItemUnlocked(catalogIndex)) {
            return DirectCraftPlan.itemNotUnlocked(itemId);
        }
        return null;
    }

    private static IngredientSelection selectIngredient(
            MateriaTableMenu menu,
            Player player,
            Ingredient ingredient
    ) {
        List<Candidate> usable = new ArrayList<>();
        Candidate locked = null;
        Candidate higherTier = null;
        String fallbackItemId = firstIngredientItemId(ingredient);

        for (int catalogIndex = 0; catalogIndex < EssenceItemCatalog.size(); catalogIndex++) {
            EssenceItemDefinition definition = EssenceItemCatalog.get(catalogIndex);
            ItemStack candidateStack = definition.createStack(player.level().registryAccess());
            if (!ingredient.test(candidateStack)) {
                continue;
            }

            Candidate candidate = new Candidate(
                    catalogIndex,
                    candidateStack,
                    definition.purchaseCost(),
                    definition.tableLevel()
            );
            if (definition.tableLevel() > menu.tableTier()) {
                if (higherTier == null || definition.tableLevel() < higherTier.tableLevel()) {
                    higherTier = candidate;
                }
            } else if (!menu.isEssenceItemUnlocked(catalogIndex)) {
                if (locked == null) {
                    locked = candidate;
                }
            } else {
                usable.add(candidate);
            }
        }

        if (!usable.isEmpty()) {
            Candidate selected = usable.stream()
                    .min(Comparator.comparingLong(Candidate::cost).thenComparingInt(Candidate::catalogIndex))
                    .orElseThrow();
            return IngredientSelection.success(selected);
        }
        if (locked != null) {
            return IngredientSelection.failure(DirectCraftPlan.itemNotUnlocked(itemId(locked.stack())));
        }
        if (higherTier != null) {
            return IngredientSelection.failure(DirectCraftPlan.requiresTableLevel(
                    itemId(higherTier.stack()),
                    higherTier.tableLevel()
            ));
        }
        return IngredientSelection.failure(DirectCraftPlan.itemUnavailable(fallbackItemId));
    }

    private static String firstIngredientItemId(Ingredient ingredient) {
        ItemStack[] stacks = ingredient.getItems();
        return stacks.length == 0 ? "unknown" : itemId(stacks[0]);
    }

    private static String itemId(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
    }

    private static long safeAdd(long left, long right) {
        return Long.MAX_VALUE - left < right ? Long.MAX_VALUE : left + right;
    }

    private record Candidate(int catalogIndex, ItemStack stack, long cost, int tableLevel) {
    }

    private record IngredientSelection(
            int catalogIndex,
            ItemStack stack,
            long cost,
            DirectCraftPlan failure
    ) {
        private static IngredientSelection success(Candidate candidate) {
            return new IngredientSelection(
                    candidate.catalogIndex(),
                    candidate.stack(),
                    candidate.cost(),
                    null
            );
        }

        private static IngredientSelection failure(DirectCraftPlan failure) {
            return new IngredientSelection(-1, ItemStack.EMPTY, 0L, failure);
        }
    }
}
