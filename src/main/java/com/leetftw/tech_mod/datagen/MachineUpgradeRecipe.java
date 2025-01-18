package com.leetftw.tech_mod.datagen;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/*
public class MachineUpgradeRecipe extends CustomRecipe
{
    public MachineUpgradeRecipe(CraftingBookCategory category)
    {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput craftingInput, Level level)
    {
        if (craftingInput)
        return false;
    }

    @Override
    public ItemStack assemble(CraftingInput craftingInput, HolderLookup.Provider provider)
    {
        return null;
    }

    @Override
    public RecipeSerializer<MachineUpgradeRecipe> getSerializer()
    {
        return new Serializer();
    }

    public class Serializer implements RecipeSerializer<MachineUpgradeRecipe>
    {
        @Override
        public MapCodec<MachineUpgradeRecipe> codec()
        {
            return null;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, MachineUpgradeRecipe> streamCodec()
        {
            return null;
        }
    }
}
*/