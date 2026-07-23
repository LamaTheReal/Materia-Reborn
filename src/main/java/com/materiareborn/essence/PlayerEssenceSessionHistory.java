package com.materiareborn.essence;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.world.entity.player.Player;

public final class PlayerEssenceSessionHistory {
    private static final int EMPTY_INDEX = -1;
    private static final Map<UUID, RecentItems> RECENT_ITEMS = new ConcurrentHashMap<>();

    private PlayerEssenceSessionHistory() {
    }

    public static int lastSoldIndex(Player player) {
        return RECENT_ITEMS.getOrDefault(player.getUUID(), RecentItems.EMPTY).lastSoldIndex();
    }

    public static int lastPurchasedIndex(Player player) {
        return RECENT_ITEMS.getOrDefault(player.getUUID(), RecentItems.EMPTY).lastPurchasedIndex();
    }

    public static void recordSold(Player player, int catalogIndex) {
        if (!isValidCatalogIndex(catalogIndex)) {
            return;
        }
        RECENT_ITEMS.compute(
                player.getUUID(),
                (uuid, current) -> new RecentItems(
                        catalogIndex,
                        current == null ? EMPTY_INDEX : current.lastPurchasedIndex()
                )
        );
    }

    public static void recordPurchased(Player player, int catalogIndex) {
        if (!isValidCatalogIndex(catalogIndex)) {
            return;
        }
        RECENT_ITEMS.compute(
                player.getUUID(),
                (uuid, current) -> new RecentItems(
                        current == null ? EMPTY_INDEX : current.lastSoldIndex(),
                        catalogIndex
                )
        );
    }

    public static void clear(Player player) {
        RECENT_ITEMS.remove(player.getUUID());
    }

    private static boolean isValidCatalogIndex(int catalogIndex) {
        return catalogIndex >= 0 && catalogIndex < EssenceItemCatalog.size();
    }

    private record RecentItems(int lastSoldIndex, int lastPurchasedIndex) {
        private static final RecentItems EMPTY = new RecentItems(EMPTY_INDEX, EMPTY_INDEX);
    }
}
