package com.materiareborn.event;

import com.materiareborn.registry.ModItems;
import com.materiareborn.registry.ModParticles;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

public final class EssenceCrystalActivationEvents {
    private static final int REFINING_TICKS = 100;
    private static final int MAX_BATCH_SIZE = 64;
    private static final int RUB_INTERVAL_TICKS = 8;
    private static final Map<Player, RefiningProcess> ACTIVE_REFINING = new WeakHashMap<>();

    private EssenceCrystalActivationEvents() {
    }

    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();
        boolean crystalInMainHand = mainHand.is(ModItems.ESSENCE_CRYSTAL.get()) && offHand.is(Items.FLINT);
        boolean flintInMainHand = mainHand.is(Items.FLINT) && offHand.is(ModItems.ESSENCE_CRYSTAL.get());
        if (!crystalInMainHand && !flintInMainHand) {
            return;
        }

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        if (!(player instanceof ServerPlayer serverPlayer) || ACTIVE_REFINING.containsKey(player)) {
            return;
        }

        int amount = Math.min(MAX_BATCH_SIZE, Math.min(mainHand.getCount(), offHand.getCount()));
        if (amount <= 0) {
            return;
        }

        ACTIVE_REFINING.put(player, new RefiningProcess(amount, crystalInMainHand));
        playRubbingEffects(serverPlayer, true);
    }

    public static void onPlayerTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        RefiningProcess process = ACTIVE_REFINING.get(player);
        if (process == null) {
            return;
        }
        if (!player.isAlive() || !process.matches(player)) {
            ACTIVE_REFINING.remove(player);
            return;
        }

        process.remainingTicks--;
        int elapsedTicks = REFINING_TICKS - process.remainingTicks;
        if (process.remainingTicks > 0 && elapsedTicks % RUB_INTERVAL_TICKS == 0) {
            playRubbingEffects(player, elapsedTicks % 16 == 0);
        }
        if (process.remainingTicks > 0) {
            return;
        }

        ACTIVE_REFINING.remove(player);
        if (process.matches(player)) {
            completeRefining(player, process);
        }
    }

    private static void completeRefining(ServerPlayer player, RefiningProcess process) {
        player.getMainHandItem().shrink(process.amount);
        player.getOffhandItem().shrink(process.amount);

        ItemStack result = new ItemStack(ModItems.ESSENCE_DUST.get(), process.amount);
        player.getInventory().add(result);
        if (!result.isEmpty()) {
            player.drop(result, false);
        }
        player.getInventory().setChanged();

        ServerLevel level = player.serverLevel();
        Vec3 effectPosition = refiningEffectPosition(player);
        level.playSound(
                null,
                player.blockPosition(),
                SoundEvents.AMETHYST_BLOCK_CHIME,
                SoundSource.PLAYERS,
                0.9F,
                1.25F
        );
        level.sendParticles(
                ModParticles.MAGIC_GLYPH.get(),
                effectPosition.x,
                effectPosition.y,
                effectPosition.z,
                Math.min(48, 12 + process.amount),
                0.18D,
                0.15D,
                0.18D,
                0.035D
        );
    }

    private static void playRubbingEffects(ServerPlayer player, boolean playSound) {
        ServerLevel level = player.serverLevel();
        Vec3 effectPosition = refiningEffectPosition(player);
        sendItemParticles(level, effectPosition, player.getMainHandItem());
        sendItemParticles(level, effectPosition, player.getOffhandItem());
        level.sendParticles(
                ModParticles.MAGIC_GLYPH.get(),
                effectPosition.x,
                effectPosition.y,
                effectPosition.z,
                1,
                0.10D,
                0.08D,
                0.10D,
                0.012D
        );
        if (playSound) {
            level.playSound(
                    null,
                    player.blockPosition(),
                    SoundEvents.FLINTANDSTEEL_USE,
                    SoundSource.PLAYERS,
                    0.28F,
                    0.85F + level.random.nextFloat() * 0.15F
            );
        }
    }

    private static void sendItemParticles(ServerLevel level, Vec3 position, ItemStack stack) {
        level.sendParticles(
                new ItemParticleOption(ParticleTypes.ITEM, stack),
                position.x,
                position.y,
                position.z,
                1,
                0.08D,
                0.06D,
                0.08D,
                0.018D
        );
    }

    private static Vec3 refiningEffectPosition(ServerPlayer player) {
        return player.getEyePosition()
                .add(player.getLookAngle().scale(0.55D))
                .add(0.0D, -0.38D, 0.0D);
    }

    private static final class RefiningProcess {
        private final int amount;
        private final boolean crystalInMainHand;
        private int remainingTicks = REFINING_TICKS;

        private RefiningProcess(int amount, boolean crystalInMainHand) {
            this.amount = amount;
            this.crystalInMainHand = crystalInMainHand;
        }

        private boolean matches(Player player) {
            ItemStack mainHand = player.getMainHandItem();
            ItemStack offHand = player.getOffhandItem();
            boolean correctItems = crystalInMainHand
                    ? mainHand.is(ModItems.ESSENCE_CRYSTAL.get()) && offHand.is(Items.FLINT)
                    : mainHand.is(Items.FLINT) && offHand.is(ModItems.ESSENCE_CRYSTAL.get());
            return correctItems && mainHand.getCount() >= amount && offHand.getCount() >= amount;
        }
    }
}
