package com.materiareborn.network;

import com.materiareborn.menu.MateriaTableMenu;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class MateriaPayloads {
    private static final String NETWORK_VERSION = "1";

    private MateriaPayloads() {
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar(NETWORK_VERSION).playToServer(
                DirectCraftPayload.TYPE,
                DirectCraftPayload.STREAM_CODEC,
                MateriaPayloads::handleDirectCraft
        );
    }

    private static void handleDirectCraft(
            DirectCraftPayload payload,
            net.neoforged.neoforge.network.handling.IPayloadContext context
    ) {
        if (context.player() instanceof ServerPlayer player
                && player.containerMenu instanceof MateriaTableMenu menu) {
            menu.directCraft(player, payload.recipeId());
        }
    }
}
