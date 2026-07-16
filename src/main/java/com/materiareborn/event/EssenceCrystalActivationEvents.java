package com.materiareborn.event;

import com.materiareborn.registry.ModItems;
import com.materiareborn.registry.ModParticles;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public final class EssenceCrystalActivationEvents {
    private EssenceCrystalActivationEvents() {
    }

    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();
        if (!((mainHand.is(ModItems.ESSENCE_CRYSTAL.get()) && offHand.is(Items.FLINT))
                || (mainHand.is(Items.FLINT) && offHand.is(ModItems.ESSENCE_CRYSTAL.get())))) {
            return;
        }

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        if (player.level().isClientSide) {
            return;
        }

        mainHand.shrink(1);
        offHand.shrink(1);
        ItemStack dust = new ItemStack(ModItems.ESSENCE_DUST.get());
        player.getInventory().add(dust);
        if (!dust.isEmpty()) {
            player.drop(dust, false);
        }
        player.level().playSound(null, player.blockPosition(), SoundEvents.FLINTANDSTEEL_USE, SoundSource.PLAYERS, 0.8F, 1.15F);
        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ModParticles.MAGIC_GLYPH.get(),
                    player.getX(),
                    player.getY() + 1.0D,
                    player.getZ(),
                    18,
                    0.22D,
                    0.30D,
                    0.22D,
                    0.04D
            );
        }
        player.swing(event.getHand(), true);
    }
}