package com.materiareborn.ritual;

import com.materiareborn.blockentity.MateriaTableBlockEntity;
import com.materiareborn.fluid.LiquidEssenceRecipe;
import com.materiareborn.registry.ModFluids;
import com.materiareborn.registry.ModItems;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class MateriaTableRitualBuilder {
    private MateriaTableRitualBuilder() {
    }

    public static boolean preview(ServerPlayer player, MateriaTableBlockEntity table) {
        Optional<MateriaTableUpgrade> upgradeResult = availableUpgrade(player, table);
        if (upgradeResult.isEmpty()) {
            return false;
        }

        ServerLevel level = player.serverLevel();
        MateriaTableUpgrade upgrade = upgradeResult.get();
        BuildPlan plan = createPlan(level, player, table.getBlockPos(), upgrade);
        Map<Item, Integer> missing = player.isCreative()
                ? Map.of()
                : findMissingItems(player.getInventory(), plan.requirements());
        if (!plan.obstructions().isEmpty() || !missing.isEmpty()) {
            reportValidationFailure(player, plan.obstructions(), missing);
            return false;
        }

        if (plan.placements().isEmpty() && plan.liquidsToBrew().isEmpty()) {
            player.displayClientMessage(
                    Component.translatable("message.materia_reborn.ritual_builder.already_complete"),
                    true
            );
            return true;
        }

        player.displayClientMessage(
                Component.translatable(
                        "message.materia_reborn.ritual_builder.preview_ready",
                        upgrade.targetTier(),
                        new ItemStack(upgrade.core()).getHoverName(),
                        upgrade.essenceCost()
                ),
                true
        );
        return true;
    }

    public static boolean tryBuild(ServerPlayer player, MateriaTableBlockEntity table) {
        Optional<MateriaTableUpgrade> upgradeResult = availableUpgrade(player, table);
        if (upgradeResult.isEmpty()) {
            return false;
        }

        ServerLevel level = player.serverLevel();
        BlockPos tablePos = table.getBlockPos();
        MateriaTableUpgrade upgrade = upgradeResult.get();
        BuildPlan plan = createPlan(level, player, tablePos, upgrade);
        boolean consumesMaterials = !player.isCreative();
        Map<Item, Integer> missing = consumesMaterials
                ? findMissingItems(player.getInventory(), plan.requirements())
                : Map.of();

        if (!plan.obstructions().isEmpty() || !missing.isEmpty()) {
            reportValidationFailure(player, plan.obstructions(), missing);
            return false;
        }
        if (plan.placements().isEmpty() && plan.liquidsToBrew().isEmpty()) {
            player.displayClientMessage(
                    Component.translatable("message.materia_reborn.ritual_builder.already_complete"),
                    true
            );
            return true;
        }

        if (consumesMaterials && !consumeRequirements(player.getInventory(), plan.requirements())) {
            player.displayClientMessage(
                    Component.translatable("message.materia_reborn.ritual_builder.inventory_changed"),
                    true
            );
            return false;
        }

        List<Placement> completed = new ArrayList<>();
        for (Placement placement : plan.placements()) {
            if (!level.setBlock(placement.pos(), placement.target(), Block.UPDATE_ALL)) {
                rollback(level, completed);
                if (consumesMaterials) {
                    refundRequirements(player, plan.requirements());
                }
                player.displayClientMessage(
                        Component.translatable("message.materia_reborn.ritual_builder.failed"),
                        true
                );
                return false;
            }
            completed.add(placement);
        }

        if (!plan.liquidsToBrew().isEmpty()
                && !MateriaTableRitualBuildProcess.start(
                        level,
                        player.getUUID(),
                        tablePos,
                        upgrade,
                        plan.liquidsToBrew()
                )) {
            rollback(level, completed);
            if (consumesMaterials) {
                refundRequirements(player, plan.requirements());
            }
            player.displayClientMessage(
                    Component.translatable("message.materia_reborn.ritual_builder.preparing"),
                    true
            );
            return false;
        }

        if (consumesMaterials) {
            giveItem(player, Items.BUCKET, plan.waterBucketsConsumed());
        }
        player.getInventory().setChanged();
        player.containerMenu.broadcastChanges();
        level.playSound(
                null,
                tablePos,
                SoundEvents.AMETHYST_BLOCK_CHIME,
                SoundSource.BLOCKS,
                0.8F,
                1.2F
        );

        if (plan.liquidsToBrew().isEmpty()) {
            player.displayClientMessage(
                    Component.translatable("message.materia_reborn.ritual_builder.success"),
                    true
            );
        } else {
            player.displayClientMessage(
                    Component.translatable(
                            "message.materia_reborn.ritual_builder.brewing",
                            LiquidEssenceRecipe.BREW_TICKS / 20
                    ),
                    true
            );
        }
        return true;
    }

    private static Optional<MateriaTableUpgrade> availableUpgrade(
            ServerPlayer player,
            MateriaTableBlockEntity table
    ) {
        Optional<MateriaTableUpgrade> upgradeResult = MateriaTableUpgrade.forSource(
                table.getBlockState().getBlock()
        );
        if (upgradeResult.isEmpty()) {
            player.displayClientMessage(
                    Component.translatable("message.materia_reborn.ritual_builder.max_tier"),
                    true
            );
            return Optional.empty();
        }
        if (table.isUpgradeRitualActive()) {
            player.displayClientMessage(
                    Component.translatable("message.materia_reborn.ritual_builder.active"),
                    true
            );
            return Optional.empty();
        }
        if (MateriaTableRitualBuildProcess.isActive(player.serverLevel(), table.getBlockPos())) {
            player.displayClientMessage(
                    Component.translatable("message.materia_reborn.ritual_builder.preparing"),
                    true
            );
            return Optional.empty();
        }
        return upgradeResult;
    }

    private static BuildPlan createPlan(
            ServerLevel level,
            ServerPlayer player,
            BlockPos tablePos,
            MateriaTableUpgrade upgrade
    ) {
        List<Placement> placements = new ArrayList<>();
        List<Obstruction> obstructions = new ArrayList<>();
        List<MateriaTableRitualBuildProcess.LiquidTarget> liquidsToBrew = new ArrayList<>();
        Map<Item, Integer> requirements = new LinkedHashMap<>();

        for (int index = 0; index < upgrade.blockCount(); index++) {
            BlockPos pos = upgrade.ritualBlockPos(tablePos, index);
            BlockState current = level.getBlockState(pos);
            Block expected = upgrade.expectedBlock(index);
            if (current.is(expected)) {
                continue;
            }

            addRequirement(requirements, expected.asItem(), 1);
            if (!level.mayInteract(player, pos)) {
                obstructions.add(new Obstruction(pos, current, true));
            } else if (!current.isAir()) {
                obstructions.add(new Obstruction(pos, current, false));
            } else {
                placements.add(new Placement(pos.immutable(), current, expected.defaultBlockState()));
            }
        }

        int waterBuckets = 0;
        for (int index = 0; index < upgrade.blockCount(); index++) {
            BlockPos pos = upgrade.ritualEssencePos(tablePos, index);
            BlockState current = level.getBlockState(pos);
            if (level.getFluidState(pos).is(ModFluids.LIQUID_ESSENCE.get())
                    && level.getFluidState(pos).isSource()) {
                continue;
            }

            boolean waterSource = current.is(Blocks.WATER) && level.getFluidState(pos).isSource();
            addRequirement(requirements, ModItems.ESSENCE.get(), LiquidEssenceRecipe.ESSENCE_COUNT);
            addRequirement(requirements, ModItems.ESSENCE_CRYSTAL.get(), LiquidEssenceRecipe.CRYSTAL_COUNT);
            if (!waterSource) {
                addRequirement(requirements, Items.WATER_BUCKET, 1);
                waterBuckets++;
            }

            if (!level.mayInteract(player, pos)) {
                obstructions.add(new Obstruction(pos, current, true));
            } else if (!current.isAir() && !waterSource) {
                obstructions.add(new Obstruction(pos, current, false));
            } else {
                liquidsToBrew.add(new MateriaTableRitualBuildProcess.LiquidTarget(
                        pos.immutable(),
                        current
                ));
            }
        }
        return new BuildPlan(
                List.copyOf(placements),
                List.copyOf(obstructions),
                List.copyOf(liquidsToBrew),
                new LinkedHashMap<>(requirements),
                waterBuckets
        );
    }

    private static void addRequirement(Map<Item, Integer> requirements, Item item, int amount) {
        if (item != Items.AIR && amount > 0) {
            requirements.merge(item, amount, Integer::sum);
        }
    }

    private static Map<Item, Integer> findMissingItems(
            Inventory inventory,
            Map<Item, Integer> requirements
    ) {
        Map<Item, Integer> missing = new LinkedHashMap<>();
        for (Map.Entry<Item, Integer> requirement : requirements.entrySet()) {
            int missingCount = requirement.getValue() - countItem(inventory, requirement.getKey());
            if (missingCount > 0) {
                missing.put(requirement.getKey(), missingCount);
            }
        }
        return missing;
    }

    private static int countItem(Inventory inventory, Item item) {
        int count = 0;
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.is(item)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private static boolean consumeRequirements(
            Inventory inventory,
            Map<Item, Integer> requirements
    ) {
        for (Map.Entry<Item, Integer> requirement : requirements.entrySet()) {
            if (countItem(inventory, requirement.getKey()) < requirement.getValue()) {
                return false;
            }
        }
        for (Map.Entry<Item, Integer> requirement : requirements.entrySet()) {
            consumeItem(inventory, requirement.getKey(), requirement.getValue());
        }
        inventory.setChanged();
        return true;
    }

    private static void consumeItem(Inventory inventory, Item item, int amount) {
        int remaining = amount;
        for (int slot = 0; slot < inventory.getContainerSize() && remaining > 0; slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (!stack.is(item)) {
                continue;
            }
            int consumed = Math.min(remaining, stack.getCount());
            stack.shrink(consumed);
            remaining -= consumed;
            if (stack.isEmpty()) {
                inventory.setItem(slot, ItemStack.EMPTY);
            }
        }
    }

    private static void rollback(ServerLevel level, List<Placement> placements) {
        for (int index = placements.size() - 1; index >= 0; index--) {
            Placement placement = placements.get(index);
            level.setBlock(placement.pos(), placement.original(), Block.UPDATE_ALL);
        }
    }

    private static void refundRequirements(
            ServerPlayer player,
            Map<Item, Integer> requirements
    ) {
        for (Map.Entry<Item, Integer> requirement : requirements.entrySet()) {
            giveItem(player, requirement.getKey(), requirement.getValue());
        }
    }

    private static void giveItem(ServerPlayer player, Item item, int amount) {
        int remaining = amount;
        while (remaining > 0) {
            ItemStack stack = new ItemStack(item);
            int count = Math.min(remaining, stack.getMaxStackSize());
            stack.setCount(count);
            remaining -= count;
            if (!player.getInventory().add(stack) && !stack.isEmpty()) {
                player.drop(stack, false);
            }
        }
    }

    private static void reportValidationFailure(
            ServerPlayer player,
            List<Obstruction> obstructions,
            Map<Item, Integer> missing
    ) {
        player.displayClientMessage(
                Component.translatable("message.materia_reborn.ritual_builder.invalid"),
                true
        );
        if (!obstructions.isEmpty()) {
            player.displayClientMessage(
                    Component.translatable("message.materia_reborn.ritual_builder.obstructions"),
                    false
            );
            for (Obstruction obstruction : obstructions) {
                String key = obstruction.protectedPosition()
                        ? "message.materia_reborn.ritual_builder.protected"
                        : "message.materia_reborn.ritual_builder.obstructed";
                player.displayClientMessage(
                        Component.translatable(
                                key,
                                obstruction.pos().getX(),
                                obstruction.pos().getY(),
                                obstruction.pos().getZ(),
                                obstruction.state().getBlock().getName()
                        ),
                        false
                );
            }
        }
        if (!missing.isEmpty()) {
            player.displayClientMessage(
                    Component.translatable("message.materia_reborn.ritual_builder.missing"),
                    false
            );
            for (Map.Entry<Item, Integer> entry : missing.entrySet()) {
                player.displayClientMessage(
                        Component.translatable(
                                "message.materia_reborn.ritual_builder.missing_line",
                                entry.getValue(),
                                new ItemStack(entry.getKey()).getHoverName()
                        ),
                        false
                );
            }
        }
    }

    private record Placement(
            BlockPos pos,
            BlockState original,
            BlockState target
    ) {
    }

    private record Obstruction(
            BlockPos pos,
            BlockState state,
            boolean protectedPosition
    ) {
    }

    private record BuildPlan(
            List<Placement> placements,
            List<Obstruction> obstructions,
            List<MateriaTableRitualBuildProcess.LiquidTarget> liquidsToBrew,
            Map<Item, Integer> requirements,
            int waterBucketsConsumed
    ) {
    }
}