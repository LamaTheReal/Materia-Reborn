package com.materiareborn.registry;

import com.materiareborn.core.ModConstants;
import com.materiareborn.effect.EssenceSightEffect;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModMobEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(Registries.MOB_EFFECT, ModConstants.MOD_ID);

    public static final DeferredHolder<MobEffect, EssenceSightEffect> ESSENCE_SIGHT = MOB_EFFECTS.register(
            "essence_sight",
            EssenceSightEffect::new
    );

    private ModMobEffects() {
    }
}
