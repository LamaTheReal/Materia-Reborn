package com.materiareborn.essence;

import com.materiareborn.api.mapping.EssenceMappingCollector;
import com.materiareborn.config.EssenceItemConfigFiles;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class EssenceItemCatalog {
    private static volatile CatalogData data = loadData();

    private EssenceItemCatalog() {
    }

    public static int size() {
        return data.definitions().size();
    }

    public static List<EssenceItemDefinition> definitions() {
        return data.definitions();
    }

    public static EssenceItemDefinition get(int index) {
        List<EssenceItemDefinition> definitions = data.definitions();
        if (index < 0 || index >= definitions.size()) {
            throw new IndexOutOfBoundsException("Unknown Essence item index: " + index);
        }
        return definitions.get(index);
    }

    public static Optional<EssenceItemDefinition> find(ItemStack stack) {
        int index = indexOf(stack);
        return index < 0 ? Optional.empty() : Optional.of(data.definitions().get(index));
    }

    public static Optional<EssenceItemDefinition> find(Item item) {
        CatalogData snapshot = data;
        Integer index = snapshot.plainIndexByItem().get(item);
        return index == null ? Optional.empty() : Optional.of(snapshot.definitions().get(index));
    }

    public static int indexOf(ItemStack stack) {
        if (stack.isEmpty()) {
            return -1;
        }
        CatalogData snapshot = data;
        List<EssenceItemDefinition> definitions = snapshot.definitions();
        List<Integer> candidates = snapshot.indicesByItem().getOrDefault(stack.getItem(), List.of());
        for (int index : candidates) {
            EssenceItemDefinition definition = definitions.get(index);
            if (!definition.variant().isEmpty() && definition.matches(stack)) {
                return index;
            }
        }
        for (int index : candidates) {
            EssenceItemDefinition definition = definitions.get(index);
            if (definition.variant().isEmpty() && definition.matches(stack)) {
                return index;
            }
        }
        return -1;
    }

    public static int indexOf(Item item) {
        return data.plainIndexByItem().getOrDefault(item, -1);
    }

    public static int indexOf(String itemKey) {
        return data.indexByKey().getOrDefault(itemKey, -1);
    }

    public static void collectMappings(EssenceMappingCollector collector) {
        for (EssenceItemDefinition definition : data.definitions()) {
            collector.setFixedValue(definition.identity(), definition.mappingValue());
        }
    }

    public static synchronized void reload() {
        data = loadData();
    }

    private static CatalogData loadData() {
        List<EssenceItemDefinition> definitions = loadDefinitions();
        return new CatalogData(
                definitions,
                createItemIndices(definitions),
                createPlainItemIndex(definitions),
                createKeyIndex(definitions)
        );
    }

    private static List<EssenceItemDefinition> loadDefinitions() {
        List<EssenceItemDefinition> result = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (String resource : EssenceItemConfigFiles.CATALOG_RESOURCES) {
            for (EssenceItemDefinition definition : EssenceItemCatalogLoader.load(resource)) {
                if (!seen.add(definition.catalogId())) {
                    throw new IllegalStateException("Duplicate Essence item " + definition.catalogId());
                }
                result.add(definition);
            }
        }
        return List.copyOf(result);
    }

    private static Map<Item, List<Integer>> createItemIndices(List<EssenceItemDefinition> definitions) {
        Map<Item, List<Integer>> result = new IdentityHashMap<>();
        for (int index = 0; index < definitions.size(); index++) {
            result.computeIfAbsent(definitions.get(index).item(), ignored -> new ArrayList<>()).add(index);
        }
        result.replaceAll((ignored, indices) -> List.copyOf(indices));
        return Map.copyOf(result);
    }

    private static Map<Item, Integer> createPlainItemIndex(List<EssenceItemDefinition> definitions) {
        Map<Item, Integer> result = new IdentityHashMap<>();
        for (int index = 0; index < definitions.size(); index++) {
            EssenceItemDefinition definition = definitions.get(index);
            if (definition.variant().isEmpty()) {
                result.put(definition.item(), index);
            }
        }
        return result;
    }

    private static Map<String, Integer> createKeyIndex(List<EssenceItemDefinition> definitions) {
        Map<String, Integer> result = new HashMap<>();
        for (int index = 0; index < definitions.size(); index++) {
            result.put(definitions.get(index).catalogId(), index);
        }
        return Map.copyOf(result);
    }

    private record CatalogData(
            List<EssenceItemDefinition> definitions,
            Map<Item, List<Integer>> indicesByItem,
            Map<Item, Integer> plainIndexByItem,
            Map<String, Integer> indexByKey
    ) {
    }
}
