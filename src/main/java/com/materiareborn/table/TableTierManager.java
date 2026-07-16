package com.materiareborn.table;

import com.materiareborn.api.table.TableTier;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public final class TableTierManager {
    private final List<TableTier> tiers;

    private TableTierManager(List<TableTier> tiers) {
        this.tiers = List.copyOf(tiers);
    }

    public static TableTierManager defaultTiers() {
        return new TableTierManager(Arrays.stream(TableTier.values())
                .sorted(Comparator.comparingInt(TableTier::rank))
                .toList());
    }

    public List<TableTier> tiers() {
        return tiers;
    }

    public boolean canAccess(TableTier currentTier, TableTier requiredTier) {
        return currentTier.supports(requiredTier);
    }

    public void requireConfigured() {
        if (tiers.isEmpty()) {
            throw new IllegalStateException("At least one table tier must be configured.");
        }
    }
}
