package com.materiareborn.fluid;

import com.materiareborn.registry.ModMobEffects;
import com.materiareborn.registry.ModParticles;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

public final class LiquidEssenceExposure {
    private static final String CONTACT_START_TAG = "materia_reborn.liquid_essence_contact_start";
    private static final String LAST_CONTACT_TAG = "materia_reborn.liquid_essence_last_contact";
    private static final String LAST_OVERLOAD_PULSE_TAG = "materia_reborn.liquid_essence_last_overload_pulse";
    private static final int OVERLOAD_TICKS = 100;

    private LiquidEssenceExposure() {
    }

    public static void touch(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        long gameTime = level.getGameTime();
        CompoundTag data = player.getPersistentData();
        long lastContact = data.getLong(LAST_CONTACT_TAG);
        if (!data.contains(LAST_CONTACT_TAG, Tag.TAG_LONG) || gameTime - lastContact > 2L) {
            data.putLong(CONTACT_START_TAG, gameTime);
            data.remove(LAST_OVERLOAD_PULSE_TAG);
        }
        data.putLong(LAST_CONTACT_TAG, gameTime);

        player.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 6, 0, true, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 6, 0, true, false, true));

        long contactTicks = gameTime - data.getLong(CONTACT_START_TAG);
        if (contactTicks < OVERLOAD_TICKS) {
            return;
        }


        player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 100, 0));
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 0));
        player.addEffect(new MobEffectInstance(ModMobEffects.ESSENCE_SIGHT, 300, 0));

        long lastPulse = data.getLong(LAST_OVERLOAD_PULSE_TAG);
        if (data.contains(LAST_OVERLOAD_PULSE_TAG, Tag.TAG_LONG) && gameTime - lastPulse < OVERLOAD_TICKS) {
            return;
        }
        data.putLong(LAST_OVERLOAD_PULSE_TAG, gameTime);
        level.sendParticles(
                ModParticles.MAGIC_GLYPH.get(),
                player.getX(),
                player.getY() + 1.0D,
                player.getZ(),
                18,
                0.45D,
                0.65D,
                0.45D,
                0.04D
        );
        level.playSound(null, player.blockPosition(), SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.PLAYERS, 0.8F, 1.25F);
    }
}
