package com.leetftw.tech_mod.client.render.block;

import com.leetftw.tech_mod.block.multiblock.energy_ring.EnergyRingControllerBlockEntity;
import com.leetftw.tech_mod.client.render.block.model.EnergyRingUnbakedModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;

public class EnergyRingRenderer implements BlockEntityRenderer<EnergyRingControllerBlockEntity>
{
    private EnergyRingUnbakedModel model;

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
        model.render(poseStack, multiBufferSource, LightTexture.pack(15, 15), packedOverlay);

        poseStack.popPose();
    }
}
