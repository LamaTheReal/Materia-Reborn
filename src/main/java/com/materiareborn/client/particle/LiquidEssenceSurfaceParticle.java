package com.materiareborn.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import org.jetbrains.annotations.Nullable;

public final class LiquidEssenceSurfaceParticle extends TextureSheetParticle {
    private final SpriteSet sprites;

    private LiquidEssenceSurfaceParticle(ClientLevel level, double x, double y, double z, SpriteSet sprites) {
        super(level, x, y, z);
        this.sprites = sprites;
        this.hasPhysics = false;
        this.gravity = 0.0F;
        this.lifetime = 9 + this.random.nextInt(4);
        this.quadSize = 0.06F + this.random.nextFloat() * 0.02F;
        this.setParticleSpeed(0.0D, 0.0D, 0.0D);
        this.setSpriteFromAge(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.removed) {
            this.setParticleSpeed(0.0D, 0.0D, 0.0D);
            this.setSpriteFromAge(sprites);
        }
    }

    @Override
    public int getLightColor(float partialTick) {
        return 15728880;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static final class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Nullable
        @Override
        public Particle createParticle(
                SimpleParticleType type,
                ClientLevel level,
                double x,
                double y,
                double z,
                double xSpeed,
                double ySpeed,
                double zSpeed
        ) {
            return new LiquidEssenceSurfaceParticle(level, x, y, z, sprites);
        }
    }
}
