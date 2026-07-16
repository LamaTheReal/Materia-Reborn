package com.materiareborn;

import com.materiareborn.client.MateriaClient;
import com.materiareborn.command.MateriaCommands;
import com.materiareborn.config.EssenceItemConfigFiles;
import com.materiareborn.config.MateriaConfig;
import com.materiareborn.compat.jer.MateriaJerIntegration;
import com.materiareborn.core.MateriaRuntime;
import com.materiareborn.event.EssenceCondensationEvents;
import com.materiareborn.event.EssenceCrystalActivationEvents;
import com.materiareborn.event.MateriaItemTooltipEvents;
import com.materiareborn.event.MateriaSoulboundEvents;
import com.materiareborn.event.MateriaTableRitualEvents;
import com.materiareborn.event.LiquidEssenceBrewingEvents;
import com.materiareborn.ritual.MateriaTableRitualBuildProcess;
import com.materiareborn.core.ModConstants;
import com.materiareborn.registry.ModRegistries;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

@Mod(ModConstants.MOD_ID)
public final class MateriaReborn {
    public static final Logger LOGGER = LogUtils.getLogger();

    private final MateriaRuntime runtime;

    public MateriaReborn(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, MateriaConfig.SPEC, ModConstants.MOD_ID + "/gameplay.toml" );
        EssenceItemConfigFiles.initialize();
        if (FMLEnvironment.dist.isClient()) {
            MateriaClient.registerConfigScreen(modContainer);
        }
        ModRegistries.register(modEventBus);
        NeoForge.EVENT_BUS.addListener(MateriaCommands::register);
        NeoForge.EVENT_BUS.addListener(EssenceCrystalActivationEvents::onRightClickItem);
        NeoForge.EVENT_BUS.addListener(EssenceCondensationEvents::onRightClickBlock);
        NeoForge.EVENT_BUS.addListener(EssenceCondensationEvents::onLevelTick);
        NeoForge.EVENT_BUS.addListener(MateriaTableRitualEvents::onRightClickBlock);
        NeoForge.EVENT_BUS.addListener(LiquidEssenceBrewingEvents::onItemTick);
        NeoForge.EVENT_BUS.addListener(LiquidEssenceBrewingEvents::onLevelTick);
        NeoForge.EVENT_BUS.addListener(MateriaTableRitualBuildProcess::onLevelTick);
        NeoForge.EVENT_BUS.addListener(MateriaSoulboundEvents::onLivingDrops);
        NeoForge.EVENT_BUS.addListener(MateriaSoulboundEvents::onPlayerClone);
        NeoForge.EVENT_BUS.addListener(MateriaItemTooltipEvents::onItemTooltip);
        this.runtime = MateriaRuntime.bootstrap();

        modEventBus.addListener(this::onCommonSetup);
        LOGGER.info("{} initialized as an expandable foundation.", ModConstants.MOD_NAME);
    }

    public MateriaRuntime runtime() {
        return runtime;
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            runtime.validateBootstrap();

            if (FMLEnvironment.dist.isClient() && ModList.get().isLoaded("jeresources")) {
                MateriaJerIntegration.registerWorldGeneration();
            }
        });
    }
}
