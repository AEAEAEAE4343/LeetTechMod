package com.leetftw.tech_mod.client.render.block;

import com.leetftw.tech_mod.block.multiblock.energy_ring.EnergyRingControllerBlockEntity;
import com.leetftw.tech_mod.client.render.block.model.EnergyRingUnbakedModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import org.joml.Vector3f;

import java.awt.Color;

public class EnergyRingRenderer implements BlockEntityRenderer<EnergyRingControllerBlockEntity>
{
    private EnergyRingUnbakedModel model;
    private SimplexNoise perlinNoise = new SimplexNoise(RandomSource.create());

    public EnergyRingRenderer(BlockEntityRendererProvider.Context ctx)
    {
        Fluid fluid = Fluids.WATER;
        IClientFluidTypeExtensions fluidAttributes = IClientFluidTypeExtensions.of(fluid);
        ResourceLocation fluidLoc = fluidAttributes.getStillTexture();
        TextureAtlasSprite fluidSprite = Minecraft.getInstance().getTextureAtlas(ResourceLocation.fromNamespaceAndPath("minecraft", "textures/atlas/blocks.png")).apply(fluidLoc);

        float d = 2f;
        float d_prime = 0.25f;
        model = new EnergyRingUnbakedModel(fluidSprite, d, d_prime, 10, 45);
        model.bake();
    }

    @Override
    public AABB getRenderBoundingBox(EnergyRingControllerBlockEntity blockEntity)
    {
        if (!blockEntity.isFormed())
            return BlockEntityRenderer.super.getRenderBoundingBox(blockEntity);

        BlockPos pos = blockEntity.getBlockPos();
        BlockPos corner1 = pos.offset(-2, 0, -2);
        BlockPos corner2 = pos.offset(2, 0, 2);
        return new AABB(corner1.getX(), corner1.getY(), corner1.getZ(),
                corner2.getX() + 1, corner2.getY() + 1, corner2.getZ() + 1);
    }

    public void drawQuad(Vector3f[] vertices, int step, int[] indeces, PoseStack poseStack, VertexConsumer consumer, int color)
    {
        Vector3f v0 = vertices[step + indeces[0]];
        Vector3f v1 = vertices[step + indeces[1]];
        Vector3f v2 = vertices[step + indeces[2]];
        Vector3f v3 = vertices[step + indeces[3]];

        /*Vector3f e1 = new Vector3f(v1).sub(v0);
        Vector3f e2 = new Vector3f(v3).sub(v0);

        Vector3f normal = new Vector3f();
        e1.cross(e2, normal);
        normal.normalize();*/

        consumer.addVertex(poseStack.last(), v0).setColor(color);
        consumer.addVertex(poseStack.last(), v1).setColor(color);
        consumer.addVertex(poseStack.last(), v2).setColor(color);
        consumer.addVertex(poseStack.last(), v3).setColor(color);
    }

    @Override
    public void render(EnergyRingControllerBlockEntity energyRingControllerBlockEntity, float partialTick, PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight, int packedOverlay)
    {
        if (!energyRingControllerBlockEntity.isFormed())
            return;

        poseStack.pushPose();

        // World Transformations
        // We set the origin to the center of the controller
        poseStack.translate(0.5, 0.5, 0.5);

        long gameTime = energyRingControllerBlockEntity.getLevel().getGameTime();
        float angle = (gameTime + partialTick) * 7.5f;
        poseStack.mulPose(Axis.YP.rotationDegrees(angle));

        // Rendering
        packedLight = LightTexture.pack(15, 15);
        model.render(poseStack, multiBufferSource, packedLight, packedOverlay);

        VertexConsumer consumer = multiBufferSource.getBuffer(RenderType.DEBUG_QUADS);

        // Draw ring with sparks (WIP)
        /*{
            final float sparkLength = (float) (2f * Math.PI * 2f);
            final float sparkStartX = 0f;
            final float sparkStepX = sparkLength / 36f;
            final float sparkRadius = 0.005f;

            final Vector3f[] vertices = new Vector3f[(int) (sparkLength / sparkStepX * 4)];
            final int color = Color.YELLOW.getRGB();

            for (int step = 0; step < sparkLength / sparkStepX; step++) {
                float z = sparkStartX + step * sparkStepX;

                double noiseTime = (gameTime + partialTick) * 0.1f;
                double noiseX = perlinNoise.getValue(z, 0.0, noiseTime);
                double noiseY = perlinNoise.getValue(z, 1.0, noiseTime);

                float offsetX = (float) noiseX * 0.15f + 2;
                float offsetY = (float) noiseY * 0.15f;

                // Calculate the new x, y, z values using the Y-axis rotation matrix
                float angleRadians = (float) ((step * sparkStepX) / sparkLength * 2 * Math.PI);
                offsetX = offsetX * (float) Math.cos(angleRadians) + z * (float) Math.sin(angleRadians);
                float offsetZ = -offsetX * (float) Math.sin(angleRadians) + z * (float) Math.cos(angleRadians);

                // Generate slices
                vertices[step * 4] = new Vector3f(offsetX + sparkRadius, offsetY - sparkRadius, offsetZ);
                vertices[step * 4 + 1] = new Vector3f(offsetX + sparkRadius, offsetY + sparkRadius, offsetZ);
                vertices[step * 4 + 2] = new Vector3f(offsetX - sparkRadius, offsetY + sparkRadius, offsetZ);
                vertices[step * 4 + 3] = new Vector3f(offsetX - sparkRadius, offsetY - sparkRadius, offsetZ);
            }

            // Map the corners of the slices to vertices
            for (int step = 0; step < vertices.length; step += 4) {
                poseStack.pushPose();
                //poseStack.mulPose(Axis.YP.rotationDegrees(step / 4 * 10));

                drawQuad(vertices, step % (vertices.length - 4), new int[]{1, 5, 6, 2}, poseStack, consumer, color);
                drawQuad(vertices, step % (vertices.length - 4), new int[]{2, 6, 7, 3}, poseStack, consumer, color);
                drawQuad(vertices, step % (vertices.length - 4), new int[]{3, 7, 4, 0}, poseStack, consumer, color);
                drawQuad(vertices, step % (vertices.length - 4), new int[]{0, 4, 5, 1}, poseStack, consumer, color);

                poseStack.popPose();
            }
        }*/

        // Render sparks
        {
            final float sparkLength = 1.5f;
            final float sparkStartX = 0.5f;
            final float sparkStepX = 0.25f;
            final float sparkRadius = 0.005f;
            final int numSpark = 20;
            final Vector3f[] vertices = new Vector3f[(int) (sparkLength / sparkStepX * 4)];
            final int color = Color.YELLOW.getRGB();

            for (int i = 0; i < numSpark; ++i) {
                for (int step = 0; step < sparkLength / sparkStepX; step++) {
                    float x = sparkStartX + step * sparkStepX;

                    double noiseTime = (gameTime + partialTick) * 0.1f;
                    double noiseY = perlinNoise.getValue(i * 100f + x, 0.0, noiseTime);
                    double noiseZ = perlinNoise.getValue(i * 100f + x, 1.0, noiseTime);

                    float offsetY = (float) noiseY * 0.15f;
                    float offsetZ = (float) noiseZ * 0.15f;

                    // Generate slices
                    vertices[step * 4] = new Vector3f(x, offsetY - sparkRadius, offsetZ - sparkRadius);
                    vertices[step * 4 + 1] = new Vector3f(x, offsetY - sparkRadius, offsetZ + sparkRadius);
                    vertices[step * 4 + 2] = new Vector3f(x, offsetY + sparkRadius, offsetZ + sparkRadius);
                    vertices[step * 4 + 3] = new Vector3f(x, offsetY + sparkRadius, offsetZ - sparkRadius);
                }

                // Map the corners of the slices to vertices
                for (int step = 0; step < vertices.length - 4; step += 4) {
                    drawQuad(vertices, step, new int[]{1, 5, 6, 2}, poseStack, consumer, color);
                    drawQuad(vertices, step, new int[]{2, 6, 7, 3}, poseStack, consumer, color);
                    drawQuad(vertices, step, new int[]{3, 7, 4, 0}, poseStack, consumer, color);
                    drawQuad(vertices, step, new int[]{0, 4, 5, 1}, poseStack, consumer, color);
                }
            }
        }

        poseStack.popPose();
    }
}
