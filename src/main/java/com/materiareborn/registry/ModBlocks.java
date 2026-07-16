package com.materiareborn.registry;

import com.materiareborn.block.DeepslateEssenceBlock;
import com.materiareborn.block.LiquidEssenceBlock;
import com.materiareborn.block.MateriaTableBlock;
import com.materiareborn.core.ModConstants;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, ModConstants.MOD_ID);

    public static final DeferredHolder<Block, MateriaTableBlock> MATERIA_TABLE = BLOCKS.register(
            "materia_table",
            () -> new MateriaTableBlock(materiaTableProperties())
    );

    public static final DeferredHolder<Block, MateriaTableBlock> MATERIA_TABLE_2 = BLOCKS.register(
            "materia_table_2",
            () -> new MateriaTableBlock(materiaTableProperties())
    );

    public static final DeferredHolder<Block, MateriaTableBlock> MATERIA_TABLE_3 = BLOCKS.register(
            "materia_table_3",
            () -> new MateriaTableBlock(materiaTableProperties())
    );

    public static final DeferredHolder<Block, MateriaTableBlock> MATERIA_TABLE_4 = BLOCKS.register(
            "materia_table_4",
            () -> new MateriaTableBlock(materiaTableProperties())
    );

    public static final DeferredHolder<Block, DeepslateEssenceBlock> DEEPSLATE_ESSENCE = BLOCKS.register(
            "deepslate_essence",
            () -> new DeepslateEssenceBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.DEEPSLATE)
                    .lightLevel(state -> 3)
                    .requiresCorrectToolForDrops()
                    .strength(33.6F, 1200.0F)
                    .sound(SoundType.DEEPSLATE))
    );

    public static final DeferredHolder<Block, LiquidEssenceBlock> LIQUID_ESSENCE = BLOCKS.register(
            "liquid_essence",
            () -> new LiquidEssenceBlock(
                    ModFluids.LIQUID_ESSENCE.get(),
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_PURPLE)
                            .replaceable()
                            .noCollission()
                            .strength(100.0F)
                            .lightLevel(state -> 2)
                            .pushReaction(PushReaction.DESTROY)
                            .noLootTable()
                            .liquid()
                            .sound(SoundType.EMPTY)
            )
    );

    private static BlockBehaviour.Properties materiaTableProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_PURPLE)
                .strength(2.5F)
                .sound(SoundType.WOOD)
                .noOcclusion();
    }

    private ModBlocks() {
    }
}