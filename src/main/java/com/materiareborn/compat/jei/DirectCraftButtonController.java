package com.materiareborn.compat.jei;

import com.materiareborn.directcraft.DirectCraftPlan;
import com.materiareborn.directcraft.DirectCraftPlanner;
import com.materiareborn.menu.MateriaTableMenu;
import com.materiareborn.network.DirectCraftPayload;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.buttons.IButtonState;
import mezz.jei.api.gui.buttons.IIconButtonController;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.inputs.IJeiUserInput;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.network.PacketDistributor;

public final class DirectCraftButtonController implements IIconButtonController {
    private final IDrawable icon;
    private final RecipeHolder<CraftingRecipe> recipe;

    public DirectCraftButtonController(IDrawable icon, RecipeHolder<CraftingRecipe> recipe) {
        this.icon = icon;
        this.recipe = recipe;
    }

    @Override
    public void initState(IButtonState state) {
        state.setIcon(icon);
        updateState(state);
    }

    @Override
    public void updateState(IButtonState state) {
        DirectCraftPlan plan = currentPlan();
        state.setVisible(plan != null);
        state.setActive(plan != null && plan.isReady());
    }

    @Override
    public boolean onPress(IJeiUserInput input) {
        DirectCraftPlan plan = currentPlan();
        if (plan == null || !plan.isReady()) {
            return false;
        }
        if (!input.isSimulate()) {
            PacketDistributor.sendToServer(new DirectCraftPayload(recipe.id()));
        }
        return true;
    }

    @Override
    public void getTooltips(ITooltipBuilder tooltip) {
        DirectCraftPlan plan = currentPlan();
        if (plan == null) {
            return;
        }

        tooltip.add(Component.translatable("jei.materia_reborn.direct_craft")
                .withStyle(ChatFormatting.WHITE));
        switch (plan.status()) {
            case READY -> tooltip.add(costLine(plan.totalCost()));
            case NOT_ENOUGH_ESSENCE -> {
                tooltip.add(costLine(plan.totalCost()));
                tooltip.add(Component.translatable("jei.materia_reborn.direct_craft.not_enough")
                        .withStyle(ChatFormatting.RED));
                tooltip.add(Component.translatable(
                        "jei.materia_reborn.direct_craft.missing",
                        MateriaTableMenu.formatEssenceValue(plan.missingEssence())
                ).withStyle(ChatFormatting.RED));
            }
            case ITEM_NOT_UNLOCKED -> {
                tooltip.add(Component.translatable("jei.materia_reborn.direct_craft.not_unlocked")
                        .withStyle(ChatFormatting.RED));
                tooltip.add(Component.literal(plan.itemId()).withStyle(ChatFormatting.RED));
            }
            case ITEM_UNAVAILABLE -> {
                tooltip.add(Component.translatable("jei.materia_reborn.direct_craft.unavailable")
                        .withStyle(ChatFormatting.RED));
                tooltip.add(Component.literal(plan.itemId()).withStyle(ChatFormatting.RED));
            }
            case REQUIRES_TABLE_LEVEL -> {
                tooltip.add(Component.translatable(
                        "jei.materia_reborn.direct_craft.table_level",
                        plan.requiredTableLevel()
                ).withStyle(ChatFormatting.RED));
                tooltip.add(Component.literal(plan.itemId()).withStyle(ChatFormatting.RED));
            }
            case CRAFTING_SLOT_BLOCKED -> tooltip.add(Component.translatable(
                    "jei.materia_reborn.direct_craft.slot_blocked",
                    plan.blockedSlot() + 1
            ).withStyle(ChatFormatting.RED));
            case UNSUPPORTED_RECIPE -> tooltip.add(Component.translatable(
                    "jei.materia_reborn.direct_craft.unsupported"
            ).withStyle(ChatFormatting.RED));
        }
    }

    private DirectCraftPlan currentPlan() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null
                || !(minecraft.player.containerMenu instanceof MateriaTableMenu menu)) {
            return null;
        }
        return DirectCraftPlanner.evaluate(menu, minecraft.player, recipe);
    }

    private static Component costLine(long cost) {
        return Component.translatable(
                "jei.materia_reborn.direct_craft.cost",
                MateriaTableMenu.formatEssenceValue(cost)
        ).withStyle(ChatFormatting.LIGHT_PURPLE);
    }
}
