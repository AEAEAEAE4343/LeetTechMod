package com.leetftw.tech_mod.datagen;

import com.leetftw.tech_mod.LeetTechMod;
import com.leetftw.tech_mod.block.ModBlocks;
import com.leetftw.tech_mod.item.ModItems;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.core.Holder;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.stream.Stream;

public class ModModelProvider extends ModelProvider
{
    public ModModelProvider(PackOutput output)
    {
        super(output, LeetTechMod.MOD_ID);
    }

    /*
    @Override
    protected Stream<? extends Holder<Block>> getKnownBlocks() {
        return Stream.of(ModBlocks.AESTHETIC_BLOCK, ModBlocks.BUDDING_AESTHETIC_BLOCK, ModBlocks.ENERGY_RING_CASING,
                ModBlocks.ENERGY_CELL, ModBlocks.CREATIVE_ENERGY_CELL, ModBlocks.GEM_REFINERY,
                ModBlocks.CRYSTALLIZER, ModBlocks.CRYSTAL_INJECTOR, ModBlocks.ENERGY_RING_CONTROLLER);
    }

    @Override
    protected Stream<? extends Holder<Item>> getKnownItems() {
        return ModItems.SIMPLE_MODEL_ITEMS.stream();
    }
    */

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels)
    {
        new ModBlockModelGenerator(blockModels.blockStateOutput, itemModels.itemModelOutput, blockModels.modelOutput).run();
        new ModItemModelGenerator(itemModels.itemModelOutput, itemModels.modelOutput).run();
    }
}
