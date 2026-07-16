package com.materiareborn.essence;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.materiareborn.config.EssenceItemConfigFiles;
import com.materiareborn.api.essence.EssenceTier;
import com.materiareborn.api.item.ItemKey;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

final class EssenceItemCatalogLoader {
    private static final int SUPPORTED_SCHEMA_VERSION = 1;

    private EssenceItemCatalogLoader() {
    }

    static List<EssenceItemDefinition> load(String resourcePath) {
        JsonObject root = readRoot(resourcePath);
        int schemaVersion = requiredInt(root, "schema_version", resourcePath);
        if (schemaVersion != SUPPORTED_SCHEMA_VERSION) {
            throw invalid(resourcePath, "Unsupported schema_version " + schemaVersion);
        }

        int tableLevel = requiredInt(root, "table_level", resourcePath);
        if (tableLevel < 1 || tableLevel > 4) {
            throw invalid(resourcePath, "table_level must be between 1 and 4");
        }

        JsonArray categories = requiredArray(root, "categories", resourcePath);
        List<EssenceItemDefinition> definitions = new ArrayList<>();
        Set<String> seenItems = new HashSet<>();
        for (JsonElement categoryElement : categories) {
            JsonObject category = requiredObject(categoryElement, resourcePath, "category");
            String categoryId = requiredString(category, "id", resourcePath);
            JsonArray items = requiredArray(category, "items", resourcePath);
            for (JsonElement itemElement : items) {
                JsonObject itemData = requiredObject(itemElement, resourcePath, "item");
                if (itemData.has("enabled") && !itemData.get("enabled").getAsBoolean()) {
                    continue;
                }
                EssenceItemDefinition definition = parseDefinition(
                        itemData,
                        categoryId,
                        tableLevel,
                        resourcePath
                );
                if (!seenItems.add(definition.catalogId())) {
                    throw invalid(resourcePath, "Duplicate item " + definition.catalogId());
                }
                definitions.add(definition);
            }
        }


        return List.copyOf(definitions);
    }

    private static EssenceItemDefinition parseDefinition(
            JsonObject data,
            String category,
            int tableLevel,
            String resourcePath
    ) {
        String itemId = requiredString(data, "id", resourcePath);
        ParsedItem parsedItem = parseItem(itemId, resourcePath);
        ResourceLocation location = parsedItem.location();
        if (location == null || !BuiltInRegistries.ITEM.containsKey(location)) {
            throw invalid(resourcePath, "Unknown item id " + itemId);
        }

        ItemKey itemKey = ItemKey.of(location.getNamespace(), location.getPath());
        Item item = BuiltInRegistries.ITEM.get(location);
        EssenceTier tier = parseTier(requiredString(data, "tier", resourcePath), resourcePath);
        int requiredAnalysis = requiredInt(data, "analysis", resourcePath);
        long baseValue = requiredLong(data, "base", resourcePath);
        long sellValue = requiredLong(data, "sell", resourcePath);
        long purchaseCost = requiredLong(data, "buy", resourcePath);

        return new EssenceItemDefinition(
                itemKey,
                item,
                parsedItem.variant(),
                tier,
                tableLevel,
                category,
                baseValue,
                sellValue,
                purchaseCost,
                requiredAnalysis
        );
    }

    private static ParsedItem parseItem(String specification, String resourcePath) {
        int componentStart = specification.indexOf('[');
        if (componentStart < 0) {
            return new ParsedItem(ResourceLocation.tryParse(specification), EssenceItemVariant.NONE);
        }
        if (!specification.endsWith("]") || specification.indexOf('[', componentStart + 1) >= 0) {
            throw invalid(resourcePath, "Invalid component variant " + specification);
        }

        ResourceLocation itemId = ResourceLocation.tryParse(specification.substring(0, componentStart));
        if (itemId == null) {
            throw invalid(resourcePath, "Invalid item id " + specification);
        }
        String component = specification.substring(componentStart + 1, specification.length() - 1);
        int separator = component.indexOf('=');
        if (separator <= 0 || separator == component.length() - 1) {
            throw invalid(resourcePath, "Invalid component variant " + specification);
        }

        String componentId = component.substring(0, separator);
        String componentValue = component.substring(separator + 1);
        try {
            EssenceItemVariant variant = switch (componentId) {
                case "potion_contents" -> {
                    requireVariantItem(itemId, specification, "potion", "splash_potion", "lingering_potion", "tipped_arrow");
                    ResourceLocation potionId = requireLocation(componentValue, specification);
                    yield EssenceItemVariant.potion(potionId);
                }
                case "stored_enchantments" -> {
                    requireVariantItem(itemId, specification, "enchanted_book");
                    int levelSeparator = componentValue.lastIndexOf(':');
                    if (levelSeparator <= 0 || levelSeparator == componentValue.length() - 1) {
                        throw new IllegalArgumentException("Stored enchantment requires namespace:id:level");
                    }
                    ResourceLocation enchantmentId = requireLocation(
                            componentValue.substring(0, levelSeparator),
                            specification
                    );
                    int level = Integer.parseInt(componentValue.substring(levelSeparator + 1));
                    yield EssenceItemVariant.storedEnchantment(enchantmentId, level);
                }
                case "ominous_bottle_amplifier" -> {
                    requireVariantItem(itemId, specification, "ominous_bottle");
                    yield EssenceItemVariant.ominousBottleAmplifier(Integer.parseInt(componentValue));
                }
                default -> throw new IllegalArgumentException("Unsupported component " + componentId);
            };
            return new ParsedItem(itemId, variant);
        } catch (IllegalArgumentException exception) {
            throw invalid(resourcePath, "Invalid component variant " + specification, exception);
        }
    }

    private static ResourceLocation requireLocation(String value, String specification) {
        ResourceLocation location = ResourceLocation.tryParse(value);
        if (location == null) {
            throw new IllegalArgumentException("Invalid resource location in " + specification);
        }
        return location;
    }

    private static void requireVariantItem(
            ResourceLocation itemId,
            String specification,
            String... allowedPaths
    ) {
        if (!"minecraft".equals(itemId.getNamespace())) {
            throw new IllegalArgumentException("Unsupported variant item " + specification);
        }
        for (String path : allowedPaths) {
            if (path.equals(itemId.getPath())) {
                return;
            }
        }
        throw new IllegalArgumentException("Component does not apply to " + specification);
    }

    private static EssenceTier parseTier(String value, String resourcePath) {
        return switch (value.toLowerCase(Locale.ROOT)) {
            case "basic" -> EssenceTier.BASIC;
            case "common" -> EssenceTier.COMMON;
            case "rare" -> EssenceTier.RARE;
            case "arcane" -> EssenceTier.ARCANE;
            case "mythic" -> EssenceTier.MYTHIC;
            default -> throw invalid(resourcePath, "Unknown tier " + value);
        };
    }

    private static JsonObject readRoot(String resourcePath) {
        return EssenceItemConfigFiles.readRoot(resourcePath);
    }

    private static JsonObject requiredObject(JsonElement value, String resourcePath, String name) {
        if (value == null || !value.isJsonObject()) {
            throw invalid(resourcePath, name + " must be an object");
        }
        return value.getAsJsonObject();
    }

    private static JsonArray requiredArray(JsonObject object, String name, String resourcePath) {
        JsonElement value = object.get(name);
        if (value == null || !value.isJsonArray()) {
            throw invalid(resourcePath, name + " must be an array");
        }
        return value.getAsJsonArray();
    }

    private static String requiredString(JsonObject object, String name, String resourcePath) {
        JsonElement value = object.get(name);
        if (value == null || !value.isJsonPrimitive()) {
            throw invalid(resourcePath, name + " must be a string");
        }
        String result = value.getAsString();
        if (result.isBlank()) {
            throw invalid(resourcePath, name + " cannot be blank");
        }
        return result;
    }

    private static int requiredInt(JsonObject object, String name, String resourcePath) {
        long value = requiredLong(object, name, resourcePath);
        if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
            throw invalid(resourcePath, name + " is outside the integer range");
        }
        return (int) value;
    }

    private static long requiredLong(JsonObject object, String name, String resourcePath) {
        JsonElement value = object.get(name);
        if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber()) {
            throw invalid(resourcePath, name + " must be a number");
        }
        try {
            return value.getAsLong();
        } catch (NumberFormatException exception) {
            throw invalid(resourcePath, name + " must be a whole number", exception);
        }
    }

    private static IllegalStateException invalid(String resourcePath, String message) {
        return new IllegalStateException("Invalid Essence catalog " + resourcePath + ": " + message);
    }

    private static IllegalStateException invalid(String resourcePath, String message, Throwable cause) {
        return new IllegalStateException("Invalid Essence catalog " + resourcePath + ": " + message, cause);
    }

    private record ParsedItem(ResourceLocation location, EssenceItemVariant variant) {
    }
}