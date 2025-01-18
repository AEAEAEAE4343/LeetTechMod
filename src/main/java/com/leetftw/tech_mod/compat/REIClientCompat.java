package com.leetftw.tech_mod.compat;

import com.leetftw.tech_mod.LeetTechMod;
import com.leetftw.tech_mod.item.ModItems;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.entry.CollapsibleEntryRegistry;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import me.shedaniel.rei.forge.REIPluginClient;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

@REIPluginClient
public class REIClientCompat implements REIClientPlugin
{
    @Override
    public void registerCollapsibleEntries(CollapsibleEntryRegistry registry)
    {
        registry.group(ResourceLocation.fromNamespaceAndPath(LeetTechMod.MOD_ID, "machine_upgrades"),
                Component.translatable("reiGroup.leet_tech.machine_upgrades"),
                entryStack -> entryStack.getType() == VanillaEntryTypes.ITEM
                        && ((EntryStack<ItemStack>)entryStack).getValue().is(ModItems.MACHINE_UPGRADE.get()));
    }
}
