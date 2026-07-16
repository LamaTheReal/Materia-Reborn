package com.materiareborn.registry;

import com.materiareborn.core.ModConstants;
import com.materiareborn.item.EssenceCrystalItem;
import com.materiareborn.item.EssenceItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, ModConstants.MOD_ID);

    public static final DeferredHolder<Item, BlockItem> MATERIA_TABLE = ITEMS.register(
            "materia_table",
            () -> new BlockItem(ModBlocks.MATERIA_TABLE.get(), new Item.Properties())
    );

    public static final DeferredHolder<Item, BlockItem> MATERIA_TABLE_2 = ITEMS.register(
            "materia_table_2",
            () -> new BlockItem(ModBlocks.MATERIA_TABLE_2.get(), new Item.Properties())
    );

    public static final DeferredHolder<Item, BlockItem> MATERIA_TABLE_3 = ITEMS.register(
            "materia_table_3",
            () -> new BlockItem(ModBlocks.MATERIA_TABLE_3.get(), new Item.Properties())
    );

    public static final DeferredHolder<Item, BlockItem> MATERIA_TABLE_4 = ITEMS.register(
            "materia_table_4",
            () -> new BlockItem(ModBlocks.MATERIA_TABLE_4.get(), new Item.Properties())
    );

    public static final DeferredHolder<Item, BlockItem> DEEPSLATE_ESSENCE = ITEMS.register(
            "deepslate_essence",
            () -> new BlockItem(ModBlocks.DEEPSLATE_ESSENCE.get(), new Item.Properties())
    );

    public static final DeferredHolder<Item, BlockItem> LIQUID_ESSENCE = ITEMS.register(
            "liquid_essence",
            () -> new BlockItem(ModBlocks.LIQUID_ESSENCE.get(), new Item.Properties())
    );

    public static final DeferredHolder<Item, EssenceCrystalItem> ESSENCE_CRYSTAL = ITEMS.register(
            "essence_crystal",
            () -> new EssenceCrystalItem(new Item.Properties())
    );

    public static final DeferredHolder<Item, EssenceItem> ESSENCE = ITEMS.register(
            "essence",
            () -> new EssenceItem(new Item.Properties())
    );

    public static final DeferredHolder<Item, Item> ESSENCE_DUST = ITEMS.register(
            "essence_dust",
            () -> new Item(new Item.Properties())
    );

    public static final DeferredHolder<Item, Item> ESSENCE_CORE = ITEMS.register(
            "essence_core",
            () -> new Item(new Item.Properties())
    );

    public static final DeferredHolder<Item, Item> UPGRADE_CORE_2 = ITEMS.register(
            "upgrade_core_2",
            () -> new Item(new Item.Properties())
    );

    public static final DeferredHolder<Item, Item> UPGRADE_CORE_3 = ITEMS.register(
            "upgrade_core_3",
            () -> new Item(new Item.Properties())
    );

    public static final DeferredHolder<Item, Item> UPGRADE_CORE_4 = ITEMS.register(
            "upgrade_core_4",
            () -> new Item(new Item.Properties())
    );

    public static final DeferredHolder<Item, Item> ESSENCE_PLATE = ITEMS.register(
            "essence_plate",
            () -> new Item(new Item.Properties())
    );

    private ModItems() {
    }
}