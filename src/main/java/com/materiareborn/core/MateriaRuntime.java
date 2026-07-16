package com.materiareborn.core;

import com.materiareborn.crafting.CraftingValueAuditor;
import com.materiareborn.essence.EssenceItemCatalog;
import com.materiareborn.essence.EssenceManager;
import com.materiareborn.essence.InMemoryEssenceCatalog;
import com.materiareborn.item.IdentityComponentRegistry;
import com.materiareborn.item.ItemStackIdentityResolver;
import com.materiareborn.knowledge.InMemoryItemKnowledgeRepository;
import com.materiareborn.knowledge.ItemKnowledgeManager;
import com.materiareborn.mapping.EssenceMappingManager;
import com.materiareborn.nbt.NbtValuePolicy;
import com.materiareborn.sync.NoopSyncGateway;
import com.materiareborn.table.TableTierManager;
import com.materiareborn.transmutation.TransmutationManager;
import com.materiareborn.unlock.DeferredUnlockPolicy;

import java.util.Objects;

public final class MateriaRuntime {
    private final MateriaServices services;

    private MateriaRuntime(MateriaServices services) {
        this.services = Objects.requireNonNull(services, "services");
    }

    public static MateriaRuntime bootstrap() {
        NoopSyncGateway syncGateway = NoopSyncGateway.INSTANCE;
        IdentityComponentRegistry componentRegistry = new IdentityComponentRegistry();
        ItemStackIdentityResolver itemIdentityResolver = new ItemStackIdentityResolver(componentRegistry);
        InMemoryEssenceCatalog essenceCatalog = new InMemoryEssenceCatalog();
        EssenceMappingManager essenceMappingManager = new EssenceMappingManager(essenceCatalog, syncGateway);
        essenceMappingManager.registerSource(EssenceItemCatalog::collectMappings);
        EssenceManager essenceManager = new EssenceManager(essenceCatalog);
        ItemKnowledgeManager knowledgeManager = new ItemKnowledgeManager(
                new InMemoryItemKnowledgeRepository(),
                new DeferredUnlockPolicy(),
                essenceCatalog,
                syncGateway
        );
        TableTierManager tableTierManager = TableTierManager.defaultTiers();
        TransmutationManager transmutationManager = new TransmutationManager(essenceManager, knowledgeManager);
        CraftingValueAuditor craftingValueAuditor = new CraftingValueAuditor(essenceManager);
        NbtValuePolicy nbtValuePolicy = NbtValuePolicy.deferred();

        return new MateriaRuntime(new MateriaServices(
                syncGateway,
                componentRegistry,
                itemIdentityResolver,
                essenceCatalog,
                essenceMappingManager,
                essenceManager,
                knowledgeManager,
                tableTierManager,
                transmutationManager,
                craftingValueAuditor,
                nbtValuePolicy
        ));
    }

    public MateriaServices services() {
        return services;
    }

    public void validateBootstrap() {
        services.tableTierManager().requireConfigured();
        services.essenceMappingManager().rebuild();
    }
}
