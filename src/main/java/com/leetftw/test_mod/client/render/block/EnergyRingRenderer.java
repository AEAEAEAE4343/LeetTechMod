package com.leetftw.test_mod.client.render.block;

import com.leetftw.test_mod.LeetTechMod;
import com.leetftw.test_mod.block.HorizontalLeetEntityBlock;
import com.leetftw.test_mod.block.entity.CrystalInjectorBlockEntity;
import com.leetftw.test_mod.block.multiblock.energy_ring.EnergyRingControllerBlockEntity;
import com.leetftw.test_mod.fluid.ModFluids;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.awt.*;
import java.util.OptionalDouble;

public class EnergyRingRenderer implements BlockEntityRenderer<EnergyRingControllerBlockEntity>
{
    private int packedLight;
    private int packedOverlay;
    private int packedColor;

    public EnergyRingRenderer(BlockEntityRendererProvider.Context ctx)
    {

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

    private void drawTorus(VertexConsumer consumer, Matrix4f pose, TextureAtlasSprite sprite, float d, float d_prime, int delta_alpha, int delta_beta)
    {
        // Calculate all vertices first
        int numRings = (int) (360f / delta_alpha);
        int numSegments = (int) (360f / delta_beta);

        Vector2f[][] textureCoords = new Vector2f[numRings + 1][numSegments + 1];
        Vector3f[][] vertices = new Vector3f[numRings + 1][numSegments + 1];

        // I'm going to be honest here, I had some trouble getting this working
        // with just the raw math. I understand the math behind drawing a torus,
        // but I just couldn't get it to work in Minecraft. Everything I did in
        // desmos did not seem to translate well to Java.

        // But ClaudeAI could. At least partially, it was able to generate proper
        // vertices, but there were some issues with mutating functions.

        // Generate vertices
        for (int i = 0; i <= numRings; i++) {
            float alpha = (float) (i * 2 * Math.PI / numRings); // Convert to radians directly

            for (int j = 0; j <= numSegments; j++) {
                float beta = (float) (j * 2 * Math.PI / numSegments); // Convert to radians directly

                // Parametric equations for torus
                float x = (float) ((d + d_prime * Math.cos(beta)) * Math.cos(alpha));
                float y = (float) (d_prime * Math.sin(beta));
                float z = (float) ((d + d_prime * Math.cos(beta)) * Math.sin(alpha));

                vertices[i][j] = new Vector3f(x, y, z);

                // Map U based on angle around major radius (i)
                // Map V based on angle around tube (j)
                float u = (float) i / numRings;
                float v = (float) j / numSegments;

                // Map to sprite coordinates
                float textureU = sprite.getU0() + (sprite.getU1() - sprite.getU0()) * u;
                float textureV = sprite.getV0() + (sprite.getV1() - sprite.getV0()) * v;
                textureCoords[i][j] = new Vector2f(textureU, textureV);
            }
        }

        // TODO: Generate the quads and normals only once so that it doesn't generate
        //       thousands of vertices every frame.
        // Generate quads
        for (int i = 0; i < numRings; i++) {
            for (int j = 0; j < numSegments; j++) {
                // Took me two full days to figure out Vector3f.add mutates the object
                // instead of creating a new modified copy.

                // Calculate the center of the quad
                Vector3f center = new Vector3f(vertices[i][j]) // Start with a copy of the first vertex
                        .add(vertices[i + 1][j])
                        .add(vertices[i + 1][j + 1])
                        .add(vertices[i][j + 1])
                        .mul(0.25f); // Average the positions to find the center

                // Calculate the vector pointing to the center of the torus ring
                Vector3f toCenter = new Vector3f(
                        (float) (center.x - d * Math.cos(i * 2 * Math.PI / numRings)), // X
                        center.y, // Y (remains the same for a torus because the shape's center is always at y=0)
                        (float) (center.z - d * Math.sin(i * 2 * Math.PI / numRings))  // Z
                );

                // Normalize the vector to get the normal of the quad
                Vector3f norm = toCenter.normalize();

                // Add vertices in counter-clockwise order
                consumer.addVertex(pose, vertices[i][j].x, vertices[i][j].y, vertices[i][j].z)
                        .setUv(textureCoords[i][j].x, textureCoords[i][j].y)
                        //.setUv(sprite.getU0(), sprite.getV0())
                        .setNormal(norm.x, norm.y, norm.z)
                        .setLight(packedLight).setOverlay(packedOverlay)
                        .setColor(packedColor);
                //.setColor((float) i / numRings, (float) j / numSegments, 1f, 0.8f);
                //.setColor(1f, 1f, 1f, 1f);

                consumer.addVertex(pose, vertices[i][j + 1].x, vertices[i][j + 1].y, vertices[i][j + 1].z)
                        .setUv(textureCoords[i][j + 1].x, textureCoords[i][j + 1].y)
                        //.setUv(sprite.getU0(), sprite.getV1())
                        .setNormal(norm.x, norm.y, norm.z)
                        .setLight(packedLight).setOverlay(packedOverlay)
                        .setColor(packedColor);
                //.setColor((float) i / numRings, (float) j / numSegments, 1f, 0.8f);
                //.setColor(1f, 0f, 0f, 1f);

                consumer.addVertex(pose, vertices[i + 1][j + 1].x, vertices[i + 1][j + 1].y, vertices[i + 1][j + 1].z)
                        .setUv(textureCoords[i + 1][j + 1].x, textureCoords[i + 1][j + 1].y)
                        //.setUv(sprite.getU1(), sprite.getV1())
                        .setNormal(norm.x, norm.y, norm.z)
                        .setLight(packedLight).setOverlay(packedOverlay)
                        .setColor(packedColor);
                //.setColor((float) i / numRings, (float) j / numSegments, 1f, 0.8f);
                //.setColor(0f, 1f, 0f, 1f);

                consumer.addVertex(pose, vertices[i + 1][j].x, vertices[i + 1][j].y, vertices[i + 1][j].z)
                        .setUv(textureCoords[i + 1][j].x, textureCoords[i + 1][j].y)
                        //.setUv(sprite.getU1(), sprite.getV0())
                        .setNormal(norm.x, norm.y, norm.z)
                        .setLight(packedLight).setOverlay(packedOverlay)
                        .setColor(packedColor);
                //.setColor((float) i / numRings, (float) j / numSegments, 1f, 0.8f);
                //.setColor(0f, 0f, 1f, 1f);

                /*if (false && i == 0 && j == 0) {
                    LeetTechMod.LOGGER.debug("DRAWING QUAD AT " + vertices[0][0].toString() + "->" + vertices[0][1].toString() + "->" + vertices[1][1].toString() + "->" + vertices[1][0].toString());
                    LeetTechMod.LOGGER.debug("WITH UV " + textureCoords[0][0].toString() + "->" + textureCoords[0][1].toString() + "->" + textureCoords[1][1].toString() + "->" + textureCoords[1][0].toString());
                    LeetTechMod.LOGGER.debug("AND NORMAL " + norm);
                }*/
            }
        }
    }

    @Override
    public void render(EnergyRingControllerBlockEntity energyRingControllerBlockEntity, float partialTick, PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight, int packedOverlay)
    {
        if (!energyRingControllerBlockEntity.isFormed())
            return;

        this.packedLight = LightTexture.pack(15, 15);
        this.packedOverlay = packedOverlay;
        this.packedColor = Color.YELLOW.getRGB();

        poseStack.pushPose();

        // World Transformations
        // We set the origin to the center of the controller
        poseStack.translate(0.5, 0.5, 0.5);

        long gameTime = energyRingControllerBlockEntity.getLevel().getGameTime();
        float angle = (gameTime + partialTick) * 7.5f;
        //poseStack.mulPose(Axis.YP.rotationDegrees(angle));

        // Rendering
        VertexConsumer builder = multiBufferSource.getBuffer(RenderType.SOLID);

        // Use water texture for now
        Fluid fluid = Fluids.WATER;
        IClientFluidTypeExtensions fluidAttributes = IClientFluidTypeExtensions.of(fluid);
        ResourceLocation fluidLoc = fluidAttributes.getStillTexture();
        TextureAtlasSprite fluidSprite = Minecraft.getInstance().getTextureAtlas(ResourceLocation.fromNamespaceAndPath("minecraft", "textures/atlas/blocks.png")).apply(fluidLoc);

        // We draw a basic torus
        float d = 2f;
        float d_prime = 0.25f;
        drawTorus(builder, poseStack.last().pose(), fluidSprite, d, d_prime, 10, 90);

        poseStack.popPose();
    }
}
