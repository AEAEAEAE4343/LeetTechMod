package com.leetftw.tech_mod.datagen;

import com.leetftw.tech_mod.LeetTechMod;
import com.leetftw.tech_mod.block.ModBlocks;
import com.leetftw.tech_mod.item.ModItems;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ItemModelOutput;
import net.minecraft.client.data.models.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.template.ExtendedModelTemplate;
import net.neoforged.neoforge.client.model.generators.template.ExtendedModelTemplateBuilder;
import net.neoforged.neoforge.client.model.generators.template.TransformVecBuilder;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.function.BiConsumer;

public class ModItemModelGenerator extends ItemModelGenerators
{
    public ModItemModelGenerator(ItemModelOutput itemModelOutput, BiConsumer<ResourceLocation, ModelInstance> modelOutput)
    {
        super(itemModelOutput, modelOutput);
    }

    private void generateAestheticBudModel()
    {
        ExtendedModelTemplate template = ExtendedModelTemplateBuilder.builder()
                .parent(ResourceLocation.fromNamespaceAndPath("minecraft", "item/generated"))
                .transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND, vecBuilder -> vecBuilder
                        .rotation(0, -90, 25)
                        .translation(0, 6, 0)
                        .scale(0.68f, 0.68f ,0.68f))
                .transform(ItemDisplayContext.FIXED, vecBuilder -> vecBuilder
                        .translation(0, 7, 0))
                .transform(ItemDisplayContext.HEAD, vecBuilder -> vecBuilder
                        .translation(0, 14, -5))
                .transform(ItemDisplayContext.GUI, vecBuilder -> vecBuilder
                        .translation(0, 2, 0))
                .build();
        template.create(ResourceLocation.fromNamespaceAndPath(LeetTechMod.MOD_ID, "item/aesthetic_bud"), new TextureMapping(), modelOutput);
    }

    private void generateAestheticCluster(Block block)
    {
        ExtendedModelTemplate template = ExtendedModelTemplateBuilder.builder()
                .parent(ResourceLocation.fromNamespaceAndPath(LeetTechMod.MOD_ID, "item/aesthetic_bud"))
                .requiredTextureSlot(TextureSlot.LAYER0)
                .build();

        ResourceLocation loc = template.create(ModelLocationUtils.getModelLocation(block.asItem()), TextureMapping.layer0(block), modelOutput);
        this.itemModelOutput.accept(block.asItem(), ItemModelUtils.plainModel(loc));
    }

    @Override
    public void run()
    {
        for (DeferredItem<Item> item : ModItems.SIMPLE_MODEL_ITEMS)
            generateFlatItem(item.get(), ModelTemplates.FLAT_ITEM);

        generateAestheticBudModel();
        generateAestheticCluster(ModBlocks.AESTHETIC_CLUSTER.get());
        generateAestheticCluster(ModBlocks.SMALL_AESTHETIC_BUD.get());
        generateAestheticCluster(ModBlocks.MEDIUM_AESTHETIC_BUD.get());
        generateAestheticCluster(ModBlocks.LARGE_AESTHETIC_BUD.get());
    }
}
