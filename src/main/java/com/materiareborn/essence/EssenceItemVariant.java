package com.materiareborn.essence;

import com.materiareborn.api.item.ComponentFingerprint;
import java.util.Map;
import java.util.Objects;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

/** Component-aware identity used by the Essence catalog. */
public record EssenceItemVariant(Kind kind, ResourceLocation valueId, int level) {
    public static final EssenceItemVariant NONE = new EssenceItemVariant(Kind.NONE, null, 0);

    public EssenceItemVariant {
        Objects.requireNonNull(kind, "kind");
        if (kind == Kind.NONE) {
            valueId = null;
            level = 0;
        } else if (kind == Kind.OMINOUS_BOTTLE_AMPLIFIER) {
            if (valueId != null || level < 0 || level > 4) {
                throw new IllegalArgumentException("Ominous bottle amplifier must be between 0 and 4.");
            }
        } else {
            Objects.requireNonNull(valueId, "valueId");
            if (kind == Kind.STORED_ENCHANTMENT && level <= 0) {
                throw new IllegalArgumentException("Stored enchantment level must be positive.");
            }
        }
    }

    public static EssenceItemVariant potion(ResourceLocation potionId) {
        if (!BuiltInRegistries.POTION.containsKey(potionId)) {
            throw new IllegalArgumentException("Unknown potion id " + potionId);
        }
        return new EssenceItemVariant(Kind.POTION_CONTENTS, potionId, 0);
    }

    public static EssenceItemVariant storedEnchantment(ResourceLocation enchantmentId, int level) {
        return new EssenceItemVariant(Kind.STORED_ENCHANTMENT, enchantmentId, level);
    }

    public static EssenceItemVariant ominousBottleAmplifier(int amplifier) {
        return new EssenceItemVariant(Kind.OMINOUS_BOTTLE_AMPLIFIER, null, amplifier);
    }

    public boolean isEmpty() {
        return kind == Kind.NONE;
    }

    public String suffix() {
        return switch (kind) {
            case NONE -> "";
            case POTION_CONTENTS -> "[potion_contents=" + valueId + "]";
            case STORED_ENCHANTMENT -> "[stored_enchantments=" + valueId + ":" + level + "]";
            case OMINOUS_BOTTLE_AMPLIFIER -> "[ominous_bottle_amplifier=" + level + "]";
        };
    }

    public ComponentFingerprint fingerprint() {
        if (isEmpty()) {
            return ComponentFingerprint.empty();
        }
        String value = switch (kind) {
            case NONE -> "";
            case POTION_CONTENTS -> valueId.toString();
            case STORED_ENCHANTMENT -> valueId + ":" + level;
            case OMINOUS_BOTTLE_AMPLIFIER -> Integer.toString(level);
        };
        return ComponentFingerprint.of(Map.of(kind.componentId, value));
    }

    public boolean matches(ItemStack stack) {
        return switch (kind) {
            case NONE -> true;
            case POTION_CONTENTS -> {
                PotionContents actual = stack.get(DataComponents.POTION_CONTENTS);
                yield actual != null && actual.equals(expectedPotionContents());
            }
            case STORED_ENCHANTMENT -> matchesStoredEnchantment(stack);
            case OMINOUS_BOTTLE_AMPLIFIER -> {
                Integer amplifier = stack.get(DataComponents.OMINOUS_BOTTLE_AMPLIFIER);
                yield amplifier != null && amplifier == level;
            }
        };
    }

    public void apply(ItemStack stack, HolderLookup.Provider registries) {
        switch (kind) {
            case NONE -> {
            }
            case POTION_CONTENTS -> stack.set(DataComponents.POTION_CONTENTS, expectedPotionContents());
            case STORED_ENCHANTMENT -> {
                ResourceKey<Enchantment> key = ResourceKey.create(Registries.ENCHANTMENT, valueId);
                Holder.Reference<Enchantment> enchantment = registries
                        .lookupOrThrow(Registries.ENCHANTMENT)
                        .getOrThrow(key);
                ItemEnchantments.Mutable enchantments = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
                enchantments.set(enchantment, level);
                stack.set(DataComponents.STORED_ENCHANTMENTS, enchantments.toImmutable());
            }
            case OMINOUS_BOTTLE_AMPLIFIER ->
                    stack.set(DataComponents.OMINOUS_BOTTLE_AMPLIFIER, level);
        }
    }

    private PotionContents expectedPotionContents() {
        Holder.Reference<Potion> potion = BuiltInRegistries.POTION
                .getHolder(valueId)
                .orElseThrow(() -> new IllegalStateException("Unknown potion id " + valueId));
        return new PotionContents(potion);
    }

    private boolean matchesStoredEnchantment(ItemStack stack) {
        ItemEnchantments enchantments = stack.get(DataComponents.STORED_ENCHANTMENTS);
        if (enchantments == null || enchantments.size() != 1) {
            return false;
        }
        for (var entry : enchantments.entrySet()) {
            ResourceLocation actualId = entry.getKey()
                    .unwrapKey()
                    .map(ResourceKey::location)
                    .orElse(null);
            return valueId.equals(actualId) && entry.getIntValue() == level;
        }
        return false;
    }

    public enum Kind {
        NONE(""),
        POTION_CONTENTS("minecraft:potion_contents"),
        STORED_ENCHANTMENT("minecraft:stored_enchantments"),
        OMINOUS_BOTTLE_AMPLIFIER("minecraft:ominous_bottle_amplifier");

        private final String componentId;

        Kind(String componentId) {
            this.componentId = componentId;
        }
    }
}
