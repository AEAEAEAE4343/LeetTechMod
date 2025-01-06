package com.leetftw.tech_mod.datagen;

import com.leetftw.tech_mod.LeetTechMod;
import com.leetftw.tech_mod.block.ModBlocks;
import com.leetftw.tech_mod.item.ModItems;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.JsonCodecProvider;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.concurrent.CompletableFuture;

class Model
{
    public static final Codec<Model> MODEL_CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codec.STRING.fieldOf("type").forGetter(Model::type),
                    Codec.STRING.fieldOf("model").forGetter(Model::model)
            ).apply(instance, Model::new)
    );

    private final String t;
    private final String m;

    public Model(String type, String model)
    {
        t = type;
        m = model;
    }

    public static Model fromResourceLocation(ResourceLocation location, String path)
    {
        return new Model("minecraft:model",
                path == null ? location.withPrefix("item/").toString()
                : location.withPrefix(path + "/").toString());
    }

    public String type() { return t; }
    public String model() { return m; }
}

class ClientItem
{
    public static final Codec<ClientItem> CLIENT_ITEM_CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Model.MODEL_CODEC.fieldOf("model").forGetter(ClientItem::model)
            ).apply(instance, ClientItem::new)
    );

    private final Model m;

    public ClientItem(Model model)
    {
        m = model;
    }

    public static ClientItem fromResourceLocation(ResourceLocation location, String path)
    {
        return new ClientItem(Model.fromResourceLocation(location, path));
    }

    public Model model() { return m; }
}

public class ModClientItemsProvider extends JsonCodecProvider<ClientItem>
{
    public ModClientItemsProvider(PackOutput output, CompletableFuture lookupProvider, ExistingFileHelper existingFileHelper)
    {
        super(output, PackOutput.Target.RESOURCE_PACK, "items", PackType.CLIENT_RESOURCES, ClientItem.CLIENT_ITEM_CODEC, lookupProvider, LeetTechMod.MOD_ID, existingFileHelper);
    }

    private void addSimpleItem(DeferredItem<?> item)
    {
        unconditional(item.getId(), ClientItem.fromResourceLocation(item.getId(), "item"));
    }

    private void addSimpleBlockItem(DeferredItem<?> item)
    {
        unconditional(item.getId(), ClientItem.fromResourceLocation(item.getId(), "block"));
    }

    @Override
    protected void gather()
    {
        for (DeferredItem<Item> item : ModItems.SIMPLE_MODEL_ITEMS)
            addSimpleItem(item);

        addSimpleItem(ModBlocks.SMALL_AESTHETIC_BUD_ITEM);
        addSimpleItem(ModBlocks.MEDIUM_AESTHETIC_BUD_ITEM);
        addSimpleItem(ModBlocks.LARGE_AESTHETIC_BUD_ITEM);
        addSimpleItem(ModBlocks.AESTHETIC_ClUSTER_ITEM);

        addSimpleBlockItem(ModBlocks.AESTHETIC_BLOCK_ITEM);
        addSimpleBlockItem(ModBlocks.BUDDING_AESTHETIC_BLOCK_ITEM);
        addSimpleBlockItem(ModBlocks.GEM_REFINERY_ITEM);
        addSimpleBlockItem(ModBlocks.CRYSTALLIZER_ITEM);
        addSimpleBlockItem(ModBlocks.CRYSTAL_INJECTOR_ITEM);

        unconditional(ModBlocks.ENERGY_CELL_ITEM.getId(), ClientItem.fromResourceLocation(ModBlocks.ENERGY_CELL_ITEM.getId().withSuffix("_front_0"), "block"));
        unconditional(ModBlocks.CREATIVE_ENERGY_CELL_ITEM.getId(), ClientItem.fromResourceLocation(ModBlocks.CREATIVE_ENERGY_CELL_ITEM.getId().withSuffix("_front_12"), "block"));
    }
}
