package com.leetftw.tech_mod.datagen;

import com.leetftw.tech_mod.LeetTechMod;
import com.leetftw.tech_mod.block.ModBlocks;
import com.leetftw.tech_mod.item.ModDataComponents;
import com.leetftw.tech_mod.item.ModItems;
import com.leetftw.tech_mod.item.upgrade.MachineUpgrade;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.critereon.ImpossibleTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider
{
    protected ModRecipeProvider(HolderLookup.Provider registries, RecipeOutput packOutput)
    {
        super(registries, packOutput);
    }

    protected ShapedRecipeBuilder shaped(RecipeCategory category, ItemStack result)
    {
        return ShapedRecipeBuilder.shaped(registries.lookupOrThrow(Registries.ITEM), category, result);
    }

    protected <T> DataComponentPredicate componentPredicate(DataComponentType<T> component, T value)
    {
        return DataComponentPredicate.builder().expect(component, value).build();
    }

    protected <T> Ingredient predicateIngredient(ItemLike item, DataComponentType<T> component, T value)
    {
        return DataComponentIngredient.of(true, componentPredicate(component, value), item);
    }

    protected <T> ItemPredicate itemPredicate(ItemLike item, DataComponentType<T> component, T value)
    {
        return ItemPredicate.Builder.item().of(registries.lookupOrThrow(Registries.ITEM), item)
                .hasComponents(componentPredicate(component, value)).build();
    }

    private Criterion<ImpossibleTrigger.TriggerInstance> impossible()
    {
        return CriteriaTriggers.IMPOSSIBLE.createCriterion(new ImpossibleTrigger.TriggerInstance());
    }

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

        // MACHINE UPGRADE - SPEED
        shaped(RecipeCategory.MISC, new ItemStack(ModItems.MACHINE_UPGRADE, 1,
                DataComponentPatch.builder().set(ModDataComponents.MACHINE_UPGRADE.get(), modLoc("speed_tier_1")).build()))
                .define('U', predicateIngredient(ModItems.MACHINE_UPGRADE, ModDataComponents.MACHINE_UPGRADE.get(), MachineUpgrade.BLANK_KEY))
                .define('C', ModItems.AESTHETIC_CRYSTAL)
                .define('S', Items.SUGAR)
                .pattern("SCS")
                .pattern("CUC")
                .pattern("SCS")
                .unlockedBy("impossible", impossible())
                //.unlockedBy("has_machine_upgrade", has(ModItems.MACHINE_UPGRADE))
                .save(output, "leet_tech:machine_upgrade_speed_tier_1");

        shaped(RecipeCategory.MISC, new ItemStack(ModItems.MACHINE_UPGRADE, 1,
                DataComponentPatch.builder().set(ModDataComponents.MACHINE_UPGRADE.get(), modLoc("speed_tier_2")).build()))
                .define('U', predicateIngredient(ModItems.MACHINE_UPGRADE, ModDataComponents.MACHINE_UPGRADE.get(), modLoc("speed_tier_1")))
                .define('C', ModItems.AESTHETIC_CRYSTAL)
                .define('E', Items.EMERALD)
                .define('S', Items.SUGAR)
                .pattern("SCE")
                .pattern("UCU")
                .pattern("ECS")
                .unlockedBy("impossible", impossible())
                .save(output, "leet_tech:machine_upgrade_speed_tier_2");

        shaped(RecipeCategory.MISC, new ItemStack(ModItems.MACHINE_UPGRADE, 1,
                DataComponentPatch.builder().set(ModDataComponents.MACHINE_UPGRADE.get(), modLoc("speed_tier_3")).build()))
                .define('U', predicateIngredient(ModItems.MACHINE_UPGRADE, ModDataComponents.MACHINE_UPGRADE.get(), modLoc("speed_tier_2")))
                .define('C', ModItems.AESTHETIC_CRYSTAL)
                .define('D', Items.DIAMOND)
                .define('S', Items.SUGAR)
                .pattern("SCD")
                .pattern("UCU")
                .pattern("DCS")
                .unlockedBy("impossible", impossible())
                .save(output, "leet_tech:machine_upgrade_speed_tier_3");

        shaped(RecipeCategory.MISC, new ItemStack(ModItems.MACHINE_UPGRADE, 1,
                DataComponentPatch.builder().set(ModDataComponents.MACHINE_UPGRADE.get(), modLoc("speed_tier_4")).build()))
                .define('U', predicateIngredient(ModItems.MACHINE_UPGRADE, ModDataComponents.MACHINE_UPGRADE.get(), modLoc("speed_tier_3")))
                .define('C', ModItems.AESTHETIC_CRYSTAL)
                .define('N', Items.NETHERITE_INGOT)
                .define('S', Items.SUGAR)
                .pattern("SCN")
                .pattern("UCU")
                .pattern("NCS")
                .unlockedBy("impossible", impossible())
                .save(output, "leet_tech:machine_upgrade_speed_tier_4");

        // MACHINE UPGRADE - EFFICIENCY
        shaped(RecipeCategory.MISC, new ItemStack(ModItems.MACHINE_UPGRADE, 1,
                DataComponentPatch.builder().set(ModDataComponents.MACHINE_UPGRADE.get(), modLoc("energy_tier_1")).build()))
                .define('U', predicateIngredient(ModItems.MACHINE_UPGRADE, ModDataComponents.MACHINE_UPGRADE.get(), MachineUpgrade.BLANK_KEY))
                .define('R', ModItems.SHINY_REDSTONE)
                .define('S', Items.COPPER_INGOT)
                .pattern("SRS")
                .pattern("RUR")
                .pattern("SRS")
                .unlockedBy("impossible", impossible())
                .save(output, "leet_tech:machine_upgrade_energy_tier_1");

        shaped(RecipeCategory.MISC, new ItemStack(ModItems.MACHINE_UPGRADE, 1,
                DataComponentPatch.builder().set(ModDataComponents.MACHINE_UPGRADE.get(), modLoc("energy_tier_2")).build()))
                .define('U', predicateIngredient(ModItems.MACHINE_UPGRADE, ModDataComponents.MACHINE_UPGRADE.get(), modLoc("energy_tier_1")))
                .define('R', ModItems.SHINY_REDSTONE)
                .define('E', Items.EMERALD)
                .define('S', Items.COPPER_INGOT)
                .pattern("SRE")
                .pattern("RUR")
                .pattern("ERS")
                .unlockedBy("impossible", impossible())
                .save(output, "leet_tech:machine_upgrade_energy_tier_2");

        shaped(RecipeCategory.MISC, new ItemStack(ModItems.MACHINE_UPGRADE, 1,
                DataComponentPatch.builder().set(ModDataComponents.MACHINE_UPGRADE.get(), modLoc("energy_tier_3")).build()))
                .define('U', predicateIngredient(ModItems.MACHINE_UPGRADE, ModDataComponents.MACHINE_UPGRADE.get(), modLoc("energy_tier_2")))
                .define('R', ModItems.SHINY_REDSTONE)
                .define('D', Items.DIAMOND)
                .define('S', Items.COPPER_INGOT)
                .pattern("SRD")
                .pattern("RUR")
                .pattern("DRS")
                .unlockedBy("impossible", impossible())
                .save(output, "leet_tech:machine_upgrade_energy_tier_3");

        shaped(RecipeCategory.MISC, new ItemStack(ModItems.MACHINE_UPGRADE, 1,
                DataComponentPatch.builder().set(ModDataComponents.MACHINE_UPGRADE.get(), modLoc("energy_tier_4")).build()))
                .define('U', predicateIngredient(ModItems.MACHINE_UPGRADE, ModDataComponents.MACHINE_UPGRADE.get(), modLoc("energy_tier_3")))
                .define('R', ModItems.SHINY_REDSTONE)
                .define('N', Items.NETHERITE_INGOT)
                .define('S', Items.COPPER_INGOT)
                .pattern("SRN")
                .pattern("RUR")
                .pattern("NRS")
                .unlockedBy("impossible", impossible())
                .save(output, "leet_tech:machine_upgrade_energy_tier_4");

        oreCooking(RecipeSerializer.SMELTING_RECIPE, SmeltingRecipe::new, List.of(Items.DIAMOND), RecipeCategory.MISC, ModItems.TINY_AESTHETIC_DUST_PILE, 0.25f, 1600, "tiny_aesthetic_dust_pile", "_from_smelting");
        oreCooking(RecipeSerializer.BLASTING_RECIPE, BlastingRecipe::new, List.of(Items.DIAMOND), RecipeCategory.MISC, ModItems.TINY_AESTHETIC_DUST_PILE, 0.25f, 1600, "tiny_aesthetic_dust_pile", "_from_blasting");
    }

    private ResourceLocation modLoc(String path)
    {
        return ResourceLocation.fromNamespaceAndPath(LeetTechMod.MOD_ID, path);
    }

    public static class Runner extends RecipeProvider.Runner
    {
        public Runner(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> registries)
        {
            super(packOutput, registries);
        }

        @Override
        protected @NotNull RecipeProvider createRecipeProvider(HolderLookup.@NotNull Provider registries, @NotNull RecipeOutput output)
        {
            return new ModRecipeProvider(registries, output);
        }

        @Override
        public @NotNull String getName()
        {
            return "Recipe Provider (LeetTech)";
        }
    }
}
