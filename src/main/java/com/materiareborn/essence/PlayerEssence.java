package com.materiareborn.essence;

import com.materiareborn.config.MateriaConfig;
import com.materiareborn.core.ModConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public final class PlayerEssence {
    private static final String ESSENCE_TAG = ModConstants.MOD_ID + ".essence";

    private PlayerEssence() {
    }

    public static long get(Player player) {
        CompoundTag data = persisted(player);
        long saved = data.getLong(ESSENCE_TAG);
        long clamped = clamp(saved);
        if (saved != clamped) {
            data.putLong(ESSENCE_TAG, clamped);
        }
        return clamped;
    }

    public static long set(Player player, long amount) {
        long value = clamp(amount);
        persisted(player).putLong(ESSENCE_TAG, value);
        return value;
    }

    public static long add(Player player, long amount) {
        return set(player, safeAdd(get(player), Math.max(0L, amount)));
    }

    public static long remove(Player player, long amount) {
        return set(player, get(player) - Math.max(0L, amount));
    }

    public static boolean trySpend(Player player, long amount) {
        long current = get(player);
        if (amount < 0L || current < amount) {
            return false;
        }
        set(player, current - amount);
        return true;
    }

    public static void syncOpenMenu(ServerPlayer player) {
        player.containerMenu.broadcastChanges();
    }

    private static long clamp(long amount) {
        return Math.max(0L, Math.min(MateriaConfig.maxEssence(), amount));
    }

    private static CompoundTag persisted(Player player) {
        CompoundTag root = player.getPersistentData();
        CompoundTag persisted = root.getCompound(Player.PERSISTED_NBT_TAG);
        root.put(Player.PERSISTED_NBT_TAG, persisted);
        return persisted;
    }

    private static long safeAdd(long left, long right) {
        return Long.MAX_VALUE - left < right ? Long.MAX_VALUE : left + right;
    }
}
