package com.materiareborn.event;

import com.materiareborn.essence.EssenceCondensationRecipe;
import com.materiareborn.registry.ModItems;
import com.materiareborn.registry.ModSounds;
import net.minecraft.core.BlockPos;
import com.materiareborn.registry.ModParticles;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public final class EssenceCondensationEvents {
    private static final Map<ServerLevel, Map<BlockPos, Integer>> ACTIVE_CONDENSATIONS = new WeakHashMap<>();

    private EssenceCondensationEvents() {
    }

    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        if (!isValidCauldron(level, pos) || !event.getItemStack().is(Items.GLASS_BOTTLE) || !hasIngredients(level, pos)) {
            return;
        }

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        if (level.isClientSide) {
            return;
        }

        ServerLevel serverLevel = (ServerLevel) level;
        Map<BlockPos, Integer> pending = ACTIVE_CONDENSATIONS.computeIfAbsent(serverLevel, ignored -> new HashMap<>());
        if (pending.containsKey(pos)) {
            return;
        }

        consumeIngredients(serverLevel, pos);
        Player player = event.getEntity();
        if (!player.getAbilities().instabuild) {
            event.getItemStack().shrink(1);
        }
        serverLevel.setBlock(pos, Blocks.CAULDRON.defaultBlockState(), Block.UPDATE_ALL);
        pending.put(pos.immutable(), EssenceCondensationRecipe.PROCESS_TICKS);
        playCondensationEffects(serverLevel, pos, 14);
    }

    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }

        Map<BlockPos, Integer> pending = ACTIVE_CONDENSATIONS.get(serverLevel);
        if (pending == null || pending.isEmpty()) {
            return;
        }

        Iterator<Map.Entry<BlockPos, Integer>> iterator = pending.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<BlockPos, Integer> entry = iterator.next();
            BlockPos pos = entry.getKey();
            int remaining = entry.getValue() - 1;

            if (!serverLevel.getBlockState(pos).is(Blocks.CAULDRON)
                    || !serverLevel.getBlockState(pos.below()).is(Blocks.BUDDING_AMETHYST)) {
                iterator.remove();
                continue;
            }

            if (remaining <= 0) {
                serverLevel.setBlock(pos.below(), Blocks.AMETHYST_BLOCK.defaultBlockState(), Block.UPDATE_ALL);
                Block.popResource(
                        serverLevel,
                        pos.above(),
                        new ItemStack(ModItems.ESSENCE.get(), EssenceCondensationRecipe.OUTPUT_COUNT)
                );
                playCondensationEffects(serverLevel, pos, 28);
                iterator.remove();
            } else {
                entry.setValue(remaining);
                if (remaining % 6 == 0) {
                    sendEnchantParticles(serverLevel, pos, 7);
                }
            }
        }

        if (pending.isEmpty()) {
            ACTIVE_CONDENSATIONS.remove(serverLevel);
        }
    }

    private static boolean isValidCauldron(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.is(Blocks.WATER_CAULDRON)
                && state.getValue(LayeredCauldronBlock.LEVEL) == 3
                && level.getBlockState(pos.below()).is(Blocks.BUDDING_AMETHYST);
    }

    private static boolean hasIngredients(Level level, BlockPos pos) {
        List<ItemEntity> items = nearbyItems(level, pos);
        return countItems(items, ModItems.ESSENCE_DUST.get()) >= EssenceCondensationRecipe.DUST_COUNT
                && countItems(items, ModItems.ESSENCE_CRYSTAL.get()) >= EssenceCondensationRecipe.CRYSTAL_COUNT;
    }

    private static void consumeIngredients(ServerLevel level, BlockPos pos) {
        List<ItemEntity> items = nearbyItems(level, pos);
        consumeItems(items, ModItems.ESSENCE_DUST.get(), EssenceCondensationRecipe.DUST_COUNT);
        consumeItems(items, ModItems.ESSENCE_CRYSTAL.get(), EssenceCondensationRecipe.CRYSTAL_COUNT);
    }

    private static List<ItemEntity> nearbyItems(Level level, BlockPos pos) {
        AABB area = new AABB(pos).inflate(0.35D);
        return level.getEntitiesOfClass(ItemEntity.class, area, entity -> !entity.isRemoved());
    }

    private static int countItems(List<ItemEntity> items, Item item) {
        return items.stream()
                .filter(entity -> entity.getItem().is(item))
                .mapToInt(entity -> entity.getItem().getCount())
                .sum();
    }

    private static void consumeItems(List<ItemEntity> items, Item item, int amount) {
        int remaining = amount;
        for (ItemEntity entity : items) {
            if (remaining <= 0) {
                return;
            }
            ItemStack stack = entity.getItem();
            if (!stack.is(item)) {
                continue;
            }
            int consumed = Math.min(remaining, stack.getCount());
            stack.shrink(consumed);
            remaining -= consumed;
            if (stack.isEmpty()) {
                entity.discard();
            }
        }
    }

    private static void playCondensationEffects(ServerLevel level, BlockPos pos, int particleCount) {
        level.playSound(null, pos, ModSounds.ESSENCE_CONDENSATION.get(), SoundSource.BLOCKS, 0.8F, 1.0F);
        sendEnchantParticles(level, pos, particleCount);
    }

    private static void sendEnchantParticles(ServerLevel level, BlockPos pos, int count) {
        level.sendParticles(
                ModParticles.MAGIC_GLYPH.get(),
                pos.getX() + 0.5D,
                pos.getY() + 0.55D,
                pos.getZ() + 0.5D,
                count,
                0.32D,
                0.22D,
                0.32D,
                0.03D
        );
    }
}