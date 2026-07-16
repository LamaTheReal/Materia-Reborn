package com.materiareborn.ritual;

import com.materiareborn.registry.ModBlocks;
import com.materiareborn.registry.ModItems;
import com.materiareborn.registry.ModFluids;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public enum MateriaTableUpgrade {
    LEVEL_2(
            2,
            ModBlocks.MATERIA_TABLE,
            ModBlocks.MATERIA_TABLE_2,
            ModItems.UPGRADE_CORE_2,
            40_000L,
            Blocks.COPPER_BLOCK,
            Blocks.CHISELED_STONE_BRICKS,
            Blocks.BONE_BLOCK,
            SoundEvents.AMETHYST_BLOCK_CHIME,
            SoundEvents.COPPER_BULB_TURN_ON,
            SoundEvents.BEACON_ACTIVATE,
            ParticleTypes.WAX_ON,
            ParticleTypes.ELECTRIC_SPARK,
            ParticleTypes.HAPPY_VILLAGER
    ),
    LEVEL_3(
            3,
            ModBlocks.MATERIA_TABLE_2,
            ModBlocks.MATERIA_TABLE_3,
            ModItems.UPGRADE_CORE_3,
            120_000L,
            Blocks.IRON_BLOCK,
            Blocks.LAPIS_BLOCK,
            Blocks.NETHER_BRICKS,
            SoundEvents.ENCHANTMENT_TABLE_USE,
            SoundEvents.RESPAWN_ANCHOR_CHARGE,
            SoundEvents.BEACON_POWER_SELECT,
            ParticleTypes.ENCHANT,
            ParticleTypes.ELECTRIC_SPARK,
            ParticleTypes.END_ROD
    ),
    LEVEL_4(
            4,
            ModBlocks.MATERIA_TABLE_3,
            ModBlocks.MATERIA_TABLE_4,
            ModItems.UPGRADE_CORE_4,
            300_000L,
            Blocks.PURPUR_BLOCK,
            Blocks.END_STONE_BRICKS,
            Blocks.CRYING_OBSIDIAN,
            SoundEvents.END_PORTAL_FRAME_FILL,
            SoundEvents.AMETHYST_BLOCK_RESONATE,
            SoundEvents.END_PORTAL_SPAWN,
            ParticleTypes.REVERSE_PORTAL,
            ParticleTypes.DRAGON_BREATH,
            ParticleTypes.PORTAL
    );

    public static final int BLOCK_TICKS = 70;
    public static final int RITUAL_Y_OFFSET = -1;
    private static final List<RitualPosition> POSITIONS = List.of(
            new RitualPosition(-2, -2, Symbol.A),
            new RitualPosition(0, -2, Symbol.B),
            new RitualPosition(2, -2, Symbol.A),
            new RitualPosition(1, -1, Symbol.C),
            new RitualPosition(2, 0, Symbol.B),
            new RitualPosition(2, 2, Symbol.A),
            new RitualPosition(1, 1, Symbol.C),
            new RitualPosition(0, 2, Symbol.B),
            new RitualPosition(-2, 2, Symbol.A),
            new RitualPosition(-1, 1, Symbol.C),
            new RitualPosition(-2, 0, Symbol.B),
            new RitualPosition(-1, -1, Symbol.C)
    );

    private static final List<BlockPos> ESSENCE_POSITIONS = List.of(
            new BlockPos(-1, RITUAL_Y_OFFSET, -2),
            new BlockPos(0, RITUAL_Y_OFFSET, -1),
            new BlockPos(1, RITUAL_Y_OFFSET, -2),
            new BlockPos(2, RITUAL_Y_OFFSET, -1),
            new BlockPos(1, RITUAL_Y_OFFSET, 0),
            new BlockPos(2, RITUAL_Y_OFFSET, 1),
            new BlockPos(1, RITUAL_Y_OFFSET, 2),
            new BlockPos(0, RITUAL_Y_OFFSET, 1),
            new BlockPos(-1, RITUAL_Y_OFFSET, 2),
            new BlockPos(-2, RITUAL_Y_OFFSET, 1),
            new BlockPos(-1, RITUAL_Y_OFFSET, 0),
            new BlockPos(-2, RITUAL_Y_OFFSET, -1)
    );

    private final int targetTier;
    private final Supplier<? extends Block> sourceTable;
    private final Supplier<? extends Block> targetTable;
    private final Supplier<? extends Item> core;
    private final long essenceCost;
    private final Block blockA;
    private final Block blockB;
    private final Block blockC;
    private final SoundEvent activationSound;
    private final SoundEvent chargingSound;
    private final SoundEvent completionSound;
    private final List<ParticleOptions> ritualParticles;

    MateriaTableUpgrade(
            int targetTier,
            Supplier<? extends Block> sourceTable,
            Supplier<? extends Block> targetTable,
            Supplier<? extends Item> core,
            long essenceCost,
            Block blockA,
            Block blockB,
            Block blockC,
            SoundEvent activationSound,
            SoundEvent chargingSound,
            SoundEvent completionSound,
            ParticleOptions... ritualParticles
    ) {
        this.targetTier = targetTier;
        this.sourceTable = sourceTable;
        this.targetTable = targetTable;
        this.core = core;
        this.essenceCost = essenceCost;
        this.blockA = blockA;
        this.blockB = blockB;
        this.blockC = blockC;
        this.activationSound = activationSound;
        this.chargingSound = chargingSound;
        this.completionSound = completionSound;
        this.ritualParticles = List.of(ritualParticles);
    }

    public int targetTier() {
        return targetTier;
    }

    public Block sourceTable() {
        return sourceTable.get();
    }

    public Block targetTable() {
        return targetTable.get();
    }

    public Item core() {
        return core.get();
    }

    public long essenceCost() {
        return essenceCost;
    }

    public Block blockA() {
        return blockA;
    }

    public Block blockB() {
        return blockB;
    }

    public Block blockC() {
        return blockC;
    }

    public SoundEvent activationSound() {
        return activationSound;
    }

    public SoundEvent chargingSound() {
        return chargingSound;
    }

    public SoundEvent completionSound() {
        return completionSound;
    }

    public List<ParticleOptions> ritualParticles() {
        return ritualParticles;
    }

    public int blockCount() {
        return POSITIONS.size();
    }

    public BlockPos ritualBlockPos(BlockPos tablePos, int index) {
        RitualPosition position = POSITIONS.get(index);
        return tablePos.offset(position.x(), RITUAL_Y_OFFSET, position.z());
    }

    public Block expectedBlock(int index) {
        return switch (POSITIONS.get(index).symbol()) {
            case A -> blockA;
            case B -> blockB;
            case C -> blockC;
        };
    }

    public BlockPos ritualEssencePos(BlockPos tablePos, int index) {
        return tablePos.offset(ESSENCE_POSITIONS.get(index));
    }

    public boolean isEssencePosition(BlockPos tablePos, BlockPos candidate) {
        for (BlockPos offset : ESSENCE_POSITIONS) {
            if (tablePos.offset(offset).equals(candidate)) {
                return true;
            }
        }
        return false;
    }

    public boolean matchesStructure(Level level, BlockPos tablePos) {
        for (int index = 0; index < POSITIONS.size(); index++) {
            if (!level.getBlockState(ritualBlockPos(tablePos, index)).is(expectedBlock(index))) {
                return false;
            }
        }
        for (BlockPos offset : ESSENCE_POSITIONS) {
            var fluid = level.getFluidState(tablePos.offset(offset));
            if (!fluid.is(ModFluids.LIQUID_ESSENCE.get()) || !fluid.isSource()) {
                return false;
            }
        }
        return true;
    }

    public boolean isStabilizingMaterial(BlockState state) {
        return state.is(blockC);
    }

    public static boolean hasStabilizingRitualBlock(LevelReader level, BlockPos liquidPos) {
        for (MateriaTableUpgrade upgrade : values()) {
            for (Direction direction : Direction.values()) {
                if (upgrade.isStabilizingMaterial(level.getBlockState(liquidPos.relative(direction)))) {
                    return true;
                }
            }
        }
        return false;
    }
    public static Optional<MateriaTableUpgrade> forSource(Block block) {
        for (MateriaTableUpgrade upgrade : values()) {
            if (upgrade.sourceTable() == block) {
                return Optional.of(upgrade);
            }
        }
        return Optional.empty();
    }

    public static Optional<MateriaTableUpgrade> byTargetTier(int tier) {
        for (MateriaTableUpgrade upgrade : values()) {
            if (upgrade.targetTier == tier) {
                return Optional.of(upgrade);
            }
        }
        return Optional.empty();
    }

    public static boolean isUpgradeCore(ItemStack stack) {
        for (MateriaTableUpgrade upgrade : values()) {
            if (stack.is(upgrade.core())) {
                return true;
            }
        }
        return false;
    }

    private enum Symbol {
        A,
        B,
        C
    }

    private record RitualPosition(int x, int z, Symbol symbol) {
    }
}
