package com.materiareborn.registry;

import com.materiareborn.core.ModConstants;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(Registries.SOUND_EVENT, ModConstants.MOD_ID);

    public static final DeferredHolder<SoundEvent, SoundEvent> ESSENCE_CONDENSATION = SOUND_EVENTS.register(
            "essence_condensation",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(
                    ModConstants.MOD_ID,
                    "essence_condensation"
            ))
    );

    private ModSounds() {
    }
}