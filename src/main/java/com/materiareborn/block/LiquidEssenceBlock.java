package com.materiareborn.block;

import com.materiareborn.fluid.LiquidEssenceExposure;
import com.materiareborn.registry.ModParticles;
import com.materiareborn.ritual.MateriaTableRitualService;
import com.materiareborn.ritual.MateriaTableUpgrade;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FlowingFluid;
import org.jetbrains.annotations.Nullable;

public final class LiquidEssenceBlock extends LiquidBlock {
    public static final int LIFETIME_TICKS = 45 * 20;
    public static final int STABILIZED_LIFETIME_TICKS = 160 * 20;
    public static final BooleanProperty RITUAL_STABILIZED = BooleanProperty.create("ritual_stabilized");
    private static final int RITUAL_RECHECK_TICKS = 20;

    public LiquidEssenceBlock(FlowingFluid fluid, Properties properties) {
        super(fluid, properties);
        registerDefaultState(defaultBlockState().setValue(RITUAL_STABILIZED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(RITUAL_STABILIZED);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide && !oldState.is(this)) {
            scheduleLifetime(level, pos, state);
        }
    }

    @Override
    public void setPlacedBy(
            Level level,
            BlockPos pos,
            BlockState state,
            @Nullable LivingEntity placer,
            ItemStack stack
    ) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide) {
            scheduleLifetime(level, pos, level.getBlockState(pos));
        }
    }

    private void scheduleLifetime(Level level, BlockPos pos, BlockState state) {
        if (!state.is(this)) {
            return;
        }

        boolean stabilized = state.getValue(RITUAL_STABILIZED)
                || MateriaTableUpgrade.hasStabilizingRitualBlock(level, pos);
        if (stabilized && !state.getValue(RITUAL_STABILIZED)) {
            state = state.setValue(RITUAL_STABILIZED, true);
            level.setBlock(pos, state, Block.UPDATE_CLIENTS);
        }
        level.scheduleTick(pos, this, stabilized ? STABILIZED_LIFETIME_TICKS : LIFETIME_TICKS);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (MateriaTableRitualService.isLiquidEssenceProtected(level, pos)) {
            level.scheduleTick(pos, this, RITUAL_RECHECK_TICKS);
            return;
        }

        if (!state.getValue(RITUAL_STABILIZED)
                && MateriaTableUpgrade.hasStabilizingRitualBlock(level, pos)) {
            level.setBlock(pos, state.setValue(RITUAL_STABILIZED, true), Block.UPDATE_CLIENTS);
            level.scheduleTick(pos, this, STABILIZED_LIFETIME_TICKS - LIFETIME_TICKS);
            return;
        }

        level.sendParticles(ParticleTypes.CLOUD, pos.getX() + 0.5D, pos.getY() + 0.35D, pos.getZ() + 0.5D,
                24, 0.38D, 0.22D, 0.38D, 0.035D);
        level.sendParticles(ParticleTypes.POOF, pos.getX() + 0.5D, pos.getY() + 0.35D, pos.getZ() + 0.5D,
                10, 0.28D, 0.16D, 0.28D, 0.025D);
        level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.45F, 1.45F);
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!level.isClientSide && entity instanceof ServerPlayer player) {
            LiquidEssenceExposure.touch(player);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(8) == 0) {
            level.addParticle(ModParticles.MAGIC_GLYPH.get(),
                    pos.getX() + random.nextDouble(),
                    pos.getY() + 0.7D + random.nextDouble() * 0.2D,
                    pos.getZ() + random.nextDouble(),
                    0.0D, 0.015D, 0.0D);
        }
    }

    @Override
    public ItemStack pickupBlock(
            @Nullable net.minecraft.world.entity.player.Player player,
            LevelAccessor level,
            BlockPos pos,
            BlockState state
    ) {
        return ItemStack.EMPTY;
    }
}