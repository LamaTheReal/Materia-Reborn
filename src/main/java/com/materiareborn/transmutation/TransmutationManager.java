package com.materiareborn.transmutation;

import com.materiareborn.api.item.ItemKnowledge;
import com.materiareborn.api.knowledge.KnowledgeSubject;
import com.materiareborn.api.transmutation.TransmutationQuote;
import com.materiareborn.api.transmutation.TransmutationRequest;
import com.materiareborn.essence.EssenceManager;
import com.materiareborn.knowledge.ItemKnowledgeManager;

import java.util.Objects;

public final class TransmutationManager {
    private final EssenceManager essenceManager;
    private final ItemKnowledgeManager knowledgeManager;

    public TransmutationManager(EssenceManager essenceManager, ItemKnowledgeManager knowledgeManager) {
        this.essenceManager = Objects.requireNonNull(essenceManager, "essenceManager");
        this.knowledgeManager = Objects.requireNonNull(knowledgeManager, "knowledgeManager");
    }

    public TransmutationQuote quoteSynthesis(KnowledgeSubject subject, TransmutationRequest request) {
        ItemKnowledge knowledge = knowledgeManager.get(subject, request.itemKey());
        if (!knowledge.isUnlocked()) {
            return TransmutationQuote.unavailable(request, "item_not_unlocked");
        }

        return essenceManager.quotePurchaseCost(request.identity(), request.amount())
                .map(cost -> TransmutationQuote.available(request, cost))
                .orElseGet(() -> TransmutationQuote.unavailable(request, "missing_essence_value"));
    }
}
