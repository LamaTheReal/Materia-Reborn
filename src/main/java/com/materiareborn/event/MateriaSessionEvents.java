package com.materiareborn.event;

import com.materiareborn.essence.PlayerEssenceSessionHistory;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public final class MateriaSessionEvents {
    private MateriaSessionEvents() {
    }

    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        PlayerEssenceSessionHistory.clear(event.getEntity());
    }

    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        PlayerEssenceSessionHistory.clear(event.getEntity());
    }
}
