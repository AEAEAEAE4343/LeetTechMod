package com.leetftw.tech_mod.datagen;

import com.google.gson.JsonPrimitive;
import com.leetftw.tech_mod.LeetTechMod;
import com.leetftw.tech_mod.block.EnergyStorageBlock;
import com.leetftw.tech_mod.block.ModBlocks;
import com.leetftw.tech_mod.block.multiblock.quarry.QuarryFrameBlock;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelOutput;
import net.minecraft.client.data.models.blockstates.*;
import net.minecraft.client.data.models.model.*;
import net.minecraft.client.renderer.item.ClientItem;
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

    private <T extends Block> void createEnergyCell(DeferredBlock<T> block, int itemFillState)
    {
        MultiVariantGenerator generator = MultiVariantGenerator.multiVariant(block.get()).with(
                PropertyDispatch.properties(EnergyStorageBlock.FILL_STATE, BlockStateProperties.HORIZONTAL_FACING)
                        .generate((fillState, facing) ->
                        {
                            ResourceLocation variantLocation = block.getId().withPrefix("block/").withSuffix("_" + fillState);
                            return Variant.variant().with(VariantProperties.MODEL, variantLocation)
                                    .with(Y_ROT, (int)(facing.toYRot() + 180) % 360);
                        })
        );
        blockStateOutput.accept(generator);

        for (int fillState : EnergyStorageBlock.FILL_STATE.getPossibleValues())
        {
            ModelTemplates.CUBE_ORIENTABLE.createWithSuffix(block.get(), "_" + fillState, new TextureMapping()
                            .put(TextureSlot.TOP, block.getId().withPrefix("block/").withSuffix("_side"))
                            .put(TextureSlot.SIDE, block.getId().withPrefix("block/").withSuffix("_side"))
                            .put(TextureSlot.FRONT, block.getId().withPrefix("block/").withSuffix("_front_" + fillState)),
                    modelOutput);
        }

        itemModelOutput.register(block.asItem(), new ClientItem(ItemModelUtils.plainModel(block.getId().withPrefix("block/").withSuffix("_" + itemFillState)), ClientItem.Properties.DEFAULT));
    }

    private <T extends QuarryFrameBlock> void createQuarryFrame(DeferredBlock<T> block)
    {
        ResourceLocation quarryFrameBase = ResourceLocation.fromNamespaceAndPath(LeetTechMod.MOD_ID, "block/quarry_frame_base");
        ResourceLocation quarryFrameExtension = ResourceLocation.fromNamespaceAndPath(LeetTechMod.MOD_ID, "block/quarry_frame_extension");

        MultiPartGenerator generator = MultiPartGenerator.multiPart(block.get())
                .with(Condition.condition().term(QuarryFrameBlock.FORMED, false),
                        Variant.variant().with(VariantProperties.MODEL, quarryFrameBase))
                .with(Condition.condition()
                                .term(QuarryFrameBlock.FORMED, false)
                                .term(QuarryFrameBlock.NORTH_CON, true),
                        Variant.variant().with(VariantProperties.MODEL, quarryFrameExtension))
                .with(Condition.condition()
                                .term(QuarryFrameBlock.FORMED, false)
                                .term(QuarryFrameBlock.EAST_CON, true),
                        Variant.variant().with(VariantProperties.MODEL, quarryFrameExtension)
                                .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90))
                .with(Condition.condition()
                                .term(QuarryFrameBlock.FORMED, false)
                                .term(QuarryFrameBlock.SOUTH_CON, true),
                        Variant.variant().with(VariantProperties.MODEL, quarryFrameExtension)
                                .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180))
                .with(Condition.condition()
                                .term(QuarryFrameBlock.FORMED, false)
                                .term(QuarryFrameBlock.WEST_CON, true),
                        Variant.variant().with(VariantProperties.MODEL, quarryFrameExtension)
                                .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270))
                .with(Condition.condition()
                                .term(QuarryFrameBlock.FORMED, false)
                                .term(QuarryFrameBlock.UP_CON, true),
                        Variant.variant().with(VariantProperties.MODEL, quarryFrameExtension)
                                .with(VariantProperties.X_ROT, VariantProperties.Rotation.R270))
                .with(Condition.condition()
                                .term(QuarryFrameBlock.FORMED, false)
                                .term(QuarryFrameBlock.DOWN_CON, true),
                        Variant.variant().with(VariantProperties.MODEL, quarryFrameExtension)
                                .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90))

                // TODO: REMOVE THIS
                //       THIS ONLY EXISTS FOR DEBUGGING PURPOSES
                .with(Condition.condition().term(QuarryFrameBlock.FORMED, true),
                        Variant.variant().with(VariantProperties.MODEL, quarryFrameBase.withSuffix("_formed")))
                .with(Condition.condition()
                                .term(QuarryFrameBlock.FORMED, true)
                                .term(QuarryFrameBlock.NORTH_CON, true),
                        Variant.variant().with(VariantProperties.MODEL, quarryFrameExtension.withSuffix("_formed")))
                .with(Condition.condition()
                                .term(QuarryFrameBlock.FORMED, true)
                                .term(QuarryFrameBlock.EAST_CON, true),
                        Variant.variant().with(VariantProperties.MODEL, quarryFrameExtension.withSuffix("_formed"))
                                .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90))
                .with(Condition.condition()
                                .term(QuarryFrameBlock.FORMED, true)
                                .term(QuarryFrameBlock.SOUTH_CON, true),
                        Variant.variant().with(VariantProperties.MODEL, quarryFrameExtension.withSuffix("_formed"))
                                .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180))
                .with(Condition.condition()
                                .term(QuarryFrameBlock.FORMED, true)
                                .term(QuarryFrameBlock.WEST_CON, true),
                        Variant.variant().with(VariantProperties.MODEL, quarryFrameExtension.withSuffix("_formed"))
                                .with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270))
                .with(Condition.condition()
                                .term(QuarryFrameBlock.FORMED, true)
                                .term(QuarryFrameBlock.UP_CON, true),
                        Variant.variant().with(VariantProperties.MODEL, quarryFrameExtension.withSuffix("_formed"))
                                .with(VariantProperties.X_ROT, VariantProperties.Rotation.R270))
                .with(Condition.condition()
                                .term(QuarryFrameBlock.FORMED, true)
                                .term(QuarryFrameBlock.DOWN_CON, true),
                        Variant.variant().with(VariantProperties.MODEL, quarryFrameExtension.withSuffix("_formed"))
                                .with(VariantProperties.X_ROT, VariantProperties.Rotation.R90));

        itemModelOutput.register(block.asItem(), new ClientItem(ItemModelUtils.plainModel(block.getId().withPrefix("block/").withSuffix("_base")), ClientItem.Properties.DEFAULT));
        blockStateOutput.accept(generator);
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

        createEnergyCell(ModBlocks.ENERGY_CELL, 0);
        createEnergyCell(ModBlocks.CREATIVE_ENERGY_CELL, EnergyStorageBlock.FILL_STATE.getPossibleValues().stream().max(Integer::compareTo).get());

        createQuarryFrame(ModBlocks.QUARRY_FRAME);

        createAmethystCluster(ModBlocks.AESTHETIC_CLUSTER.get());
        createAmethystCluster(ModBlocks.SMALL_AESTHETIC_BUD.get());
        createAmethystCluster(ModBlocks.MEDIUM_AESTHETIC_BUD.get());
        createAmethystCluster(ModBlocks.LARGE_AESTHETIC_BUD.get());

        createParticleOnlyBlock(ModBlocks.LIQUID_AESTHETIC_BLOCK.get());
    }
}
