package com.materiareborn.registry;

import com.materiareborn.core.ModConstants;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(Registries.PARTICLE_TYPE, ModConstants.MOD_ID);

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> MAGIC_GLYPH = PARTICLE_TYPES.register(
            "magic_glyph",
            () -> new SimpleParticleType(false)
    );

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> LIQUID_ESSENCE_GLYPH = PARTICLE_TYPES.register(
            "liquid_essence_glyph",
            () -> new SimpleParticleType(false)
    );

    private ModParticles() {
    }
}