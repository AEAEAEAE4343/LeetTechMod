package com.leetftw.tech_mod.item;

import com.leetftw.tech_mod.LeetTechMod;
import com.leetftw.tech_mod.fluid.ModFluids;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ToolMaterial;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.function.Function;

public class ModItems
{
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(LeetTechMod.MOD_ID);
    // TODO: Factor out this terrible hack by giving ModBlocks its own item register
    public static final ArrayList<DeferredItem<Item>> SIMPLE_MODEL_ITEMS = new ArrayList<>();

    public static final DeferredItem<Item> AESTHETIC_DUST = registerSimpleItemSimpleModel("aesthetic_dust", new Item.Properties());
    public static final DeferredItem<Item> TINY_AESTHETIC_DUST_PILE = registerSimpleItemSimpleModel("tiny_aesthetic_dust_pile", new Item.Properties());

    public static final DeferredItem<Item> AESTHETIC_CRYSTAL = registerSimpleItemSimpleModel("aesthetic_crystal", new Item.Properties());

    public static final DeferredItem<AestheticHammerItem> AESTHETIC_HAMMER = registerItemSimpleModel("aesthetic_hammer",
            properties -> new AestheticHammerItem(ToolMaterial.DIAMOND, 8, -3.5f, properties));

    public static final DeferredItem<BucketItem> LIQUID_AESTHETIC_BUCKET = registerItemSimpleModel("liquid_aesthetic_bucket",
            properties -> new BucketItem(ModFluids.LIQUID_AESTHETIC.get(), properties),
            new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1));

    public static DeferredItem<Item> registerSimpleItemSimpleModel(String id, Item.Properties properties)
    {
        DeferredItem<Item> registeredItem = ITEMS.registerSimpleItem(id, properties);
        SIMPLE_MODEL_ITEMS.add(registeredItem);
        return registeredItem;
    }

    public static <T extends Item> DeferredItem<T> registerItemSimpleModel(String id, Function<Item.Properties, T> func)
    {
        DeferredItem<T> registeredItem = ITEMS.registerItem(id, func);
        SIMPLE_MODEL_ITEMS.add((DeferredItem<Item>) registeredItem);
        return registeredItem;
    }

    public static <T extends Item> DeferredItem<T> registerItemSimpleModel(String id, Function<Item.Properties, T> func, Item.Properties properties)
    {
        DeferredItem<T> registeredItem = ITEMS.registerItem(id, func, properties);
        SIMPLE_MODEL_ITEMS.add((DeferredItem<Item>) registeredItem);
        return registeredItem;
    }

    public static void register(IEventBus bus)
    {
        ITEMS.register(bus);
    }
}
