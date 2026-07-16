package com.materiareborn.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.materiareborn.MateriaReborn;
import com.materiareborn.core.ModConstants;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import net.neoforged.fml.loading.FMLPaths;

public final class EssenceItemConfigFiles {
    public static final List<String> CATALOG_RESOURCES = List.of(
            "data/materia_reborn/essence/table_level_1.json",
            "data/materia_reborn/essence/table_level_2.json",
            "data/materia_reborn/essence/table_level_3.json",
            "data/materia_reborn/essence/table_level_4.json"
    );

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private EssenceItemConfigFiles() {
    }

    public static Path configDirectory() {
        return FMLPaths.CONFIGDIR.get().resolve(ModConstants.MOD_ID);
    }

    public static Path essenceDirectory() {
        return configDirectory().resolve("essence");
    }

    public static void initialize() {
        try {
            Files.createDirectories(essenceDirectory());
            for (String resourcePath : CATALOG_RESOURCES) {
                Path target = externalPath(resourcePath);
                if (Files.notExists(target)) {
                    JsonObject defaults = readBundledRoot(resourcePath);
                    addEnabledDefaults(defaults);
                    writeRoot(target, defaults);
                }
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Could not initialize Materia Reborn item configuration", exception);
        }
    }

    public static JsonObject readRoot(String resourcePath) {
        initialize();
        Path external = externalPath(resourcePath);
        try (Reader reader = Files.newBufferedReader(external, StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        } catch (IOException | RuntimeException exception) {
            MateriaReborn.LOGGER.error("Could not read {}, falling back to bundled defaults", external, exception);
            return readBundledRoot(resourcePath);
        }
    }

    public static List<ConfiguredEssenceItem> loadEditableItems() {
        List<ConfiguredEssenceItem> result = new ArrayList<>();
        for (String resourcePath : CATALOG_RESOURCES) {
            JsonObject root = readRoot(resourcePath);
            int tableLevel = root.get("table_level").getAsInt();
            for (JsonElement categoryElement : root.getAsJsonArray("categories")) {
                JsonObject category = categoryElement.getAsJsonObject();
                String categoryId = category.get("id").getAsString();
                for (JsonElement itemElement : category.getAsJsonArray("items")) {
                    JsonObject item = itemElement.getAsJsonObject();
                    result.add(new ConfiguredEssenceItem(
                            resourcePath,
                            tableLevel,
                            categoryId,
                            item.get("id").getAsString(),
                            item.get("tier").getAsString(),
                            !item.has("enabled") || item.get("enabled").getAsBoolean(),
                            item.get("analysis").getAsInt(),
                            item.get("base").getAsLong(),
                            item.get("sell").getAsLong(),
                            item.get("buy").getAsLong()
                    ));
                }
            }
        }
        return List.copyOf(result);
    }

    public static void save(ConfiguredEssenceItem changedItem) {
        JsonObject root = readRoot(changedItem.resourcePath());
        boolean found = false;
        for (JsonElement categoryElement : root.getAsJsonArray("categories")) {
            JsonObject category = categoryElement.getAsJsonObject();
            if (!changedItem.category().equals(category.get("id").getAsString())) {
                continue;
            }
            for (JsonElement itemElement : category.getAsJsonArray("items")) {
                JsonObject item = itemElement.getAsJsonObject();
                if (!changedItem.id().equals(item.get("id").getAsString())) {
                    continue;
                }
                item.addProperty("enabled", changedItem.enabled());
                item.addProperty("analysis", Math.max(1, changedItem.analysis()));
                item.addProperty("base", Math.max(1L, changedItem.baseValue()));
                item.addProperty("sell", Math.max(1L, changedItem.sellValue()));
                item.addProperty("buy", Math.max(1L, changedItem.purchaseCost()));
                found = true;
                break;
            }
        }
        if (!found) {
            throw new IllegalArgumentException("Unknown configured Essence item " + changedItem.id());
        }
        try {
            writeRoot(externalPath(changedItem.resourcePath()), root);
        } catch (IOException exception) {
            throw new IllegalStateException("Could not save Essence item " + changedItem.id(), exception);
        }
    }

    private static Path externalPath(String resourcePath) {
        int separator = resourcePath.lastIndexOf('/');
        return essenceDirectory().resolve(resourcePath.substring(separator + 1));
    }

    private static JsonObject readBundledRoot(String resourcePath) {
        ClassLoader classLoader = EssenceItemConfigFiles.class.getClassLoader();
        try (InputStream stream = classLoader.getResourceAsStream(resourcePath)) {
            if (stream == null) {
                throw new IllegalStateException("Bundled catalog was not found: " + resourcePath);
            }
            try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                return JsonParser.parseReader(reader).getAsJsonObject();
            }
        } catch (IOException | RuntimeException exception) {
            throw new IllegalStateException("Could not read bundled catalog " + resourcePath, exception);
        }
    }

    private static void addEnabledDefaults(JsonObject root) {
        JsonArray categories = root.getAsJsonArray("categories");
        for (JsonElement categoryElement : categories) {
            for (JsonElement itemElement : categoryElement.getAsJsonObject().getAsJsonArray("items")) {
                JsonObject item = itemElement.getAsJsonObject();
                if (!item.has("enabled")) {
                    item.addProperty("enabled", true);
                }
            }
        }
    }

    private static void writeRoot(Path target, JsonObject root) throws IOException {
        Files.createDirectories(target.getParent());
        Path temporary = target.resolveSibling(target.getFileName() + ".tmp");
        Files.writeString(temporary, GSON.toJson(root) + System.lineSeparator(), StandardCharsets.UTF_8);
        Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
    }
}
