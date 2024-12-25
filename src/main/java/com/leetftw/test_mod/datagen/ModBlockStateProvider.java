package com.leetftw.test_mod.datagen;

import com.leetftw.test_mod.LeetTechMod;
import com.leetftw.test_mod.block.EnergyStorageBlock;
import com.leetftw.test_mod.block.ModBlocks;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.client.model.generators.VariantBlockStateBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;

public class ModBlockStateProvider extends BlockStateProvider
{
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper)
    {
        super(output, LeetTechMod.MOD_ID, exFileHelper);
    }

    private <T extends Block> void energyCell(DeferredBlock<T> block)
    {
        VariantBlockStateBuilder variantBuilder = getVariantBuilder(block.get());
        variantBuilder.forAllStates(state ->
                ConfiguredModel.builder()
                        .modelFile(this.models()
                                .orientable(block.getId().getPath() + "_front_" + state.getValue(EnergyStorageBlock.FILL_STATE),
                                        ResourceLocation.fromNamespaceAndPath(LeetTechMod.MOD_ID, "block/" + block.getId().getPath() + "_side"),
                                        ResourceLocation.fromNamespaceAndPath(LeetTechMod.MOD_ID, "block/" + block.getId().getPath() + "_front_" + state.getValue(EnergyStorageBlock.FILL_STATE)),
                                        ResourceLocation.fromNamespaceAndPath(LeetTechMod.MOD_ID, "block/"+ block.getId().getPath() + "_side")))
                        .rotationY(((int)(state.getValue(BlockStateProperties.HORIZONTAL_FACING)).toYRot() + 180) % 360)
                        .build());
    }

    @Override
    protected void registerStatesAndModels()
    {
        simpleBlockWithItemCubeAll(ModBlocks.AESTHETIC_BLOCK);
        simpleBlockWithItemCubeAll(ModBlocks.BUDDING_AESTHETIC_BLOCK);

        energyCell(ModBlocks.ENERGY_CELL);
        energyCell(ModBlocks.CREATIVE_ENERGY_CELL);

        ResourceLocation refinery_front = modLoc("block/gem_refinery_front");
        ResourceLocation refinery_side = modLoc("block/gem_refinery_side");
        horizontalBlock(ModBlocks.GEM_REFINERY.get(), refinery_side, refinery_front, refinery_side);
        itemModels().simpleBlockItem(ModBlocks.GEM_REFINERY.get());

        ResourceLocation crystallizer_front = modLoc("block/crystallizer_front");
        ResourceLocation crystallizer_side = modLoc("block/crystallizer_side");
        horizontalBlock(ModBlocks.CRYSTALLIZER.get(), crystallizer_side, crystallizer_front, crystallizer_side);
        itemModels().simpleBlockItem(ModBlocks.CRYSTALLIZER.get());

        ResourceLocation crystal_injector_front = modLoc("block/crystal_injector_front");
        ResourceLocation crystal_injector_side = modLoc("block/crystal_injector_side");
        horizontalBlock(ModBlocks.CRYSTAL_INJECTOR.get(), crystal_injector_side, crystal_injector_front, crystal_injector_side);
        itemModels().simpleBlockItem(ModBlocks.CRYSTAL_INJECTOR.get());

        ResourceLocation itemGenerated = ResourceLocation.fromNamespaceAndPath("minecraft", "item/generated");

        itemModels().withExistingParent("aesthetic_bud", itemGenerated)
                .transforms()
                .transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND)
                .rotation(0, -90, 25)
                .translation(0, 6, 0)
                .scale(0.68f, 0.68f ,0.68f)
                .end().transform(ItemDisplayContext.FIXED)
                .translation(0, 7, 0)
                .end().transform(ItemDisplayContext.HEAD)
                .translation(0, 14, -5)
                .end().transform(ItemDisplayContext.GUI)
                .translation(0, 2, 0)
                .end()
                .end();

        simpleBlockWithItemCross(ModBlocks.SMALL_AESTHETIC_BUD);
        itemModels().withExistingParent(ModBlocks.SMALL_AESTHETIC_BUD_ITEM.getId().getPath(),
                ResourceLocation.fromNamespaceAndPath(LeetTechMod.MOD_ID, "aesthetic_bud"))
                .texture("layer0", ModBlocks.SMALL_AESTHETIC_BUD.getId().withPrefix("block/"));

        simpleBlockWithItemCross(ModBlocks.MEDIUM_AESTHETIC_BUD);
        itemModels().withExistingParent(ModBlocks.MEDIUM_AESTHETIC_BUD_ITEM.getId().getPath(),
                ResourceLocation.fromNamespaceAndPath(LeetTechMod.MOD_ID, "aesthetic_bud"))
                .texture("layer0", ModBlocks.MEDIUM_AESTHETIC_BUD.getId().withPrefix("block/"));

        simpleBlockWithItemCross(ModBlocks.LARGE_AESTHETIC_BUD);
        itemModels().withExistingParent(ModBlocks.LARGE_AESTHETIC_BUD_ITEM.getId().getPath(),
                ResourceLocation.fromNamespaceAndPath(LeetTechMod.MOD_ID, "aesthetic_bud"))
                .texture("layer0", ModBlocks.LARGE_AESTHETIC_BUD.getId().withPrefix("block/"));

        simpleBlockWithItemCross(ModBlocks.AESTHETIC_CLUSTER);
        itemModels().withExistingParent(ModBlocks.AESTHETIC_ClUSTER_ITEM.getId().getPath(), itemGenerated)
                .texture("layer0", ModBlocks.AESTHETIC_CLUSTER.getId().withPrefix("block/"))
                .transforms()
                .transform(ItemDisplayContext.HEAD)
                .translation(0, 14, -5)
                .end()
                .end();

        //simpleBlock(ModBlocks.LIQUID_AESTHETIC_BLOCK.get(), models().singleTexture());
    }

    private void simpleBlockWithItemCross(DeferredBlock<?> deferredBlock)
    {
        ResourceLocation resource = deferredBlock.getId();
        ModelFile model = models().cross(resource.getPath(), resource.withPrefix("block/")).renderType("cutout");
        directionalBlock(deferredBlock.get(), model);
    }

    private void simpleBlockWithItemCubeAll(DeferredBlock<?> deferredBlock)
    {
        simpleBlock(deferredBlock.get(), cubeAll(deferredBlock.get()));
    }
}
