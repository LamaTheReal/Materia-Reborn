package com.materiareborn.event;

import com.materiareborn.block.MateriaTableBlock;
import com.materiareborn.blockentity.MateriaTableBlockEntity;
import com.materiareborn.essence.PlayerEssence;
import com.materiareborn.ritual.MateriaTableRitualService;
import com.materiareborn.ritual.MateriaTableUpgrade;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public final class MateriaTableRitualEvents {
    private MateriaTableRitualEvents() {
    }

    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getLevel().getBlockState(event.getPos()).getBlock() instanceof MateriaTableBlock)
                || !MateriaTableUpgrade.isUpgradeCore(event.getItemStack())) {
            return;
        }

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        if (event.getLevel().isClientSide || !(event.getEntity() instanceof ServerPlayer player)
                || !(event.getLevel().getBlockEntity(event.getPos()) instanceof MateriaTableBlockEntity table)) {
            return;
        }

        Optional<MateriaTableUpgrade> expectedUpgrade = MateriaTableUpgrade.forSource(table.getBlockState().getBlock());
        MateriaTableRitualService.StartResult result = MateriaTableRitualService.tryStart(
                player,
                event.getPos(),
                event.getItemStack(),
                table
        );
        if (result == MateriaTableRitualService.StartResult.STARTED) {
            player.swing(event.getHand(), true);
            return;
        }

        long requiredEssence = expectedUpgrade.map(MateriaTableUpgrade::essenceCost).orElse(0L);
        long availableEssence = PlayerEssence.get(player);
        Component message = switch (result) {
            case ALREADY_ACTIVE -> Component.translatable("message.materia_reborn.ritual.already_active");
            case WRONG_CORE -> Component.translatable(
                    "message.materia_reborn.ritual.wrong_core",
                    expectedUpgrade
                            .map(upgrade -> new ItemStack(upgrade.core()).getHoverName())
                            .orElse(Component.empty())
            );
            case INVALID_STRUCTURE -> Component.translatable("message.materia_reborn.ritual.invalid_structure");
            case STRUCTURE_PREPARING -> Component.translatable(
                    "message.materia_reborn.ritual.structure_preparing"
            );
            case NOT_ENOUGH_ESSENCE -> Component.translatable(
                    "message.materia_reborn.ritual.not_enough_essence",
                    Math.max(0L, requiredEssence - availableEssence),
                    requiredEssence,
                    availableEssence
            );
            case MAX_TIER -> Component.translatable("message.materia_reborn.ritual.max_tier");
            case STARTED -> Component.empty();
        };
        player.displayClientMessage(message, true);
    }
}
