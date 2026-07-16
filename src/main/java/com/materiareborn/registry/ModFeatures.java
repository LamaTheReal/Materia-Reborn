package com.materiareborn.registry;

import com.materiareborn.core.ModConstants;
import com.materiareborn.worldgen.DeepslateEssenceOreFeature;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, ModConstants.MOD_ID);

    public static final DeferredHolder<Feature<?>, DeepslateEssenceOreFeature> DEEPSLATE_ESSENCE_ORE = FEATURES.register(
            "deepslate_essence_ore",
            DeepslateEssenceOreFeature::new
    );

    private ModFeatures() {
    }
}