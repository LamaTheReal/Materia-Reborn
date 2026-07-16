package com.materiareborn.api.transmutation;

import java.util.List;
import java.util.Objects;

public record TransmutationViewState(
        TransmutationQuery query,
        List<TransmutationQuote> quotes,
        boolean hasPreviousPage,
        boolean hasNextPage
) {
    public TransmutationViewState {
        Objects.requireNonNull(query, "query");
        quotes = List.copyOf(Objects.requireNonNull(quotes, "quotes"));
    }

    public static TransmutationViewState empty(TransmutationQuery query) {
        return new TransmutationViewState(query, List.of(), false, false);
    }
}
