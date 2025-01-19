package com.leetftw.tech_mod.compat;

import com.leetftw.tech_mod.item.ModDataComponents;
import com.leetftw.tech_mod.item.ModItems;
import me.shedaniel.rei.api.common.entry.comparison.ItemComparatorRegistry;
import me.shedaniel.rei.api.common.plugins.REICommonPlugin;
import me.shedaniel.rei.forge.REIPluginCommon;

@REIPluginCommon
public class REICommonCompat implements REICommonPlugin
{
    @Override
    public void registerItemComparators(ItemComparatorRegistry registry)
    {
        registry.register((context, stack) ->
                context.isExact() ? stack.hashCode() : stack.get(ModDataComponents.MACHINE_UPGRADE.get()).hashCode(), ModItems.MACHINE_UPGRADE.get());
    }
}
