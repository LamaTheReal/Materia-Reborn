package com.materiareborn.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.materiareborn.MateriaReborn;
import com.materiareborn.core.ModConstants;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = ModConstants.MOD_ID, value = Dist.CLIENT)
public final class ClientWorldWelcomeMessage {
    private static final String DISCORD_URL = "https://discord.gg/4YmYJc8JTb";
    private static final String FILE_NAME = "client_seen_worlds.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static String activeWorldHash;

    private ClientWorldWelcomeMessage() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            activeWorldHash = null;
            return;
        }

        Optional<String> identity = currentWorldIdentity(minecraft);
        if (identity.isEmpty()) {
            return;
        }

        String worldHash = hash(identity.get());
        if (worldHash.equals(activeWorldHash)) {
            return;
        }
        activeWorldHash = worldHash;

        if (markAsSeen(worldHash)) {
            minecraft.player.displayClientMessage(createMessage(), false);
        }
    }

    private static Optional<String> currentWorldIdentity(Minecraft minecraft) {
        IntegratedServer integratedServer = minecraft.getSingleplayerServer();
        if (integratedServer != null) {
            Path worldPath = integratedServer.getWorldPath(LevelResource.ROOT)
                    .toAbsolutePath()
                    .normalize();
            long seed = integratedServer.getWorldData().worldGenOptions().seed();
            return Optional.of("singleplayer|" + worldPath + "|" + seed);
        }

        ServerData serverData = minecraft.getCurrentServer();
        if (serverData != null && serverData.ip != null && !serverData.ip.isBlank()) {
            return Optional.of("multiplayer|" + serverData.ip.trim().toLowerCase(Locale.ROOT));
        }
        return Optional.empty();
    }

    private static boolean markAsSeen(String worldHash) {
        Path target = storagePath();
        Set<String> seenWorlds = readSeenWorlds(target);
        if (!seenWorlds.add(worldHash)) {
            return false;
        }

        try {
            Files.createDirectories(target.getParent());
            JsonObject root = new JsonObject();
            root.addProperty("schema_version", 1);
            JsonArray entries = new JsonArray();
            seenWorlds.stream().sorted().forEach(entries::add);
            root.add("seen_worlds", entries);

            Path temporary = target.resolveSibling(target.getFileName() + ".tmp");
            Files.writeString(temporary, GSON.toJson(root), StandardCharsets.UTF_8);
            try {
                Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
            }
            return true;
        } catch (IOException exception) {
            MateriaReborn.LOGGER.warn("Could not save the client welcome-message world history", exception);
            return true;
        }
    }

    private static Set<String> readSeenWorlds(Path target) {
        Set<String> result = new HashSet<>();
        if (Files.notExists(target)) {
            return result;
        }

        try (Reader reader = Files.newBufferedReader(target, StandardCharsets.UTF_8)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            JsonArray entries = root.getAsJsonArray("seen_worlds");
            if (entries != null) {
                for (JsonElement entry : entries) {
                    if (entry.isJsonPrimitive() && entry.getAsJsonPrimitive().isString()) {
                        result.add(entry.getAsString());
                    }
                }
            }
        } catch (IOException | RuntimeException exception) {
            MateriaReborn.LOGGER.warn("Could not read the client welcome-message world history", exception);
        }
        return result;
    }

    private static Path storagePath() {
        return FMLPaths.CONFIGDIR.get().resolve(ModConstants.MOD_ID).resolve(FILE_NAME);
    }

    private static String hash(String identity) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(identity.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    private static MutableComponent createMessage() {
        MutableComponent discord = Component.literal("Discord: " + DISCORD_URL)
                .withStyle(ChatFormatting.BLUE)
                .withStyle(style -> style
                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, DISCORD_URL))
                        .withHoverEvent(new HoverEvent(
                                HoverEvent.Action.SHOW_TEXT,
                                Component.literal("Open Materia-Reborn Discord")
                        )));

        return Component.literal("Bugs, ").withStyle(ChatFormatting.DARK_RED)
                .append(Component.literal("Ideas? ").withStyle(ChatFormatting.DARK_GREEN))
                .append(Component.literal("Join ").withStyle(ChatFormatting.WHITE))
                .append(Component.literal("Materia-Reborn ").withStyle(ChatFormatting.DARK_PURPLE))
                .append(discord);
    }
}
