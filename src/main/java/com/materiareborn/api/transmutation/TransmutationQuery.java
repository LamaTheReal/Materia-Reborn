package com.materiareborn.api.transmutation;

import com.materiareborn.api.table.TableTier;

import java.util.Objects;

public record TransmutationQuery(String searchText, TransmutationTab tab, TableTier tableTier, int page) {
    public TransmutationQuery {
        searchText = Objects.requireNonNullElse(searchText, "");
        Objects.requireNonNull(tab, "tab");
        Objects.requireNonNull(tableTier, "tableTier");
        if (page < 0) {
            throw new IllegalArgumentException("Page cannot be negative.");
        }
    }

    public static TransmutationQuery firstPage(TransmutationTab tab, TableTier tableTier) {
        return new TransmutationQuery("", tab, tableTier, 0);
    }
}
