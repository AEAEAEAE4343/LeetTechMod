package com.leetftw.tech_mod.datagen.items;

import com.leetftw.tech_mod.LeetTechMod;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.JsonCodecProvider;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.concurrent.CompletableFuture;

// I really don't know what to call these files
// From what I can tell they are 'Top-level Client Item Info' files
// src: https://www.minecraft.net/en-us/article/minecraft-java-edition-1-21-4
public abstract class ClientItemProvider extends JsonCodecProvider<ClientItemDefinition>
{
    public ClientItemProvider(PackOutput output, CompletableFuture lookupProvider, ExistingFileHelper existingFileHelper)
    {
        super(output, PackOutput.Target.RESOURCE_PACK, "items", PackType.CLIENT_RESOURCES, ClientItemDefinition.CLIENT_ITEM_CODEC, lookupProvider, LeetTechMod.MOD_ID, existingFileHelper);
    }

    protected void addSimpleItem(DeferredItem<?> item)
    {
        addItemWithItemModel(item, item.getId());
    }

    protected void addSimpleBlockItem(DeferredItem<?> item)
    {
        addItemWithBlockModel(item, item.getId());
    }

    protected void addItemWithItemModel(DeferredItem<?> item, ResourceLocation modelLocation)
    {
        unconditional(item.getId(), ClientItemDefinition.fromResourceLocation(modelLocation, "item"));
    }

    protected void addItemWithBlockModel(DeferredItem<?> item, ResourceLocation modelLocation)
    {
        unconditional(item.getId(), ClientItemDefinition.fromResourceLocation(modelLocation, "block"));
    }

    @Override
    protected abstract void gather();
}