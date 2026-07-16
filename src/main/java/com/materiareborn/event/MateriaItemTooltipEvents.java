package com.materiareborn.event;

import com.materiareborn.essence.EssenceItemCatalog;
import com.materiareborn.essence.EssenceItemDefinition;
import java.util.List;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

public final class MateriaItemTooltipEvents {
    private MateriaItemTooltipEvents() {
    }

    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        EssenceItemCatalog.find(stack).ifPresent(definition -> addEssenceLines(event.getToolTip(), stack, definition));
    }

    private static void addEssenceLines(
            List<Component> tooltip,
            ItemStack stack,
            EssenceItemDefinition definition
    ) {
        long stackValue = safeMultiply(definition.sellValue(), stack.getCount());
        int insertionIndex = Math.min(1, tooltip.size());
        tooltip.add(insertionIndex, Component.translatable(
                "tooltip.materia_reborn.essence_value",
                format(definition.sellValue())
        ).withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(insertionIndex + 1, Component.translatable(
                "tooltip.materia_reborn.stack_essence_value",
                format(stackValue)
        ).withStyle(ChatFormatting.DARK_PURPLE));
    }

    private static String format(long value) {
        return String.format(Locale.ROOT, "%,d", value);
    }

    private static long safeMultiply(long value, int count) {
        return count > 0 && value > Long.MAX_VALUE / count ? Long.MAX_VALUE : value * count;
    }
}