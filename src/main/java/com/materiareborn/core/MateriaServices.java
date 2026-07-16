package com.materiareborn.core;

import com.materiareborn.crafting.CraftingValueAuditor;
import com.materiareborn.api.sync.SyncGateway;
import com.materiareborn.essence.EssenceManager;
import com.materiareborn.essence.InMemoryEssenceCatalog;
import com.materiareborn.item.IdentityComponentRegistry;
import com.materiareborn.item.ItemStackIdentityResolver;
import com.materiareborn.knowledge.ItemKnowledgeManager;
import com.materiareborn.mapping.EssenceMappingManager;
import com.materiareborn.nbt.NbtValuePolicy;
import com.materiareborn.table.TableTierManager;
import com.materiareborn.transmutation.TransmutationManager;

public record MateriaServices(
        SyncGateway syncGateway,
        IdentityComponentRegistry componentRegistry,
        ItemStackIdentityResolver itemIdentityResolver,
        InMemoryEssenceCatalog essenceCatalog,
        EssenceMappingManager essenceMappingManager,
        EssenceManager essenceManager,
        ItemKnowledgeManager knowledgeManager,
        TableTierManager tableTierManager,
        TransmutationManager transmutationManager,
        CraftingValueAuditor craftingValueAuditor,
        NbtValuePolicy nbtValuePolicy
) {
}
