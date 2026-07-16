package com.materiareborn.client.screen;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;

public enum EssenceCreativeFilter {
    BUILDING_BLOCKS(CreativeModeTabs.BUILDING_BLOCKS),
    COLORED_BLOCKS(CreativeModeTabs.COLORED_BLOCKS),
    NATURAL_BLOCKS(CreativeModeTabs.NATURAL_BLOCKS),
    FUNCTIONAL_BLOCKS(CreativeModeTabs.FUNCTIONAL_BLOCKS),
    REDSTONE_BLOCKS(CreativeModeTabs.REDSTONE_BLOCKS),
    TOOLS_AND_UTILITIES(CreativeModeTabs.TOOLS_AND_UTILITIES),
    COMBAT(CreativeModeTabs.COMBAT),
    FOOD_AND_DRINKS(CreativeModeTabs.FOOD_AND_DRINKS),
    INGREDIENTS(CreativeModeTabs.INGREDIENTS),
    SPAWN_EGGS(CreativeModeTabs.SPAWN_EGGS);

    private final ResourceKey<CreativeModeTab> tabKey;

    EssenceCreativeFilter(ResourceKey<CreativeModeTab> tabKey) {
        this.tabKey = tabKey;
    }

    public CreativeModeTab tab() {
        return BuiltInRegistries.CREATIVE_MODE_TAB.getOrThrow(tabKey);
    }

    public Component displayName() {
        return tab().getDisplayName();
    }

    public ItemStack icon() {
        return tab().getIconItem();
    }
}