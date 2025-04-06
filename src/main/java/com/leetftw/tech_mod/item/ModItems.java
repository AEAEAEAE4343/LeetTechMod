package com.leetftw.tech_mod.item;

import com.leetftw.tech_mod.LeetTechMod;
import com.leetftw.tech_mod.fluid.ModFluids;
import com.leetftw.tech_mod.item.upgrade.MachineUpgrade;
import com.leetftw.tech_mod.item.upgrade.MachineUpgradeItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ToolMaterial;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems
{
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(LeetTechMod.MOD_ID);

    public static final DeferredItem<Item> AESTHETIC_DUST = ITEMS.registerSimpleItem("aesthetic_dust", new Item.Properties());
    public static final DeferredItem<Item> TINY_AESTHETIC_DUST_PILE = ITEMS.registerSimpleItem("tiny_aesthetic_dust_pile", new Item.Properties());

    public static final DeferredItem<Item> SHINY_REDSTONE = ITEMS.registerSimpleItem("shiny_redstone", new Item.Properties());

    public static final DeferredItem<Item> AESTHETIC_CRYSTAL = ITEMS.registerSimpleItem("aesthetic_crystal", new Item.Properties());

    public static final DeferredItem<AestheticHammerItem> AESTHETIC_HAMMER = ITEMS.registerItem("aesthetic_hammer",
            properties -> new AestheticHammerItem(ToolMaterial.DIAMOND, 8, -3.5f, properties));

    public static final DeferredItem<BucketItem> LIQUID_AESTHETIC_BUCKET = ITEMS.registerItem("liquid_aesthetic_bucket",
            properties -> new BucketItem(ModFluids.LIQUID_AESTHETIC.get(), properties),
            new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1));

    public static final DeferredItem<MachineUpgradeItem> MACHINE_UPGRADE = ITEMS.registerItem("machine_upgrade",
            properties -> new MachineUpgradeItem(properties.component(ModDataComponents.MACHINE_UPGRADE, MachineUpgrade.BLANK.getUpgradeId())));;

    public static void register(IEventBus bus)
    {
        ITEMS.register(bus);
    }
}
