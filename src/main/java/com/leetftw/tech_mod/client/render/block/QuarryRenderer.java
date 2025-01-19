package com.leetftw.tech_mod.client.render.block;

import com.google.common.collect.Maps;
import com.leetftw.tech_mod.LeetTechMod;
import com.leetftw.tech_mod.block.ModBlocks;
import com.leetftw.tech_mod.block.multiblock.StaticMultiBlockPart;
import com.leetftw.tech_mod.block.multiblock.quarry.QuarryControllerBlockEntity;
import com.leetftw.tech_mod.block.multiblock.quarry.QuarryFrameBlock;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LightEngine;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.RenderTypeHelper;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.IntStream;

public class QuarryRenderer implements BlockEntityRenderer<QuarryControllerBlockEntity>
{
    private static MultiPartBakedModel fullFrameModel;
    private static final Map<Direction, BakedModel> partialModels = Maps.newHashMap();

    public QuarryRenderer(BlockEntityRendererProvider.Context ctx)
    {
        ResourceLocation baseModel = ResourceLocation.fromNamespaceAndPath(LeetTechMod.MOD_ID, "quarry_frame");
        ModelManager modelManager = Minecraft.getInstance().getModelManager();

        BakedModel frameXModel = modelManager.getModel(new ModelResourceLocation(baseModel, "down=true,east=true,formed=false,north=true,south=true,up=true,west=true"));
        if (!(frameXModel instanceof MultiPartBakedModel multiPartXModel))
            throw new RuntimeException("Expected multipart Quarry Frame model! Got '" + frameXModel + "' instead!");
        fullFrameModel = multiPartXModel;

        for (Direction direction : Direction.values())
            partialModels.put(direction, getPartialModel(fullFrameModel, direction));
    }

    // This function is probably going to receive some hate.
    // It's beautiful.
    private BakedModel getPartialModel(MultiPartBakedModel partBakedModel, Direction direction)
    {
        BlockState state = ModBlocks.QUARRY_FRAME.get().defaultBlockState();

        switch (direction)
        {
            case UP: state = state.setValue(QuarryFrameBlock.UP_CON, true);
            case DOWN: state = state.setValue(QuarryFrameBlock.DOWN_CON, true);
            case NORTH: state = state.setValue(QuarryFrameBlock.NORTH_CON, true);
            case EAST: state = state.setValue(QuarryFrameBlock.EAST_CON, true);
            case SOUTH: state = state.setValue(QuarryFrameBlock.SOUTH_CON, true);
            case WEST: state = state.setValue(QuarryFrameBlock.WEST_CON, true);
        }

        BitSet selectors = partBakedModel.getSelectors(state);
        Field selectorsField = Arrays.stream(FieldUtils.getAllFields(MultiPartBakedModel.class)).filter(a -> a.getName().equals("selectors")).findFirst().get();
        selectorsField.setAccessible(true);

        BakedModel sideModel;
        try {
            List<MultiPartBakedModel.Selector> modelComponents = (List<MultiPartBakedModel.Selector>) FieldUtils.readField(selectorsField, partBakedModel, true);
            sideModel = (BakedModel)IntStream.range(0, selectors.size()).filter(selectors::get)
                    .mapToObj(i -> modelComponents.get(i).model())
                    .toArray()[1];
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        return sideModel;
    }

    @Override
    public AABB getRenderBoundingBox(QuarryControllerBlockEntity blockEntity)
    {
        if (!blockEntity.getBlockState().getValue(StaticMultiBlockPart.FORMED))
            return BlockEntityRenderer.super.getRenderBoundingBox(blockEntity);

        BlockPos startCorner = blockEntity.getCornerOne();
        BlockPos endCorner = blockEntity.getCornerTwo();
        BlockPos targetBlock = blockEntity.getTargetPosition();
        return AABB.encapsulatingFullBlocks(startCorner.atY(targetBlock.getY()), endCorner);
    }

    @Override
    public boolean shouldRenderOffScreen(QuarryControllerBlockEntity blockEntity) {
        return true;
    }

    @Override
    public boolean shouldRender(QuarryControllerBlockEntity blockEntity, Vec3 cameraPos) {
        Vec3 blockEntityPos = Vec3.atCenterOf(blockEntity.getBlockPos());

        double horizontalDistance = Math.sqrt(
                Math.pow(blockEntityPos.x - cameraPos.x, 2) +
                        Math.pow(blockEntityPos.z - cameraPos.z, 2)
        );

        return horizontalDistance < (double) this.getViewDistance();
    }

    private BlockState getFrameBlockState(boolean up, boolean down, boolean north, boolean east, boolean south, boolean west)
    {
        return ModBlocks.QUARRY_FRAME.get().defaultBlockState()
                .setValue(QuarryFrameBlock.UP_CON, up)
                .setValue(QuarryFrameBlock.DOWN_CON, down)
                .setValue(QuarryFrameBlock.NORTH_CON, north)
                .setValue(QuarryFrameBlock.EAST_CON, east)
                .setValue(QuarryFrameBlock.SOUTH_CON, south)
                .setValue(QuarryFrameBlock.WEST_CON, west);
    }

    // I can't thank the Bibliocraft dev anough for this:
    // https://github.com/MinecraftschurliMods/Bibliocraft-Legacy/blob/main/src/main/java/com/github/minecraftschurlimods/bibliocraft/util/ClientUtil.java#L127-L149
    private void renderModel(BakedModel model, BlockState state, Level level, BlockPos pos, ModelData modelData, MultiBufferSource buffer, PoseStack stack)
    {
        ModelBlockRenderer renderer = Minecraft.getInstance().getBlockRenderer().getModelRenderer();
        int color = Minecraft.getInstance().getBlockColors().getColor(state, level, pos, 0);
        float red = (float) (color >> 16 & 255) / 255.0F;
        float green = (float) (color >> 8 & 255) / 255.0F;
        float blue = (float) (color & 255) / 255.0F;
        int light = LevelRenderer.getLightColor(level, pos);
        for (RenderType type : model.getRenderTypes(state, level.getRandom(), modelData)) {
            renderer.renderModel(stack.last(), buffer.getBuffer(RenderTypeHelper.getEntityRenderType(type)), state, model, red, green, blue, light, OverlayTexture.NO_OVERLAY, modelData, type);
        }
    }

    private float lerp(float start, float end, float t)
    {
        return start + t * (end - start);
    }

    public static void drawCube(VertexConsumer consumer, PoseStack poseStack) {
        float size = 0.6f;

        float[][] vertices = {
                {-size, -size, -size}, // 0: Bottom-left-back
                { size, -size, -size}, // 1: Bottom-right-back
                { size, -size,  size}, // 2: Bottom-right-front
                {-size, -size,  size}, // 3: Bottom-left-front
                {-size,  size, -size}, // 4: Top-left-back
                { size,  size, -size}, // 5: Top-right-back
                { size,  size,  size}, // 6: Top-right-front
                {-size,  size,  size}  // 7: Top-left-front
        };

        int[][] faces = {
                {0, 1, 2, 3}, // Bottom face
                {4, 5, 6, 7}, // Top face
                {3, 2, 6, 7}, // North face
                {0, 1, 5, 4}, // South face
                {1, 2, 6, 5}, // East face
                {0, 3, 7, 4}  // West face
        };

        for (int[] face : faces)
        {
            for (int vertexIndex : face)
            {
                float[] vertex = vertices[vertexIndex];
                consumer.addVertex(poseStack.last().pose(), vertex[0], vertex[1], vertex[2]).setColor(128, 0, 0, 50);
            }
        }
    }

    @Override
    public void render(QuarryControllerBlockEntity quarryBe, float partialTick, PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight, int packedOverlay)
    {
        int highlight = quarryBe.getBadBlockDuration();
        if (highlight > 0)
        {
            VertexConsumer consumer = multiBufferSource.getBuffer(RenderType.DEBUG_QUADS);
            poseStack.pushPose();
            poseStack.translate(0.5, 0.5, 0.5);
            poseStack.translate(Vec3.atLowerCornerOf(quarryBe.getBadBlock().subtract(quarryBe.getBlockPos())));

            drawCube(consumer, poseStack);

            poseStack.popPose();
        }

        if (!quarryBe.getBlockState().getValue(StaticMultiBlockPart.FORMED)
            || quarryBe.getLevel() == null)
            return;

        BlockPos quarryPos = quarryBe.getBlockPos();
        BlockPos startCorner = quarryBe.getCornerOne();
        BlockPos endCorner = quarryBe.getCornerTwo();
        BlockPos targetBlock = quarryBe.getTargetPosition();
        BlockPos nextBlock = quarryBe.getNextPosition();

        float t = quarryBe.isMoving() ? (quarryBe.getProgress() - partialTick) / quarryBe.getMovementTicks() : 1;
        float fakeFrameX = lerp(nextBlock.getX(), targetBlock.getX(), t) - quarryPos.getX();
        float fakeFrameY = endCorner.getY() - quarryPos.getY();
        float fakeFrameZ = lerp(nextBlock.getZ(), targetBlock.getZ(), t) - quarryPos.getZ();

        //
        // Render fake frame along z
        //
        poseStack.pushPose();
        poseStack.translate(fakeFrameX, fakeFrameY, startCorner.getZ() - quarryPos.getZ());
        BlockState frameState = getFrameBlockState(false, false, true, false, true, false);

        // Render start part
        BakedModel partialModel = partialModels.get(Direction.SOUTH);
        renderModel(partialModel, frameState, quarryBe.getLevel(), new BlockPos(targetBlock.getX(), endCorner.getY(), startCorner.getZ()),
                quarryBe.getModelData(), multiBufferSource, poseStack);

        // Render middle parts
        poseStack.translate(0, 0, 1);
        for (int z = startCorner.getZ() - quarryPos.getZ() + 1; z < endCorner.getZ() - quarryPos.getZ(); z++)
        {
            if (z != fakeFrameZ)
            {
                renderModel(fullFrameModel, frameState, quarryBe.getLevel(), new BlockPos(targetBlock.getX(), endCorner.getY(), z + quarryPos.getZ()),
                        quarryBe.getModelData(), multiBufferSource, poseStack);
            }
            poseStack.translate(0, 0, 1);
        }

        // Render end part
        partialModel = partialModels.get(Direction.NORTH);
        renderModel(partialModel, frameState, quarryBe.getLevel(), new BlockPos(targetBlock.getX(), endCorner.getY(), endCorner.getZ()),
                quarryBe.getModelData(), multiBufferSource, poseStack);
        poseStack.popPose();

        //
        // Render fake frame along x
        //
        poseStack.pushPose();
        poseStack.translate(startCorner.getX() - quarryPos.getX(), fakeFrameY, fakeFrameZ);
        frameState = getFrameBlockState(false, false, false, true, false, true);

        // Render start part
        partialModel = partialModels.get(Direction.EAST);
        renderModel(partialModel, frameState, quarryBe.getLevel(), new BlockPos(startCorner.getX(), endCorner.getY(), targetBlock.getZ()),
                quarryBe.getModelData(), multiBufferSource, poseStack);

        // Render middle parts
        poseStack.translate(1, 0, 0);
        for (int x = startCorner.getX() - quarryPos.getX() + 1; x < endCorner.getX() - quarryPos.getX(); x++) {
            if (x != fakeFrameX)
            {
                renderModel(fullFrameModel, frameState, quarryBe.getLevel(), new BlockPos(x + quarryPos.getX(), endCorner.getY(), targetBlock.getZ()),
                        quarryBe.getModelData(), multiBufferSource, poseStack);
            }
            poseStack.translate(1, 0, 0);
        }

        // Render end part
        partialModel = partialModels.get(Direction.WEST);
        renderModel(partialModel, frameState, quarryBe.getLevel(), new BlockPos(endCorner.getX(), endCorner.getY(), targetBlock.getZ()),
                quarryBe.getModelData(), multiBufferSource, poseStack);
        poseStack.popPose();

        //
        // Render fake frame going down along y
        //
        frameState = getFrameBlockState(true, true, false, false, false, false);
        poseStack.pushPose();
        poseStack.translate(fakeFrameX, fakeFrameY - 1, fakeFrameZ);
        for (int y = endCorner.getY() - 1; y > targetBlock.getY(); y--) {
            renderModel(fullFrameModel, frameState, quarryBe.getLevel(), new BlockPos(targetBlock.getX(), y, targetBlock.getZ()),
                    quarryBe.getModelData(), multiBufferSource, poseStack);
            poseStack.translate(0, -1, 0);
        }
        poseStack.popPose();

        // Render fake frame x,y,z crossing
        frameState = getFrameBlockState(false, true, true, true, true, true);
        poseStack.pushPose();
        poseStack.translate(fakeFrameX, fakeFrameY, fakeFrameZ);
        renderModel(fullFrameModel, frameState, quarryBe.getLevel(), targetBlock.atY(endCorner.getY()),
                quarryBe.getModelData(), multiBufferSource, poseStack);
        poseStack.popPose();
    }
}
