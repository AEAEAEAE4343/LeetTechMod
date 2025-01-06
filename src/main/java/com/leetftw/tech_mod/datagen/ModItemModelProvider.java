package com.leetftw.tech_mod.datagen;

import com.leetftw.tech_mod.LeetTechMod;
import com.leetftw.tech_mod.item.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredItem;

public class ModItemModelProvider extends ItemModelProvider
{
    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper)
    {
        super(output, LeetTechMod.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels()
    {
        for (DeferredItem<Item> item : ModItems.SIMPLE_MODEL_ITEMS)
            basicItem(item.get());
    }
}
