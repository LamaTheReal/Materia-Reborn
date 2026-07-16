package com.materiareborn.client;

import com.materiareborn.client.particle.LiquidEssenceSurfaceParticle;
import com.materiareborn.client.particle.MagicGlyphParticle;
import com.materiareborn.client.screen.MateriaTableScreen;
import com.materiareborn.client.screen.config.MateriaConfigHomeScreen;
import com.materiareborn.core.ModConstants;
import com.materiareborn.registry.ModMenuTypes;
import com.materiareborn.registry.ModFluids;
import com.materiareborn.registry.ModParticles;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

@EventBusSubscriber(modid = ModConstants.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class MateriaClient {
    private static final ResourceLocation WATER_STILL = ResourceLocation.withDefaultNamespace("block/water_still");
    private static final ResourceLocation WATER_FLOW = ResourceLocation.withDefaultNamespace("block/water_flow");

    private MateriaClient() {
    }
    public static void registerConfigScreen(ModContainer modContainer) {
        modContainer.registerExtensionPoint(
                IConfigScreenFactory.class,
                (container, parent) -> new MateriaConfigHomeScreen(modContainer, parent)
        );
    }

    @SubscribeEvent
    public static void registerParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.MAGIC_GLYPH.get(), MagicGlyphParticle.Provider::new);
        event.registerSpriteSet(ModParticles.LIQUID_ESSENCE_GLYPH.get(), LiquidEssenceSurfaceParticle.Provider::new);
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.MATERIA_TABLE.get(), MateriaTableScreen::new);
    }

    @SubscribeEvent
    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {
                return WATER_STILL;
            }

            @Override
            public ResourceLocation getFlowingTexture() {
                return WATER_FLOW;
            }

            @Override
            public int getTintColor() {
                return 0xD08A28D7;
            }
        }, ModFluids.LIQUID_ESSENCE_TYPE);
    }
}