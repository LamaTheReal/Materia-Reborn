package com.materiareborn.item;

import com.materiareborn.api.item.ComponentFingerprint;
import com.materiareborn.api.item.ItemIdentityResolver;
import com.materiareborn.api.item.ItemKey;
import com.materiareborn.api.item.MateriaItemIdentity;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.Objects;

public final class ItemStackIdentityResolver implements ItemIdentityResolver<ItemStack> {
    private final IdentityComponentRegistry componentRegistry;

    public ItemStackIdentityResolver(IdentityComponentRegistry componentRegistry) {
        this.componentRegistry = Objects.requireNonNull(componentRegistry, "componentRegistry");
    }

    @Override
    public MateriaItemIdentity resolve(ItemStack stack) {
        Objects.requireNonNull(stack, "stack");
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        ItemKey itemKey = ItemKey.of(itemId.getNamespace(), itemId.getPath());
        Map<String, String> components = new LinkedHashMap<>();

        PotionContents potion = stack.get(DataComponents.POTION_CONTENTS);
        if ((stack.is(Items.POTION)
                || stack.is(Items.SPLASH_POTION)
                || stack.is(Items.LINGERING_POTION)
                || stack.is(Items.TIPPED_ARROW))
                && potion != null
                && potion.customColor().isEmpty()
                && potion.customEffects().isEmpty()
                && potion.potion().isPresent()) {
            potion.potion().get().unwrapKey().ifPresent(key -> components.put(
                    "minecraft:potion_contents",
                    key.location().toString()
            ));
        }

        ItemEnchantments enchantments = stack.get(DataComponents.STORED_ENCHANTMENTS);
        if (stack.is(Items.ENCHANTED_BOOK) && enchantments != null && !enchantments.isEmpty()) {
            List<String> stored = new ArrayList<>();
            for (var entry : enchantments.entrySet()) {
                entry.getKey().unwrapKey().ifPresent(key -> stored.add(
                        key.location() + ":" + entry.getIntValue()
                ));
            }
            stored.sort(String::compareTo);
            if (!stored.isEmpty()) {
                components.put("minecraft:stored_enchantments", String.join(",", stored));
            }
        }

        Integer amplifier = stack.get(DataComponents.OMINOUS_BOTTLE_AMPLIFIER);
        if (stack.is(Items.OMINOUS_BOTTLE) && amplifier != null) {
            components.put("minecraft:ominous_bottle_amplifier", Integer.toString(amplifier));
        }

        return MateriaItemIdentity.of(itemKey, ComponentFingerprint.of(components));
    }

    public IdentityComponentRegistry componentRegistry() {
        return componentRegistry;
    }
}
