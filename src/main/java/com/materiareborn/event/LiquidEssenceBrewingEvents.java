package com.materiareborn.event;

import com.materiareborn.fluid.LiquidEssenceRecipe;
import com.materiareborn.registry.ModBlocks;
import com.materiareborn.registry.ModItems;
import com.materiareborn.registry.ModParticles;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

public final class LiquidEssenceBrewingEvents {
    private static final Map<ServerLevel, Map<BlockPos, Integer>> ACTIVE_BREWS = new WeakHashMap<>();

    private LiquidEssenceBrewingEvents() {
    }

    public static void onItemTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof ItemEntity itemEntity)
                || !(itemEntity.level() instanceof ServerLevel level)
                || itemEntity.tickCount % 5 != 0
                || !isIngredient(itemEntity.getItem())) {
            return;
        }
        BlockPos pos = findWaterSource(level, itemEntity.blockPosition());
        if (pos == null) {
            return;
        }
        Map<BlockPos, Integer> active = ACTIVE_BREWS.computeIfAbsent(level, ignored -> new HashMap<>());
        if (active.containsKey(pos) || !hasIngredients(level, pos)) {
            return;
        }
        active.put(pos.immutable(), LiquidEssenceRecipe.BREW_TICKS);
        playMiniExplosion(level, pos, 0.85F);
        sendMagicParticles(level, pos, 10);
    }

    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        Map<BlockPos, Integer> active = ACTIVE_BREWS.get(level);
        if (active == null || active.isEmpty()) {
            return;
        }
        Iterator<Map.Entry<BlockPos, Integer>> iterator = active.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<BlockPos, Integer> entry = iterator.next();
            BlockPos pos = entry.getKey();
            if (!level.hasChunkAt(pos)) {
                continue;
            }
            if (!isWaterSource(level, pos) || !hasIngredients(level, pos)) {
                iterator.remove();
                continue;
            }
            int remaining = entry.getValue() - 1;
            if (remaining > 0) {
                entry.setValue(remaining);
                if (remaining % 20 == 0) {
                    playMiniExplosion(level, pos, 0.9F + (LiquidEssenceRecipe.BREW_TICKS - remaining) * 0.0025F);
                }
                if (remaining % 4 == 0) {
                    sendMagicParticles(level, pos, 4);
                }
                continue;
            }
            consumeIngredients(level, pos);
            level.setBlock(pos, ModBlocks.LIQUID_ESSENCE.get().defaultBlockState(), Block.UPDATE_ALL);
            playMiniExplosion(level, pos, 1.15F);
            sendMagicParticles(level, pos, 28);
            iterator.remove();
        }
        if (active.isEmpty()) {
            ACTIVE_BREWS.remove(level);
        }
    }

    private static boolean isWaterSource(ServerLevel level, BlockPos pos) {
        return level.getBlockState(pos).is(Blocks.WATER) && level.getFluidState(pos).isSource();
    }

    private static BlockPos findWaterSource(ServerLevel level, BlockPos itemPos) {
        if (isWaterSource(level, itemPos)) {
            return itemPos.immutable();
        }
        BlockPos below = itemPos.below();
        return isWaterSource(level, below) ? below.immutable() : null;
    }

    private static boolean hasIngredients(ServerLevel level, BlockPos pos) {
        List<ItemEntity> items = nearbyItems(level, pos);
        return countItems(items, ModItems.ESSENCE.get()) >= LiquidEssenceRecipe.ESSENCE_COUNT
                && countItems(items, ModItems.ESSENCE_CRYSTAL.get()) >= LiquidEssenceRecipe.CRYSTAL_COUNT;
    }

    private static void consumeIngredients(ServerLevel level, BlockPos pos) {
        List<ItemEntity> items = nearbyItems(level, pos);
        consumeItems(items, ModItems.ESSENCE.get(), LiquidEssenceRecipe.ESSENCE_COUNT);
        consumeItems(items, ModItems.ESSENCE_CRYSTAL.get(), LiquidEssenceRecipe.CRYSTAL_COUNT);
    }

    private static List<ItemEntity> nearbyItems(ServerLevel level, BlockPos pos) {
        return level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(0.35D), entity -> !entity.isRemoved());
    }

    private static int countItems(List<ItemEntity> items, Item item) {
        return items.stream().filter(entity -> entity.getItem().is(item))
                .mapToInt(entity -> entity.getItem().getCount()).sum();
    }

    private static void consumeItems(List<ItemEntity> items, Item item, int amount) {
        int remaining = amount;
        for (ItemEntity entity : items) {
            if (remaining <= 0) return;
            ItemStack stack = entity.getItem();
            if (!stack.is(item)) continue;
            int consumed = Math.min(remaining, stack.getCount());
            stack.shrink(consumed);
            remaining -= consumed;
            if (stack.isEmpty()) entity.discard();
        }
    }

    private static boolean isIngredient(ItemStack stack) {
        return stack.is(ModItems.ESSENCE.get()) || stack.is(ModItems.ESSENCE_CRYSTAL.get());
    }

    private static void playMiniExplosion(ServerLevel level, BlockPos pos, float pitch) {
        level.sendParticles(ParticleTypes.EXPLOSION,
                pos.getX() + 0.5D, pos.getY() + 0.55D, pos.getZ() + 0.5D,
                1, 0.12D, 0.10D, 0.12D, 0.0D);
        level.playSound(null, pos, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 0.22F, pitch);
    }

    private static void sendMagicParticles(ServerLevel level, BlockPos pos, int count) {
        level.sendParticles(ModParticles.MAGIC_GLYPH.get(),
                pos.getX() + 0.5D, pos.getY() + 0.55D, pos.getZ() + 0.5D,
                count, 0.34D, 0.24D, 0.34D, 0.025D);
    }
}
