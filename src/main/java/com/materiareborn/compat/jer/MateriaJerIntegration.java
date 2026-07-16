package com.materiareborn.compat.jer;

import com.materiareborn.registry.ModBlocks;
import com.materiareborn.registry.ModItems;
import jeresources.api.IJERAPI;
import jeresources.api.distributions.DistributionSquare;
import jeresources.api.drop.LootDrop;
import jeresources.api.restrictions.Restriction;
import jeresources.compatibility.api.JERAPI;
import net.minecraft.world.item.ItemStack;

/** Registers the hand-authored distribution used by the custom Y=0 ore feature. */
public final class MateriaJerIntegration {
    private static boolean registered;

    private MateriaJerIntegration() {
    }

    public static void registerWorldGeneration() {
        if (registered) {
            return;
        }

        IJERAPI jerApi = JERAPI.getInstance();
        jerApi.getWorldGenRegistry().register(
                new ItemStack(ModBlocks.DEEPSLATE_ESSENCE.get()),
                new DistributionSquare(0, 0, 0.70F),
                Restriction.OVERWORLD,
                new LootDrop(new ItemStack(ModItems.ESSENCE_CRYSTAL.get()), 1, 1)
        );
        registered = true;
    }
}