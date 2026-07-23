package com.materiareborn.client;

import com.materiareborn.core.ModConstants;
import com.materiareborn.registry.ModItems;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = ModConstants.MOD_ID, value = Dist.CLIENT)
public final class EssenceRefiningHandAnimation {
    private static final int DURATION_TICKS = 100;
    private static int remainingTicks;
    private static int requiredAmount;
    private static boolean crystalInMainHand;

    private EssenceRefiningHandAnimation() {
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!event.getLevel().isClientSide()
                || event.getEntity() != minecraft.player
                || remainingTicks > 0) {
            return;
        }

        LocalPlayer player = minecraft.player;
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();
        boolean crystalMain = mainHand.is(ModItems.ESSENCE_CRYSTAL.get()) && offHand.is(Items.FLINT);
        boolean flintMain = mainHand.is(Items.FLINT) && offHand.is(ModItems.ESSENCE_CRYSTAL.get());
        if (!crystalMain && !flintMain) {
            return;
        }

        crystalInMainHand = crystalMain;
        requiredAmount = Math.min(64, Math.min(mainHand.getCount(), offHand.getCount()));
        remainingTicks = requiredAmount > 0 ? DURATION_TICKS : 0;
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (remainingTicks <= 0) {
            return;
        }

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || !matchesStartedRecipe(player)) {
            clear();
            return;
        }
        remainingTicks--;
        if (remainingTicks <= 0) {
            clear();
        }
    }

    static boolean applyHandTransform(
            PoseStack poseStack,
            LocalPlayer player,
            HumanoidArm arm,
            float partialTick
    ) {
        if (remainingTicks <= 0 || !matchesStartedRecipe(player)) {
            return false;
        }

        float side = arm == HumanoidArm.RIGHT ? 1.0F : -1.0F;
        float elapsed = DURATION_TICKS - remainingTicks + partialTick;
        float fadeIn = Mth.clamp(elapsed / 4.0F, 0.0F, 1.0F);
        float fadeOut = Mth.clamp((remainingTicks - partialTick) / 4.0F, 0.0F, 1.0F);
        float strength = Math.min(fadeIn, fadeOut);
        float rubbing = Mth.sin(elapsed * 1.1F);

        poseStack.translate(
                side * (0.10F + rubbing * 0.018F * strength),
                -0.10F + side * rubbing * 0.028F * strength,
                -0.88F
        );
        poseStack.scale(0.76F, 0.76F, 0.76F);
        poseStack.mulPose(Axis.XP.rotationDegrees(-8.0F));
        poseStack.mulPose(Axis.YP.rotationDegrees(side * 8.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(side * (8.0F + rubbing * 4.0F * strength)));
        return true;
    }

    private static boolean matchesStartedRecipe(LocalPlayer player) {
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();
        boolean correctItems = crystalInMainHand
                ? mainHand.is(ModItems.ESSENCE_CRYSTAL.get()) && offHand.is(Items.FLINT)
                : mainHand.is(Items.FLINT) && offHand.is(ModItems.ESSENCE_CRYSTAL.get());
        return correctItems
                && mainHand.getCount() >= requiredAmount
                && offHand.getCount() >= requiredAmount;
    }

    private static void clear() {
        remainingTicks = 0;
        requiredAmount = 0;
    }
}
