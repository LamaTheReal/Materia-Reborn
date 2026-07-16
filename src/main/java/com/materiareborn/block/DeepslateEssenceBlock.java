package com.materiareborn.block;

import net.minecraft.core.BlockPos;
import com.materiareborn.registry.ModParticles;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public final class DeepslateEssenceBlock extends Block {
    private static final int PARTICLE_INTERVAL = 5;
    private static final double PARTICLE_RADIUS = 3.0D;

    public DeepslateEssenceBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(PARTICLE_INTERVAL) != 0) {
            return;
        }
        double x = pos.getX() + 0.5D + (random.nextDouble() - 0.5D) * PARTICLE_RADIUS * 2.0D;
        double y = pos.getY() + random.nextDouble();
        double z = pos.getZ() + 0.5D + (random.nextDouble() - 0.5D) * PARTICLE_RADIUS * 2.0D;
        level.addParticle(ModParticles.MAGIC_GLYPH.get(), x, y, z, 0.0D, 0.02D, 0.0D);
    }
}