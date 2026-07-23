package com.materiareborn.network;

import com.materiareborn.core.ModConstants;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record DirectCraftPayload(ResourceLocation recipeId) implements CustomPacketPayload {
    public static final Type<DirectCraftPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(ModConstants.MOD_ID, "direct_craft")
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, DirectCraftPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ResourceLocation.STREAM_CODEC,
                    DirectCraftPayload::recipeId,
                    DirectCraftPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
