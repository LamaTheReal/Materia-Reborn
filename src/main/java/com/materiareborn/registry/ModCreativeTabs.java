package com.materiareborn.registry;

import com.materiareborn.core.ModConstants;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ModConstants.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MATERIA_REBORN =
            CREATIVE_TABS.register("materia_reborn", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.materia_reborn"))
                    .icon(() -> new ItemStack(ModItems.MATERIA_TABLE.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.MATERIA_TABLE.get());
                        output.accept(ModItems.MATERIA_TABLE_2.get());
                        output.accept(ModItems.MATERIA_TABLE_3.get());
                        output.accept(ModItems.MATERIA_TABLE_4.get());
                        output.accept(ModItems.DEEPSLATE_ESSENCE.get());
                        output.accept(ModItems.LIQUID_ESSENCE.get());
                        output.accept(ModItems.ESSENCE.get());
                        output.accept(ModItems.ESSENCE_DUST.get());
                        output.accept(ModItems.ESSENCE_CRYSTAL.get());
                        output.accept(ModItems.ESSENCE_CORE.get());
                        output.accept(ModItems.UPGRADE_CORE_2.get());
                        output.accept(ModItems.UPGRADE_CORE_3.get());
                        output.accept(ModItems.UPGRADE_CORE_4.get());
                        output.accept(ModItems.ESSENCE_PLATE.get());
                    })
                    .build());

    private ModCreativeTabs() {
    }
}