package com.materiareborn.item;

import com.materiareborn.essence.PlayerEssence;
import com.materiareborn.registry.ModParticles;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class EssenceItem extends Item {
    public EssenceItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            int amount = level.random.nextInt(25) + 1;
            PlayerEssence.add(player, amount);
            stack.shrink(1);
            level.playSound(null, player.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 0.7F, 1.1F);
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ModParticles.MAGIC_GLYPH.get(), player.getX(), player.getY() + 1.0D, player.getZ(), 14, 0.2D, 0.3D, 0.2D, 0.03D);
            }
        }
        player.swing(hand, true);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}