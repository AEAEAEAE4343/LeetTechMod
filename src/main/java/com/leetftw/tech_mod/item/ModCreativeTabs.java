package com.leetftw.tech_mod.item;

import com.leetftw.tech_mod.LeetTechMod;
import com.leetftw.tech_mod.block.ModBlocks;
import com.leetftw.tech_mod.item.upgrade.MachineUpgrade;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModCreativeTabs
{
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(BuiltInRegistries.CREATIVE_MODE_TAB, LeetTechMod.MOD_ID);

    public static final Supplier<CreativeModeTab> MOD_TAB = CREATIVE_MODE_TABS.register("items", () -> CreativeModeTab.builder()
            //Set the title of the tab. Don't forget to add a translation!
            .title(Component.translatable("itemGroup." + LeetTechMod.MOD_ID + ".items"))
            //Set the icon of the tab.
            .icon(() -> new ItemStack(ModItems.AESTHETIC_CRYSTAL.get()))
            //Add your items to the tab.
            .displayItems((params, output) ->
            {
                for (DeferredHolder<Item, ? extends Item> item : ModItems.ITEMS.getEntries())
                    output.accept(item.get());

                for (DeferredHolder<Item, ? extends Item> item : ModBlocks.BLOCK_ITEMS.getEntries())
                    output.accept(item.get());

                for (MachineUpgrade upgrade : MachineUpgrade.getRegisteredUpgrades(params.holders()))
                {
                    ItemStack stack = new ItemStack(ModItems.MACHINE_UPGRADE.get());
                    stack.set(ModDataComponents.MACHINE_UPGRADE, upgrade.getUpgradeId());
                    output.accept(stack);
                }
            })
            .build()
    );

    public static void register(IEventBus bus)
    {
        CREATIVE_MODE_TABS.register(bus);
    }
}
