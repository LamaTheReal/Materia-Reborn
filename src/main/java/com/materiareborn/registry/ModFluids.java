package com.materiareborn.registry;

import com.materiareborn.core.ModConstants;
import com.materiareborn.fluid.LiquidEssenceFluid;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class ModFluids {
    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, ModConstants.MOD_ID);
    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(Registries.FLUID, ModConstants.MOD_ID);

    public static final DeferredHolder<FluidType, FluidType> LIQUID_ESSENCE_TYPE = FLUID_TYPES.register(
            "liquid_essence",
            () -> new FluidType(FluidType.Properties.create()
                    .descriptionId("fluid.materia_reborn.liquid_essence")
                    .lightLevel(2)
                    .density(900)
                    .viscosity(1_600)
                    .motionScale(0.0D)
                    .canPushEntity(false)
                    .canSwim(false)
                    .canDrown(false)
                    .fallDistanceModifier(0.0F))
    );

    public static final DeferredHolder<Fluid, LiquidEssenceFluid.Source> LIQUID_ESSENCE = FLUIDS.register(
            "liquid_essence",
            () -> new LiquidEssenceFluid.Source(properties())
    );

    public static final DeferredHolder<Fluid, LiquidEssenceFluid.Flowing> FLOWING_LIQUID_ESSENCE = FLUIDS.register(
            "flowing_liquid_essence",
            () -> new LiquidEssenceFluid.Flowing(properties())
    );

    private static BaseFlowingFluid.Properties properties() {
        return new BaseFlowingFluid.Properties(
                LIQUID_ESSENCE_TYPE,
                LIQUID_ESSENCE,
                FLOWING_LIQUID_ESSENCE
        ).block(ModBlocks.LIQUID_ESSENCE)
                .slopeFindDistance(0)
                .levelDecreasePerBlock(8)
                .tickRate(20)
                .explosionResistance(100.0F);
    }

    private ModFluids() {
    }
}
