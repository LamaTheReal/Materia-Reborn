package com.materiareborn.api.transmutation;

import com.materiareborn.api.essence.EssenceAmount;

import java.util.Objects;

public record TransmutationQuote(
        TransmutationRequest request,
        EssenceAmount cost,
        boolean available,
        String reason
) {
    public TransmutationQuote {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(cost, "cost");
        Objects.requireNonNull(reason, "reason");
    }

    public static TransmutationQuote available(TransmutationRequest request, EssenceAmount cost) {
        return new TransmutationQuote(request, cost, true, "available");
    }

    public static TransmutationQuote unavailable(TransmutationRequest request, String reason) {
        return new TransmutationQuote(request, EssenceAmount.ZERO, false, reason);
    }
}
