package com.leetftw.tech_mod.util;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class ItemHelper
{
    public static void saveItemsToTag(CompoundTag tag, HolderLookup.Provider registries, String name, Stream<ItemStack> items)
    {
        ListTag itemList = new ListTag();
        AtomicInteger i = new AtomicInteger();
        items.forEach(item ->
        {
            if (!item.isEmpty())
            {
                CompoundTag itemTag = new CompoundTag();
                itemTag.putInt("Slot", i.get());
                itemList.add(item.save(registries, itemTag));
            }
            i.getAndIncrement();
        });
        tag.put(name, itemList);
    }
}
