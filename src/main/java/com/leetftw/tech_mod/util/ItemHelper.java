package com.leetftw.tech_mod.util;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
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

    public static NonNullList<ItemStack> loadItemsFromTag(CompoundTag tag, HolderLookup.Provider registries, String name, int maxSlots)
    {
        NonNullList<ItemStack> returnVal = NonNullList.withSize(maxSlots, ItemStack.EMPTY);
        ListTag itemList = tag.getList(name, 10);
        itemList.forEach(element ->
        {
            if (!(element instanceof CompoundTag itemTags))
                return;

            int slot = itemTags.getInt("Slot");
            if (slot >= 0 && slot < maxSlots)
            {
                ItemStack.parse(registries, itemTags).ifPresent((stack) -> returnVal.set(slot, stack));
            }
        });
        return returnVal;
    }
}
