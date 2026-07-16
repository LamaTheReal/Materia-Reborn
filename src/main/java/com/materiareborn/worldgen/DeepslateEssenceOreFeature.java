package com.materiareborn.worldgen;

import com.materiareborn.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public final class DeepslateEssenceOreFeature extends Feature<NoneFeatureConfiguration> {
    private static final int CHUNK_SUCCESS_PERCENT = 70;
    private static final int MIN_VEIN_SIZE = 1;
    private static final int MAX_VEIN_SIZE = 5;

    public DeepslateEssenceOreFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        RandomSource random = context.random();
        if (random.nextInt(100) >= CHUNK_SUCCESS_PERCENT) {
            return false;
        }

        BlockPos.MutableBlockPos cursor = context.origin().mutable();
        int requestedSize = MIN_VEIN_SIZE + random.nextInt(MAX_VEIN_SIZE);
        int placed = 0;
        for (int index = 0; index < requestedSize; index++) {
            if (context.level().getBlockState(cursor).is(Blocks.DEEPSLATE)) {
                context.level().setBlock(cursor, ModBlocks.DEEPSLATE_ESSENCE.get().defaultBlockState(), 2);
                placed++;
            }
            cursor.move(random.nextInt(3) - 1, 0, random.nextInt(3) - 1);
        }
        return placed > 0;
    }
}