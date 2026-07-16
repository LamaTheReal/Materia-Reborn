package com.materiareborn.event;

import com.materiareborn.block.MateriaTableBlock;
import com.materiareborn.progression.BackpackExtraUpgrade;
import com.materiareborn.progression.MateriaTableProgression;
import com.materiareborn.progression.PlayerMateriaProgress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public final class MateriaSoulboundEvents {
    private static final Map<UUID, List<ItemStack>> PENDING_RETURNS = new HashMap<>();

    private MateriaSoulboundEvents() {
    }

    public static void onLivingDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || player.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)
                || !MateriaTableProgression.areBackpackExtrasUnlocked(
                        4, PlayerMateriaProgress.storageSlots(player))
                || PlayerMateriaProgress.backpackExtraSetting(player, BackpackExtraUpgrade.SOULBOUND) < 1
                || PlayerMateriaProgress.backpackExtraSetting(player, BackpackExtraUpgrade.KEEP_INVENTORY) < 1) {
            return;
        }

        Collection<ItemEntity> drops = event.getDrops();
        List<ItemStack> retained = new ArrayList<>();
        Iterator<ItemEntity> iterator = drops.iterator();
        while (iterator.hasNext()) {
            ItemEntity drop = iterator.next();
            ItemStack stack = drop.getItem();
            if (isSoulboundTable(stack)) {
                retained.add(stack.copy());
                iterator.remove();
                drop.discard();
            }
        }
        if (!retained.isEmpty()) {
            PENDING_RETURNS.put(player.getUUID(), retained);
        }
    }

    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath() || !(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        List<ItemStack> retained = PENDING_RETURNS.remove(event.getOriginal().getUUID());
        if (retained == null || player.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) {
            return;
        }
        for (ItemStack stack : retained) {
            ItemStack remaining = stack.copy();
            player.getInventory().add(remaining);
            if (!remaining.isEmpty()) {
                player.drop(remaining, false);
            }
        }
    }

    private static boolean isSoulboundTable(ItemStack stack) {
        return stack.getItem() instanceof BlockItem blockItem
                && blockItem.getBlock() instanceof MateriaTableBlock
                && MateriaTableProgression.tableTier(blockItem.getBlock()) >= 4;
    }
}
