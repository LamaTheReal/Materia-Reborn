package com.materiareborn.ritual;

import com.materiareborn.blockentity.MateriaTableBlockEntity;
import com.materiareborn.essence.PlayerEssence;
import com.materiareborn.registry.ModParticles;
import com.materiareborn.registry.ModFluids;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public final class MateriaTableRitualService {
    private static final int FLIGHT_WAVE_COUNT = 6;
    private static final int FLIGHT_WAVE_INTERVAL_TICKS = 10;
    private static final int FLIGHT_WAVE_TRAVEL_TICKS = 20;
    private static final int FLIGHT_TAIL_PARTICLES = 4;
    private static final int LIQUID_SURFACE_PARTICLE_INTERVAL_TICKS = 20;
    private static final double LIQUID_SURFACE_PARTICLE_OFFSET = 0.03D;
    private static final double LIQUID_SURFACE_PARTICLE_RADIUS = 0.18D;
    private MateriaTableRitualService() {
    }

    public static StartResult tryStart(
            ServerPlayer player,
            BlockPos tablePos,
            ItemStack coreStack,
            MateriaTableBlockEntity table
    ) {
        Optional<MateriaTableUpgrade> upgradeResult = MateriaTableUpgrade.forSource(table.getBlockState().getBlock());
        if (upgradeResult.isEmpty()) {
            return StartResult.MAX_TIER;
        }

        MateriaTableUpgrade upgrade = upgradeResult.get();
        if (table.isUpgradeRitualActive()) {
            return StartResult.ALREADY_ACTIVE;
        }
        if (MateriaTableRitualBuildProcess.isActive(player.serverLevel(), tablePos)) {
            return StartResult.STRUCTURE_PREPARING;
        }
        if (!coreStack.is(upgrade.core())) {
            return StartResult.WRONG_CORE;
        }
        if (!upgrade.matchesStructure(player.level(), tablePos)) {
            return StartResult.INVALID_STRUCTURE;
        }
        if (!PlayerEssence.trySpend(player, upgrade.essenceCost())) {
            return StartResult.NOT_ENOUGH_ESSENCE;
        }

        coreStack.shrink(1);
        table.startUpgradeRitual(upgrade.targetTier(), player.getUUID());
        PlayerEssence.syncOpenMenu(player);
        playSound((ServerLevel) player.level(), tablePos, upgrade.activationSound(), 1.0F, 1.0F);
        ((ServerLevel) player.level()).sendParticles(
                ModParticles.MAGIC_GLYPH.get(),
                tablePos.getX() + 0.5D,
                tablePos.getY() + 0.75D,
                tablePos.getZ() + 0.5D,
                3,
                0.25D,
                0.15D,
                0.25D,
                0.02D
        );
        return StartResult.STARTED;
    }

    /**
     * @return true while the table is occupied by an active ritual
     */
    public static boolean tick(
            ServerLevel level,
            BlockPos tablePos,
            BlockState tableState,
            MateriaTableBlockEntity table
    ) {
        if (!table.isUpgradeRitualActive()) {
            return false;
        }

        Optional<MateriaTableUpgrade> upgradeResult = MateriaTableUpgrade.byTargetTier(table.upgradeRitualTargetTier());
        if (upgradeResult.isEmpty() || tableState.getBlock() != upgradeResult.get().sourceTable()) {
            cancelChangedRitual(level, tablePos, table);
            return false;
        }

        MateriaTableUpgrade upgrade = upgradeResult.get();
        int step = table.upgradeRitualStep();
        if (step >= upgrade.blockCount()) {
            complete(level, tablePos, tableState, table, upgrade);
            return true;
        }
        if (!remainingStructureMatches(level, tablePos, upgrade, step)) {
            cancelChangedRitual(level, tablePos, table);
            return false;
        }

        BlockPos sourcePos = upgrade.ritualBlockPos(tablePos, step);
        BlockState sourceState = level.getBlockState(sourcePos);
        BlockPos essencePos = upgrade.ritualEssencePos(tablePos, step);

        int ritualTick = table.advanceUpgradeRitualTick();
        spawnFlyingBlockParticles(level, sourcePos, tablePos, sourceState, ritualTick);
        spawnLiquidEssenceSurfaceParticles(level, essencePos, ritualTick);
        spawnRitualParticles(level, tablePos, sourcePos, upgrade, ritualTick);

        if (ritualTick < MateriaTableUpgrade.BLOCK_TICKS) {
            return true;
        }

        playSound(level, sourcePos, upgrade.chargingSound(), 0.65F, 0.95F + step * 0.01F);
        level.destroyBlock(sourcePos, false);
        double essenceSurfaceY = liquidSurfaceY(level, essencePos);
        level.setBlock(essencePos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        level.sendParticles(
                ModParticles.LIQUID_ESSENCE_GLYPH.get(),
                essencePos.getX() + 0.5D,
                essenceSurfaceY + LIQUID_SURFACE_PARTICLE_OFFSET,
                essencePos.getZ() + 0.5D,
                0,
                0.0D,
                0.0D,
                0.0D,
                0.0D
        );
        table.advanceUpgradeRitualStep();

        if (table.upgradeRitualStep() >= upgrade.blockCount()) {
            complete(level, tablePos, tableState, table, upgrade);
        }
        return true;
    }

    private static boolean remainingStructureMatches(
            ServerLevel level,
            BlockPos tablePos,
            MateriaTableUpgrade upgrade,
            int firstIndex
    ) {
        for (int index = firstIndex; index < upgrade.blockCount(); index++) {
            if (!level.getBlockState(upgrade.ritualBlockPos(tablePos, index))
                    .is(upgrade.expectedBlock(index))) {
                return false;
            }
            BlockPos liquidPos = upgrade.ritualEssencePos(tablePos, index);
            if (!level.getFluidState(liquidPos).is(ModFluids.LIQUID_ESSENCE.get())
                    || !level.getFluidState(liquidPos).isSource()) {
                return false;
            }
        }
        return true;
    }

    private static void cancelChangedRitual(
            ServerLevel level,
            BlockPos tablePos,
            MateriaTableBlockEntity table
    ) {
        java.util.UUID owner = table.upgradeRitualOwner();
        table.clearUpgradeRitual();
        level.sendParticles(
                ParticleTypes.POOF,
                tablePos.getX() + 0.5D,
                tablePos.getY() + 0.75D,
                tablePos.getZ() + 0.5D,
                22,
                0.55D,
                0.30D,
                0.55D,
                0.04D
        );
        playSound(level, tablePos, SoundEvents.FIRE_EXTINGUISH, 0.65F, 0.8F);
        if (owner != null) {
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(owner);
            if (player != null) {
                player.displayClientMessage(
                        net.minecraft.network.chat.Component.translatable(
                                "message.materia_reborn.ritual.structure_changed"
                        ),
                        true
                );
            }
        }
    }
    private static void spawnFlyingBlockParticles(
            ServerLevel level,
            BlockPos sourcePos,
            BlockPos tablePos,
            BlockState sourceState,
            int ritualTick
    ) {
        Vec3 start = new Vec3(sourcePos.getX() + 0.5D, sourcePos.getY() + 1.03D, sourcePos.getZ() + 0.5D);
        Vec3 end = new Vec3(tablePos.getX() + 0.5D, tablePos.getY() + 0.72D, tablePos.getZ() + 0.5D);
        Vec3 control = new Vec3(
                (start.x + end.x) * 0.5D,
                Math.max(start.y, end.y) + 1.8D,
                (start.z + end.z) * 0.5D
        );
        BlockParticleOption particle = new BlockParticleOption(ParticleTypes.BLOCK, sourceState);
        if (ritualTick == 1 || (ritualTick <= 50 && ritualTick % FLIGHT_WAVE_INTERVAL_TICKS == 0)) {
            level.sendParticles(particle, start.x, start.y, start.z, 7, 0.22D, 0.12D, 0.22D, 0.035D);
        }
        if ((ritualTick & 1) != 0) {
            return;
        }

        for (int wave = 0; wave < FLIGHT_WAVE_COUNT; wave++) {
            int waveAge = ritualTick - wave * FLIGHT_WAVE_INTERVAL_TICKS;
            if (waveAge < 0 || waveAge > FLIGHT_WAVE_TRAVEL_TICKS) {
                continue;
            }
            double headProgress = waveAge / (double) FLIGHT_WAVE_TRAVEL_TICKS;
            for (int tail = 0; tail < FLIGHT_TAIL_PARTICLES; tail++) {
                double progress = headProgress - tail * 0.055D * (1.0D - headProgress);
                if (progress < 0.0D) {
                    continue;
                }
                progress = Math.min(progress, 1.0D);
                Vec3 point = quadratic(start, control, end, progress);
                Vec3 tangent = quadraticTangent(start, control, end, progress).normalize().scale(0.045D);
                level.sendParticles(particle, point.x, point.y, point.z, 0, tangent.x, tangent.y, tangent.z, 1.0D);
            }
        }
    }
    private static void spawnRitualParticles(
            ServerLevel level,
            BlockPos tablePos,
            BlockPos sourcePos,
            MateriaTableUpgrade upgrade,
            int ritualTick
    ) {
        if (ritualTick % 5 == 0) {
            int particleIndex = (ritualTick / 5) % upgrade.ritualParticles().size();
            ParticleOptions particle = upgrade.ritualParticles().get(particleIndex);
            level.sendParticles(
                    particle,
                    sourcePos.getX() + 0.5D,
                    sourcePos.getY() + 1.05D,
                    sourcePos.getZ() + 0.5D,
                    2,
                    0.18D,
                    0.10D,
                    0.18D,
                    0.015D
            );
        }
        if (ritualTick % 20 == 0) {
            level.sendParticles(
                    ModParticles.MAGIC_GLYPH.get(),
                    tablePos.getX() + 0.5D,
                    tablePos.getY() + 0.78D,
                    tablePos.getZ() + 0.5D,
                    1,
                    0.18D,
                    0.10D,
                    0.18D,
                    0.01D
            );
        }
    }

    private static void spawnLiquidEssenceSurfaceParticles(
            ServerLevel level,
            BlockPos sourcePos,
            int ritualTick
    ) {
        if (ritualTick != 1 && ritualTick % LIQUID_SURFACE_PARTICLE_INTERVAL_TICKS != 0) {
            return;
        }

        double angle = ritualTick * 0.45D;
        double x = sourcePos.getX() + 0.5D + Math.cos(angle) * LIQUID_SURFACE_PARTICLE_RADIUS;
        double z = sourcePos.getZ() + 0.5D + Math.sin(angle) * LIQUID_SURFACE_PARTICLE_RADIUS;
        level.sendParticles(
                ModParticles.LIQUID_ESSENCE_GLYPH.get(),
                x,
                liquidSurfaceY(level, sourcePos) + LIQUID_SURFACE_PARTICLE_OFFSET,
                z,
                0,
                0.0D,
                0.0D,
                0.0D,
                0.0D
        );
    }

    private static double liquidSurfaceY(ServerLevel level, BlockPos sourcePos) {
        return sourcePos.getY() + level.getFluidState(sourcePos).getHeight(level, sourcePos);
    }

    public static boolean isLiquidEssenceProtected(ServerLevel level, BlockPos liquidPos) {
        for (int offsetX = -2; offsetX <= 2; offsetX++) {
            for (int offsetZ = -2; offsetZ <= 2; offsetZ++) {
                BlockPos tablePos = liquidPos.offset(offsetX, 1, offsetZ);
                if (!(level.getBlockEntity(tablePos) instanceof MateriaTableBlockEntity table)
                        || !table.isUpgradeRitualActive()) {
                    continue;
                }
                Optional<MateriaTableUpgrade> upgrade =
                        MateriaTableUpgrade.byTargetTier(table.upgradeRitualTargetTier());
                if (upgrade.isPresent() && upgrade.get().isEssencePosition(tablePos, liquidPos)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void complete(
            ServerLevel level,
            BlockPos tablePos,
            BlockState oldState,
            MateriaTableBlockEntity table,
            MateriaTableUpgrade upgrade
    ) {
        table.clearUpgradeRitual();
        CompoundTag savedData = table.saveWithoutMetadata(level.registryAccess());
        table.setUpgradeReplacementInProgress(true);

        BlockState upgradedState = upgrade.targetTable().defaultBlockState()
                .setValue(HorizontalDirectionalBlock.FACING, oldState.getValue(HorizontalDirectionalBlock.FACING))
                .setValue(AbstractFurnaceBlock.LIT, oldState.getValue(AbstractFurnaceBlock.LIT));
        level.setBlock(tablePos, upgradedState, Block.UPDATE_ALL);

        BlockEntity replacement = level.getBlockEntity(tablePos);
        if (replacement instanceof MateriaTableBlockEntity upgradedTable) {
            upgradedTable.loadWithComponents(savedData, level.registryAccess());
            upgradedTable.setUpgradeReplacementInProgress(false);
            upgradedTable.setChanged();
            level.sendBlockUpdated(tablePos, upgradedState, upgradedState, Block.UPDATE_CLIENTS);
        }
        table.setUpgradeReplacementInProgress(false);

        playSound(level, tablePos, upgrade.completionSound(), 1.2F, 1.0F);
        spawnCompletionParticles(level, tablePos, upgrade.targetTier());
    }

    private static void spawnCompletionParticles(ServerLevel level, BlockPos tablePos, int targetTier) {
        double x = tablePos.getX() + 0.5D;
        double y = tablePos.getY() + 0.8D;
        double z = tablePos.getZ() + 0.5D;
        if (targetTier == 2) {
            level.sendParticles(ParticleTypes.COMPOSTER, x, y, z, 38, 0.75D, 0.45D, 0.75D, 0.08D);
            spawnRing(level, tablePos, ParticleTypes.END_ROD, 32, 1.15D);
        } else if (targetTier == 3) {
            spawnRing(level, tablePos, ParticleTypes.END_ROD, 72, 1.55D);
        } else {
            level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, x, y, z, 65, 0.85D, 0.65D, 0.85D, 0.14D);
            spawnRing(level, tablePos, ParticleTypes.END_ROD, 48, 1.4D);
        }
        level.sendParticles(ModParticles.MAGIC_GLYPH.get(), x, y, z, 5, 0.42D, 0.30D, 0.42D, 0.025D);
    }

    private static void spawnRing(
            ServerLevel level,
            BlockPos tablePos,
            ParticleOptions particle,
            int count,
            double radius
    ) {
        for (int index = 0; index < count; index++) {
            double angle = Math.PI * 2.0D * index / count;
            double directionX = Math.cos(angle);
            double directionZ = Math.sin(angle);
            level.sendParticles(
                    particle,
                    tablePos.getX() + 0.5D + directionX * radius,
                    tablePos.getY() + 0.72D,
                    tablePos.getZ() + 0.5D + directionZ * radius,
                    0,
                    directionX * 0.075D,
                    0.045D,
                    directionZ * 0.075D,
                    1.0D
            );
        }
    }

    private static Vec3 quadratic(Vec3 start, Vec3 control, Vec3 end, double progress) {
        double inverse = 1.0D - progress;
        return start.scale(inverse * inverse)
                .add(control.scale(2.0D * inverse * progress))
                .add(end.scale(progress * progress));
    }

    private static Vec3 quadraticTangent(Vec3 start, Vec3 control, Vec3 end, double progress) {
        return control.subtract(start).scale(2.0D * (1.0D - progress))
                .add(end.subtract(control).scale(2.0D * progress));
    }

    private static void playSound(
            ServerLevel level,
            BlockPos pos,
            net.minecraft.sounds.SoundEvent sound,
            float volume,
            float pitch
    ) {
        level.playSound(null, pos, sound, SoundSource.BLOCKS, volume, pitch);
    }

    public enum StartResult {
        STARTED,
        ALREADY_ACTIVE,
        WRONG_CORE,
        INVALID_STRUCTURE,
        STRUCTURE_PREPARING,
        NOT_ENOUGH_ESSENCE,
        MAX_TIER
    }
}
