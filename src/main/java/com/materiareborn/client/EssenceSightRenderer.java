package com.materiareborn.client;

import com.materiareborn.core.ModConstants;
import com.materiareborn.registry.ModBlocks;
import com.materiareborn.registry.ModMobEffects;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

@EventBusSubscriber(modid = ModConstants.MOD_ID, value = Dist.CLIENT)
public final class EssenceSightRenderer {
    private static final int RADIUS = 50;
    private static final int RADIUS_SQUARED = RADIUS * RADIUS;
    private static final int SCAN_INTERVAL_TICKS = 10;
    private static final RenderType SIGHT_FILL = RenderType.create(
            "materia_reborn_essence_sight_fill",
            DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.TRIANGLE_STRIP,
            1536,
            false,
            true,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.POSITION_COLOR_SHADER)
                    .setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setDepthTestState(RenderStateShard.NO_DEPTH_TEST)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .createCompositeState(false)
    );
    private static final RenderType SIGHT_LINES = RenderType.create(
            "materia_reborn_essence_sight_lines",
            DefaultVertexFormat.POSITION_COLOR_NORMAL,
            VertexFormat.Mode.LINES,
            512,
            false,
            false,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.RENDERTYPE_LINES_SHADER)
                    .setLineState(new RenderStateShard.LineStateShard(OptionalDouble.of(3.0D)))
                    .setLayeringState(RenderStateShard.VIEW_OFFSET_Z_LAYERING)
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setDepthTestState(RenderStateShard.NO_DEPTH_TEST)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .createCompositeState(false)
    );

    private static List<BlockPos> highlightedOres = List.of();
    private static int scanCooldown;

    private EssenceSightRenderer() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null
                || !minecraft.player.hasEffect(ModMobEffects.ESSENCE_SIGHT)) {
            highlightedOres = List.of();
            scanCooldown = 0;
            return;
        }
        if (scanCooldown-- > 0) {
            return;
        }
        scanCooldown = SCAN_INTERVAL_TICKS;
        highlightedOres = findNearbyOres(minecraft, minecraft.player.blockPosition());
    }

    private static List<BlockPos> findNearbyOres(Minecraft minecraft, BlockPos center) {
        int minChunkX = blockToSection(center.getX() - RADIUS);
        int maxChunkX = blockToSection(center.getX() + RADIUS);
        int minChunkZ = blockToSection(center.getZ() - RADIUS);
        int maxChunkZ = blockToSection(center.getZ() + RADIUS);
        Block essenceOre = ModBlocks.DEEPSLATE_ESSENCE.get();
        List<BlockPos> found = new ArrayList<>();

        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                LevelChunk chunk = minecraft.level.getChunkSource()
                        .getChunk(chunkX, chunkZ, ChunkStatus.FULL, false);
                if (chunk != null) {
                    scanChunk(chunk, center, essenceOre, found);
                }
            }
        }
        return List.copyOf(found);
    }

    private static void scanChunk(LevelChunk chunk, BlockPos center, Block essenceOre, List<BlockPos> found) {
        LevelChunkSection[] sections = chunk.getSections();
        int baseX = chunk.getPos().getMinBlockX();
        int baseZ = chunk.getPos().getMinBlockZ();

        for (int sectionIndex = 0; sectionIndex < sections.length; sectionIndex++) {
            LevelChunkSection section = sections[sectionIndex];
            int baseY = chunk.getSectionYFromSectionIndex(sectionIndex) << 4;
            if (baseY > center.getY() + RADIUS || baseY + 15 < center.getY() - RADIUS
                    || !section.maybeHas(state -> state.is(essenceOre))) {
                continue;
            }

            for (int localX = 0; localX < 16; localX++) {
                int worldX = baseX + localX;
                int offsetX = worldX - center.getX();
                int offsetXSquared = offsetX * offsetX;
                for (int localZ = 0; localZ < 16; localZ++) {
                    int worldZ = baseZ + localZ;
                    int offsetZ = worldZ - center.getZ();
                    int horizontalSquared = offsetXSquared + offsetZ * offsetZ;
                    if (horizontalSquared > RADIUS_SQUARED) {
                        continue;
                    }
                    for (int localY = 0; localY < 16; localY++) {
                        int worldY = baseY + localY;
                        int offsetY = worldY - center.getY();
                        if (horizontalSquared + offsetY * offsetY <= RADIUS_SQUARED
                                && section.getBlockState(localX, localY, localZ).is(essenceOre)) {
                            found.add(new BlockPos(worldX, worldY, worldZ));
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_LEVEL || highlightedOres.isEmpty()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null
                || !minecraft.player.hasEffect(ModMobEffects.ESSENCE_SIGHT)) {
            return;
        }

        float pulse = 0.5F + 0.5F * (float) Math.sin(
                (minecraft.level.getGameTime()
                        + event.getPartialTick().getGameTimeDeltaPartialTick(false)) * 0.15D
        );
        float red = 0.55F + pulse * 0.25F;
        float green = 0.18F + pulse * 0.35F;
        float blue = 1.0F;
        float fillAlpha = 0.11F + pulse * 0.07F;
        PoseStack poseStack = event.getPoseStack();
        Vec3 camera = event.getCamera().getPosition();
        poseStack.pushPose();
        // AFTER_LEVEL receives an identity pose stack after LevelRenderer has already
        // removed the camera rotation from the global model-view stack.
        poseStack.mulPose(event.getModelViewMatrix());
        poseStack.translate(-camera.x, -camera.y, -camera.z);

        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        try {
            VertexConsumer fill = buffers.getBuffer(SIGHT_FILL);
            for (BlockPos pos : highlightedOres) {
                AABB box = new AABB(pos).inflate(0.015D);
                LevelRenderer.addChainedFilledBoxVertices(
                        poseStack,
                        fill,
                        box.minX,
                        box.minY,
                        box.minZ,
                        box.maxX,
                        box.maxY,
                        box.maxZ,
                        red,
                        green,
                        blue,
                        fillAlpha
                );
            }
            buffers.endBatch(SIGHT_FILL);

            VertexConsumer lines = buffers.getBuffer(SIGHT_LINES);
            for (BlockPos pos : highlightedOres) {
                LevelRenderer.renderLineBox(
                        poseStack,
                        lines,
                        new AABB(pos).inflate(0.02D),
                        red,
                        green,
                        blue,
                        1.0F
                );
            }
            buffers.endBatch(SIGHT_LINES);
        } finally {
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            poseStack.popPose();
        }
    }

    private static int blockToSection(int coordinate) {
        return coordinate >> 4;
    }
}