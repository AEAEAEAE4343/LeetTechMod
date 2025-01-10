package com.leetftw.tech_mod.datagen;

import com.google.gson.JsonPrimitive;
import com.leetftw.tech_mod.block.EnergyStorageBlock;
import com.leetftw.tech_mod.block.ModBlocks;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelOutput;
import net.minecraft.client.data.models.blockstates.*;
import net.minecraft.client.data.models.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class ModBlockModelGenerator extends BlockModelGenerators
{
    // VariantProperties.Rotation has a private constructor, and no easy way
    // to convert a direction into it.
    private static final VariantProperty<Integer> Y_ROT = new VariantProperty<>("y", JsonPrimitive::new);

    // Some blocks have the same texture for side, bottom and top
    // Minecraft has no builtin provider for this
    private static final TexturedModel.Provider ORIENTABLE_ONLY_FRONT_SIDE = TexturedModel.createDefault(
            block -> new TextureMapping()
                    .put(TextureSlot.SIDE, TextureMapping.getBlockTexture(block, "_side"))
                    .put(TextureSlot.FRONT, TextureMapping.getBlockTexture(block, "_front"))
                    .put(TextureSlot.TOP, TextureMapping.getBlockTexture(block, "_side")),
            ModelTemplates.CUBE_ORIENTABLE);

    // Same as above, but for using a side texture from another block
    private static final Function<Block, TexturedModel.Provider> ORIENTABLE_ONLY_FRONT_CUSTOM_SIDE = sideTextureBlock ->
            TexturedModel.createDefault(
                    block -> new TextureMapping()
                            .put(TextureSlot.SIDE, TextureMapping.getBlockTexture(sideTextureBlock))
                            .put(TextureSlot.FRONT, TextureMapping.getBlockTexture(block, "_front"))
                            .put(TextureSlot.TOP, TextureMapping.getBlockTexture(sideTextureBlock)),
                    ModelTemplates.CUBE_ORIENTABLE);


    public ModBlockModelGenerator(Consumer<BlockStateGenerator> blockStateOutput, ItemModelOutput itemModelOutput, BiConsumer<ResourceLocation, ModelInstance> modelOutput)
    {
        super(blockStateOutput, itemModelOutput, modelOutput);
    }

    private <T extends Block> void createEnergyCell(DeferredBlock<T> block)
    {
        MultiVariantGenerator generator = MultiVariantGenerator.multiVariant(block.get()).with(
                PropertyDispatch.properties(EnergyStorageBlock.FILL_STATE, BlockStateProperties.HORIZONTAL_FACING)
                        .generate((fillState, facing) ->
                        {
                            ResourceLocation variantLocation = block.getId().withSuffix("_" + fillState);
                            return Variant.variant().with(VariantProperties.MODEL, variantLocation)
                                    .with(Y_ROT, (int)(facing.toYRot() + 180) % 360);
                        })
        );
        blockStateOutput.accept(generator);

        for (int fillState : EnergyStorageBlock.FILL_STATE.getPossibleValues())
        {
            ModelTemplates.CUBE_ORIENTABLE.createWithSuffix(block.get(), "_" + fillState, new TextureMapping()
                            .put(TextureSlot.TOP, block.getId().withSuffix("_side"))
                            .put(TextureSlot.SIDE, block.getId().withSuffix("_side"))
                            .put(TextureSlot.FRONT, block.getId().withSuffix("_front_" + fillState)),
                    modelOutput);
        }
    }

    @Override
    public void run()
    {
        /*for (DeferredHolder<Block, ? extends Block> block : ModBlocks.BLOCKS.getEntries())
            registerSimpleFlatItemModel(block.get());*/

        createTrivialCube(ModBlocks.AESTHETIC_BLOCK.get());
        createTrivialCube(ModBlocks.BUDDING_AESTHETIC_BLOCK.get());
        createTrivialCube(ModBlocks.ENERGY_RING_CASING.get());

        createHorizontallyRotatedBlock(ModBlocks.GEM_REFINERY.get(), ORIENTABLE_ONLY_FRONT_SIDE);
        createHorizontallyRotatedBlock(ModBlocks.CRYSTALLIZER.get(), ORIENTABLE_ONLY_FRONT_SIDE);
        createHorizontallyRotatedBlock(ModBlocks.CRYSTAL_INJECTOR.get(), ORIENTABLE_ONLY_FRONT_SIDE);
        createHorizontallyRotatedBlock(ModBlocks.QUARRY_CONTROLLER.get(), ORIENTABLE_ONLY_FRONT_SIDE);

        createHorizontallyRotatedBlock(ModBlocks.ENERGY_RING_INPUT_PORT.get(), ORIENTABLE_ONLY_FRONT_CUSTOM_SIDE.apply(ModBlocks.ENERGY_RING_CASING.get()));
        createHorizontallyRotatedBlock(ModBlocks.ENERGY_RING_OUTPUT_PORT.get(), ORIENTABLE_ONLY_FRONT_CUSTOM_SIDE.apply(ModBlocks.ENERGY_RING_CASING.get()));

        createTrivialBlock(ModBlocks.ENERGY_RING_CONTROLLER.get(), TexturedModel.createDefault(
                block -> new TextureMapping()
                        .put(TextureSlot.SIDE, TextureMapping.getBlockTexture(block, "_side"))
                        .put(TextureSlot.END, TextureMapping.getBlockTexture(ModBlocks.ENERGY_RING_CASING.get())),
                ModelTemplates.CUBE_COLUMN));

        createEnergyCell(ModBlocks.ENERGY_CELL);
        createEnergyCell(ModBlocks.CREATIVE_ENERGY_CELL);

        // PLACEHOLDERS!!!!
        // TODO: IMPLEMENT THESE!!!!
        createTrivialCube(ModBlocks.AESTHETIC_CLUSTER.get());
        createTrivialCube(ModBlocks.SMALL_AESTHETIC_BUD.get());
        createTrivialCube(ModBlocks.MEDIUM_AESTHETIC_BUD.get());
        createTrivialCube(ModBlocks.LARGE_AESTHETIC_BUD.get());
        createTrivialCube(ModBlocks.LIQUID_AESTHETIC_BLOCK.get());
        createTrivialCube(ModBlocks.QUARRY_FRAME.get());
    }
}
