package com.materiareborn.registry;

import com.materiareborn.blockentity.MateriaTableBlockEntity;
import com.materiareborn.core.ModConstants;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlockEntityTypes {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ModConstants.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MateriaTableBlockEntity>> MATERIA_TABLE =
            BLOCK_ENTITY_TYPES.register("materia_table", () -> BlockEntityType.Builder.of(
                    MateriaTableBlockEntity::new,
                    ModBlocks.MATERIA_TABLE.get(),
                    ModBlocks.MATERIA_TABLE_2.get(),
                    ModBlocks.MATERIA_TABLE_3.get(),
                    ModBlocks.MATERIA_TABLE_4.get()
            ).build(null));

    private ModBlockEntityTypes() {
    }
}