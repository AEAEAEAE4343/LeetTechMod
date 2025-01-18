package com.leetftw.tech_mod.datagen;

import com.leetftw.tech_mod.LeetTechMod;
import com.leetftw.tech_mod.block.ModBlocks;
import com.leetftw.tech_mod.item.ModDataComponents;
import com.leetftw.tech_mod.item.ModItems;
import com.leetftw.tech_mod.item.upgrade.MachineUpgrade;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider.Runner
{
    protected ModRecipeProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> registries)
    {
        super(packOutput, registries);
    }

    private ResourceLocation modLoc(String path)
    {
        return ResourceLocation.fromNamespaceAndPath(LeetTechMod.MOD_ID, path);
    }

    @Override
    protected @NotNull RecipeProvider createRecipeProvider(HolderLookup.@NotNull Provider registries, @NotNull RecipeOutput output)
    {
        return new RecipeProvider(registries, output)
        {
            @Override
            protected void buildRecipes()
            {
                shaped(RecipeCategory.MISC, ModItems.AESTHETIC_DUST)
                        .define('T', ModItems.TINY_AESTHETIC_DUST_PILE)
                        .pattern("TT")
                        .pattern("TT")
                        .unlockedBy("has_tiny_aesthetic_dust_pile", has(ModItems.TINY_AESTHETIC_DUST_PILE))
                        .save(output);

                shaped(RecipeCategory.MISC, ModItems.AESTHETIC_HAMMER)
                        .define('S', Items.STICK)
                        .define('I', ModItems.AESTHETIC_CRYSTAL)
                        .pattern("III")
                        .pattern("ISI")
                        .pattern(" S ")
                        .unlockedBy("has_aesthetic_crystal", has(ModItems.AESTHETIC_CRYSTAL))
                        .save(output);

                shaped(RecipeCategory.MISC, ModBlocks.GEM_REFINERY_ITEM)
                        .define('F', Items.FURNACE)
                        .define('D', Items.DIAMOND)
                        .define('A', ModItems.AESTHETIC_DUST)
                        .pattern("DAD")
                        .pattern("AFA")
                        .pattern("DAD")
                        .unlockedBy("has_aesthetic_dust", has(ModItems.AESTHETIC_DUST))
                        .save(output);

                // TODO: Create helper class for Shaped Recipe with DataComponentIngredients instead of Items
                output.accept(ResourceKey.create(Registries.RECIPE, modLoc("machine_upgrade_speed_tier_1")),
                        new ShapedRecipe("", CraftingBookCategory.MISC, ShapedRecipePattern.of(Map.of(
                                        'B', DataComponentIngredient.of(true, DataComponentPredicate.builder().expect(ModDataComponents.MACHINE_UPGRADE.get(),
                                                MachineUpgrade.BLANK_KEY).build(), ModItems.MACHINE_UPGRADE.get()),
                                        'C', Ingredient.of(ModItems.AESTHETIC_CRYSTAL),
                                        'S', Ingredient.of(Items.SUGAR)),
                                "SCS",
                                "CBC",
                                "SCS"), new ItemStack(ModItems.MACHINE_UPGRADE, 1,
                                DataComponentPatch.builder().set(ModDataComponents.MACHINE_UPGRADE.get(), modLoc("speed_tier_1")).build())),
                        output.advancement().addCriterion("has_machine_upgrade", has(ModItems.MACHINE_UPGRADE)).build(modLoc("has_machine_upgrade")));

                oreCooking(RecipeSerializer.SMELTING_RECIPE, SmeltingRecipe::new, List.of(Items.DIAMOND), RecipeCategory.MISC, ModItems.TINY_AESTHETIC_DUST_PILE, 0.25f, 1600, "tiny_aesthetic_dust_pile", "_from_smelting");
                oreCooking(RecipeSerializer.BLASTING_RECIPE, BlastingRecipe::new, List.of(Items.DIAMOND), RecipeCategory.MISC, ModItems.TINY_AESTHETIC_DUST_PILE, 0.25f, 1600, "tiny_aesthetic_dust_pile", "_from_blasting");
            }
        };
    }

    @Override
    public @NotNull String getName()
    {
        return "Recipe Provider (LeetTech)";
    }
}
