package com.materiareborn.ritual;

import com.materiareborn.blockentity.MateriaTableBlockEntity;
import com.materiareborn.fluid.LiquidEssenceRecipe;
import com.materiareborn.registry.ModBlocks;
import com.materiareborn.registry.ModFluids;
import com.materiareborn.registry.ModParticles;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

public final class MateriaTableRitualBuildProcess {
    private static final Map<ServerLevel, Map<BlockPos, PendingBuild>> ACTIVE_BUILDS = new WeakHashMap<>();

    private MateriaTableRitualBuildProcess() {
    }

    public static boolean isActive(ServerLevel level, BlockPos tablePos) {
        Map<BlockPos, PendingBuild> levelBuilds = ACTIVE_BUILDS.get(level);
        return levelBuilds != null && levelBuilds.containsKey(tablePos);
    }

    public static boolean start(
            ServerLevel level,
            UUID owner,
            BlockPos tablePos,
            MateriaTableUpgrade upgrade,
            List<LiquidTarget> liquidTargets
    ) {
        if (liquidTargets.isEmpty()) {
            return true;
        }

        Map<BlockPos, PendingBuild> levelBuilds = ACTIVE_BUILDS.computeIfAbsent(
                level,
                ignored -> new HashMap<>()
        );
        BlockPos key = tablePos.immutable();
        if (levelBuilds.containsKey(key)) {
            return false;
        }

        Map<BlockPos, BlockState> expectedStates = new LinkedHashMap<>();
        for (LiquidTarget target : liquidTargets) {
            expectedStates.put(target.pos().immutable(), target.originalState());
        }
        levelBuilds.put(
                key,
                new PendingBuild(
                        owner,
                        key,
                        upgrade,
                        Map.copyOf(expectedStates),
                        LiquidEssenceRecipe.BREW_TICKS
                )
        );
        playBrewingPulse(level, key, expectedStates.keySet().stream().toList(), true);
        return true;
    }

    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        Map<BlockPos, PendingBuild> levelBuilds = ACTIVE_BUILDS.get(level);
        if (levelBuilds == null || levelBuilds.isEmpty()) {
            return;
        }

        Iterator<Map.Entry<BlockPos, PendingBuild>> iterator = levelBuilds.entrySet().iterator();
        while (iterator.hasNext()) {
            PendingBuild build = iterator.next().getValue();
            if (!allChunksLoaded(level, build)) {
                continue;
            }
            if (!structureUnchanged(level, build)) {
                cancel(level, build);
                iterator.remove();
                continue;
            }

            build.remainingTicks--;
            if (build.remainingTicks > 0) {
                if (build.remainingTicks % 4 == 0) {
                    spawnBrewingParticles(level, build.liquidTargets.keySet());
                }
                if (build.remainingTicks % 10 == 0) {
                    playBrewingPulse(
                            level,
                            build.tablePos,
                            build.liquidTargets.keySet().stream().toList(),
                            false
                    );
                }
                continue;
            }

            if (complete(level, build)) {
                notifyPlayer(
                        level,
                        build.owner,
                        Component.translatable("message.materia_reborn.ritual_builder.success")
                );
            } else {
                notifyPlayer(
                        level,
                        build.owner,
                        Component.translatable("message.materia_reborn.ritual_builder.brewing_cancelled")
                );
            }
            iterator.remove();
        }

        if (levelBuilds.isEmpty()) {
            ACTIVE_BUILDS.remove(level);
        }
    }

    private static boolean allChunksLoaded(ServerLevel level, PendingBuild build) {
        if (!level.hasChunkAt(build.tablePos)) {
            return false;
        }
        for (int index = 0; index < build.upgrade.blockCount(); index++) {
            if (!level.hasChunkAt(build.upgrade.ritualBlockPos(build.tablePos, index))
                    || !level.hasChunkAt(build.upgrade.ritualEssencePos(build.tablePos, index))) {
                return false;
            }
        }
        return true;
    }

    private static boolean structureUnchanged(ServerLevel level, PendingBuild build) {
        if (!(level.getBlockEntity(build.tablePos) instanceof MateriaTableBlockEntity table)
                || table.isUpgradeRitualActive()
                || level.getBlockState(build.tablePos).getBlock() != build.upgrade.sourceTable()) {
            return false;
        }

        for (int index = 0; index < build.upgrade.blockCount(); index++) {
            if (!level.getBlockState(build.upgrade.ritualBlockPos(build.tablePos, index))
                    .is(build.upgrade.expectedBlock(index))) {
                return false;
            }

            BlockPos liquidPos = build.upgrade.ritualEssencePos(build.tablePos, index);
            BlockState expected = build.liquidTargets.get(liquidPos);
            if (expected == null) {
                if (!isLiquidEssenceSource(level, liquidPos)) {
                    return false;
                }
            } else if (!isLiquidEssenceSource(level, liquidPos)
                    && !matchesOriginalLiquidPosition(level, liquidPos, expected)) {
                return false;
            }
        }
        return true;
    }

    private static boolean matchesOriginalLiquidPosition(
            ServerLevel level,
            BlockPos pos,
            BlockState expected
    ) {
        BlockState current = level.getBlockState(pos);
        if (expected.isAir()) {
            return current.isAir();
        }
        return expected.is(Blocks.WATER)
                && current.is(Blocks.WATER)
                && level.getFluidState(pos).isSource();
    }

    private static boolean isLiquidEssenceSource(ServerLevel level, BlockPos pos) {
        return level.getFluidState(pos).is(ModFluids.LIQUID_ESSENCE.get())
                && level.getFluidState(pos).isSource();
    }

    private static boolean complete(ServerLevel level, PendingBuild build) {
        List<LiquidTarget> completed = new ArrayList<>();
        for (Map.Entry<BlockPos, BlockState> target : build.liquidTargets.entrySet()) {
            BlockPos pos = target.getKey();
            if (isLiquidEssenceSource(level, pos)) {
                continue;
            }
            if (!level.setBlock(pos, ModBlocks.LIQUID_ESSENCE.get().defaultBlockState(), Block.UPDATE_ALL)) {
                rollbackLiquids(level, completed);
                return false;
            }
            completed.add(new LiquidTarget(pos, target.getValue()));
        }

        for (BlockPos pos : build.liquidTargets.keySet()) {
            level.sendParticles(
                    ModParticles.MAGIC_GLYPH.get(),
                    pos.getX() + 0.5D,
                    pos.getY() + 0.55D,
                    pos.getZ() + 0.5D,
                    12,
                    0.30D,
                    0.22D,
                    0.30D,
                    0.03D
            );
        }
        level.playSound(
                null,
                build.tablePos,
                SoundEvents.AMETHYST_BLOCK_RESONATE,
                SoundSource.BLOCKS,
                1.0F,
                1.25F
        );
        return true;
    }

    private static void rollbackLiquids(ServerLevel level, List<LiquidTarget> completed) {
        for (int index = completed.size() - 1; index >= 0; index--) {
            LiquidTarget target = completed.get(index);
            level.setBlock(target.pos(), target.originalState(), Block.UPDATE_ALL);
        }
    }

    private static void cancel(ServerLevel level, PendingBuild build) {
        level.sendParticles(
                ParticleTypes.POOF,
                build.tablePos.getX() + 0.5D,
                build.tablePos.getY() + 0.7D,
                build.tablePos.getZ() + 0.5D,
                18,
                0.55D,
                0.30D,
                0.55D,
                0.035D
        );
        level.playSound(
                null,
                build.tablePos,
                SoundEvents.FIRE_EXTINGUISH,
                SoundSource.BLOCKS,
                0.55F,
                1.15F
        );
        notifyPlayer(
                level,
                build.owner,
                Component.translatable("message.materia_reborn.ritual_builder.brewing_cancelled")
        );
    }

    private static void spawnBrewingParticles(ServerLevel level, Iterable<BlockPos> positions) {
        for (BlockPos pos : positions) {
            level.sendParticles(
                    ModParticles.MAGIC_GLYPH.get(),
                    pos.getX() + 0.5D,
                    pos.getY() + 0.45D,
                    pos.getZ() + 0.5D,
                    2,
                    0.28D,
                    0.18D,
                    0.28D,
                    0.018D
            );
        }
    }

    private static void playBrewingPulse(
            ServerLevel level,
            BlockPos tablePos,
            List<BlockPos> positions,
            boolean firstPulse
    ) {
        if (positions.isEmpty()) {
            return;
        }
        int elapsed = LiquidEssenceRecipe.BREW_TICKS;
        Map<BlockPos, PendingBuild> levelBuilds = ACTIVE_BUILDS.get(level);
        if (levelBuilds != null && levelBuilds.get(tablePos) != null) {
            elapsed -= levelBuilds.get(tablePos).remainingTicks;
        }
        BlockPos pos = positions.get(Math.floorMod(elapsed / 10, positions.size()));
        level.sendParticles(
                ParticleTypes.EXPLOSION,
                pos.getX() + 0.5D,
                pos.getY() + 0.5D,
                pos.getZ() + 0.5D,
                1,
                0.08D,
                0.08D,
                0.08D,
                0.0D
        );
        level.playSound(
                null,
                pos,
                SoundEvents.GENERIC_EXPLODE.value(),
                SoundSource.BLOCKS,
                firstPulse ? 0.18F : 0.12F,
                1.15F + Math.min(0.35F, elapsed * 0.004F)
        );
    }

    private static void notifyPlayer(ServerLevel level, UUID owner, Component message) {
        ServerPlayer player = level.getServer().getPlayerList().getPlayer(owner);
        if (player != null) {
            player.displayClientMessage(message, true);
        }
    }

    public record LiquidTarget(BlockPos pos, BlockState originalState) {
    }

    private static final class PendingBuild {
        private final UUID owner;
        private final BlockPos tablePos;
        private final MateriaTableUpgrade upgrade;
        private final Map<BlockPos, BlockState> liquidTargets;
        private int remainingTicks;

        private PendingBuild(
                UUID owner,
                BlockPos tablePos,
                MateriaTableUpgrade upgrade,
                Map<BlockPos, BlockState> liquidTargets,
                int remainingTicks
        ) {
            this.owner = owner;
            this.tablePos = tablePos;
            this.upgrade = upgrade;
            this.liquidTargets = liquidTargets;
            this.remainingTicks = remainingTicks;
        }
    }
}