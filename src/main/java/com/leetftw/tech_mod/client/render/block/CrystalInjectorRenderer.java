package com.leetftw.tech_mod.client.render.block;

import com.leetftw.tech_mod.block.HorizontalLeetEntityBlock;
import com.leetftw.tech_mod.block.entity.CrystalInjectorBlockEntity;
import com.leetftw.tech_mod.fluid.ModFluids;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;

public class CrystalInjectorRenderer implements BlockEntityRenderer<CrystalInjectorBlockEntity>
{
    FluidState fluidDefaultState;
    IClientFluidTypeExtensions fluidAttributes;
    ResourceLocation fluidLoc;
    TextureAtlasSprite fluidSprite;

    public CrystalInjectorRenderer(BlockEntityRendererProvider.Context ctx)
    {
        // Do things here
        Fluid fluid = ModFluids.LIQUID_AESTHETIC.get();
        fluidDefaultState = fluid.defaultFluidState();
        fluidAttributes = IClientFluidTypeExtensions.of(fluid);
        fluidLoc = fluidAttributes.getStillTexture();
        fluidSprite = Minecraft.getInstance().getTextureAtlas(ResourceLocation.fromNamespaceAndPath("minecraft", "textures/atlas/blocks.png")).apply(fluidLoc);
    }

    @Override
    public AABB getRenderBoundingBox(CrystalInjectorBlockEntity blockEntity)
    {
        if (!blockEntity.isCrafting())
            return BlockEntityRenderer.super.getRenderBoundingBox(blockEntity);

        BlockPos pos = blockEntity.getBlockPos();
        return switch (blockEntity.getBlockState().getValue(HorizontalLeetEntityBlock.FACING)) {
            case Direction.SOUTH -> new AABB(
                    pos.getX(), pos.getY(), pos.getZ() + 1,  // Min corner
                    pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1 + blockEntity.getLaserLength() // Max corner
            );
            case Direction.WEST -> new AABB(
                    pos.getX(), pos.getY(), pos.getZ(),  // Min corner
                    pos.getX() - blockEntity.getLaserLength(), pos.getY() + 1, pos.getZ() + 1 // Max corner
            );
            case Direction.NORTH -> new AABB(
                    pos.getX(), pos.getY(), pos.getZ(),  // Min corner
                    pos.getX() + 1, pos.getY() + 1, pos.getZ() - blockEntity.getLaserLength() // Max corner
            );
            case Direction.EAST -> new AABB(
                    pos.getX() + 1, pos.getY(), pos.getZ(),  // Min corner
                    pos.getX() + 1 + blockEntity.getLaserLength(), pos.getY() + 1, pos.getZ() + 1 // Max corner
            );
            default -> BlockEntityRenderer.super.getRenderBoundingBox(blockEntity);
        };
    }

    @Override
    public void render(CrystalInjectorBlockEntity crystalInjectorBlockEntity, float partialTicks, PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight, int packedOverlay)
    {
        // Only render while crafting
        if (!crystalInjectorBlockEntity.isCrafting())
            return;

        poseStack.pushPose();
        // Rotate around the block according to direction
        // This way the laser is always parallel to the X-axis
        // which makes rendering easier.
        poseStack.translate(0.5, 0.5, 0.5);
        switch (crystalInjectorBlockEntity.getBlockState().getValue(HorizontalLeetEntityBlock.FACING))
        {
            case Direction.NORTH:
                poseStack.mulPose(Axis.YP.rotationDegrees(90));
                break;
            case Direction.WEST:
                poseStack.mulPose(Axis.YP.rotationDegrees(180));
                break;
            case Direction.SOUTH:
                poseStack.mulPose(Axis.YP.rotationDegrees(-90));
                break;
            case Direction.EAST:
            default:
                break;
        }

        // Rotate the beam itself according to tick
        long gameTime = crystalInjectorBlockEntity.getLevel().getGameTime();
        float angle = (gameTime + partialTicks) * 5.0f;
        poseStack.mulPose(Axis.XP.rotationDegrees(angle));
        poseStack.translate(-0.5, -0.5, -0.5);

        // Get liquid aesthetic texture sprite
        int tintColor = fluidAttributes.getTintColor(fluidDefaultState, crystalInjectorBlockEntity.getLevel(), crystalInjectorBlockEntity.getBlockPos());

        // TODO: fix brightness
        // Since it is technically not a laser but a liquid stream, it should not be
        // this bright in all light levels
        int fullBright = LightTexture.pack(15, 15);
        packedLight = fullBright;

        // Draw the vertices
        VertexConsumer builder = multiBufferSource.getBuffer(ItemBlockRenderTypes.getRenderLayer(fluidDefaultState));

        float laserDiameter = 0.125f;
        float laserRadius = laserDiameter / 2f;
        float laserEdgeNear = 0.5f - laserRadius;
        float laserEdgeFar = 0.5f + laserRadius;
        int laserLength = crystalInjectorBlockEntity.getLaserLength();

        // TODO: make fluid texture scale properly

        // Sides are from the perspective of the block
        // Left side
        drawVertex(1, laserEdgeFar, laserEdgeNear, fluidSprite.getU0(), fluidSprite.getV0(), 0, 0, -1, builder, poseStack, packedLight, packedOverlay, tintColor);
        drawVertex(1 + laserLength, laserEdgeFar, laserEdgeNear, fluidSprite.getU1(), fluidSprite.getV0(), 0, 0, -1, builder, poseStack, packedLight, packedOverlay, tintColor);
        drawVertex(1 + laserLength, laserEdgeNear, laserEdgeNear, fluidSprite.getU1(), fluidSprite.getV1(), 0, 0, -1, builder, poseStack, packedLight, packedOverlay, tintColor);
        drawVertex(1, laserEdgeNear, laserEdgeNear, fluidSprite.getU0(), fluidSprite.getV1(), 0, 0, -1, builder, poseStack, packedLight, packedOverlay, tintColor);

        // Right
        drawVertex(1, laserEdgeNear, laserEdgeFar, fluidSprite.getU0(), fluidSprite.getV0(), 0, 0, 1, builder, poseStack, packedLight, packedOverlay, tintColor);
        drawVertex(1 + laserLength, laserEdgeNear, laserEdgeFar, fluidSprite.getU1(), fluidSprite.getV0(), 0, 0, 1, builder, poseStack, packedLight, packedOverlay, tintColor);
        drawVertex(1 + laserLength, laserEdgeFar, laserEdgeFar, fluidSprite.getU1(), fluidSprite.getV1(), 0, 0, 1, builder, poseStack, packedLight, packedOverlay, tintColor);
        drawVertex(1, laserEdgeFar, laserEdgeFar, fluidSprite.getU0(), fluidSprite.getV1(), 0, 0, 1, builder, poseStack, packedLight, packedOverlay, tintColor);

        // Top
        drawVertex(1, laserEdgeFar, laserEdgeFar, fluidSprite.getU0(), fluidSprite.getV0(), 0, 1, 0, builder, poseStack, packedLight, packedOverlay, tintColor);
        drawVertex(1 + laserLength, laserEdgeFar, laserEdgeFar, fluidSprite.getU1(), fluidSprite.getV0(), 0, 1, 0, builder, poseStack, packedLight, packedOverlay, tintColor);
        drawVertex(1 + laserLength, laserEdgeFar, laserEdgeNear, fluidSprite.getU1(), fluidSprite.getV1(), 0, 1, 0, builder, poseStack, packedLight, packedOverlay, tintColor);
        drawVertex(1, laserEdgeFar, laserEdgeNear, fluidSprite.getU0(), fluidSprite.getV1(), 0, 1, 0, builder, poseStack, packedLight, packedOverlay, tintColor);

        // Bottom
        drawVertex(1, laserEdgeNear, laserEdgeNear, fluidSprite.getU0(), fluidSprite.getV0(), 0, -1, 0, builder, poseStack, packedLight, packedOverlay, tintColor);
        drawVertex(1 + laserLength, laserEdgeNear, laserEdgeNear, fluidSprite.getU1(), fluidSprite.getV0(), 0, -1, 0, builder, poseStack, packedLight, packedOverlay, tintColor);
        drawVertex(1 + laserLength, laserEdgeNear, laserEdgeFar, fluidSprite.getU1(), fluidSprite.getV1(), 0, -1, 0, builder, poseStack, packedLight, packedOverlay, tintColor);
        drawVertex(1, laserEdgeNear, laserEdgeFar, fluidSprite.getU0(), fluidSprite.getV1(), 0, -1, 0, builder, poseStack, packedLight, packedOverlay, tintColor);

        poseStack.popPose();
    }

    private void drawVertex(float x0, float y0, float z0, float u, float v, float xn, float yn, float zn,
                            VertexConsumer builder, PoseStack poseStack, int packedLight, int packedOverlay, int tintColor)
    {
        builder.addVertex(poseStack.last().pose(), x0, y0, z0).setLight(packedLight).setOverlay(packedOverlay).setColor(tintColor).setUv(u, v).setNormal(xn, yn, zn);
    }
}
