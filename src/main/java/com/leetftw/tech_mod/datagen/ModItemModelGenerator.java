package com.leetftw.tech_mod.datagen;

import com.leetftw.tech_mod.item.ModItems;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ItemModelOutput;
import net.minecraft.client.data.models.model.ModelInstance;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.function.BiConsumer;

public class ModItemModelGenerator extends ItemModelGenerators
{
    public ModItemModelGenerator(ItemModelOutput itemModelOutput, BiConsumer<ResourceLocation, ModelInstance> modelOutput)
    {
        super(itemModelOutput, modelOutput);
    }

    @Override
    public void run()
    {
        for (DeferredItem<Item> item : ModItems.SIMPLE_MODEL_ITEMS)
            generateFlatItem(item.get(), ModelTemplates.FLAT_ITEM);
    }
}
