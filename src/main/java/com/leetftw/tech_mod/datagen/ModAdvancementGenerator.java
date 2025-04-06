package com.leetftw.tech_mod.datagen;

import com.leetftw.tech_mod.LeetTechMod;
import com.leetftw.tech_mod.block.ModBlocks;
import com.leetftw.tech_mod.item.ModItems;
import net.minecraft.advancements.*;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;

import java.util.List;
import java.util.function.Consumer;

public class ModAdvancementGenerator implements AdvancementSubProvider
{
    private ResourceLocation modLoc(String path)
    {
        return ResourceLocation.fromNamespaceAndPath(LeetTechMod.MOD_ID, path);
    }

    @Override
    public void generate(HolderLookup.Provider provider, Consumer<AdvancementHolder> consumer)
    {
        // MAIN TAB
        // TODO: Create builder for display, because I hate how this code looks
        // TODO: Translatable components
        Advancement.Builder.advancement()
                .display(ModItems.AESTHETIC_CRYSTAL,
                        Component.literal("LeetTech"),
                        Component.literal("Goals for progressing through the mod"),
                        ResourceLocation.fromNamespaceAndPath("minecraft", "textures/gui/advancements/backgrounds/adventure.png"),
                        AdvancementType.TASK,
                        true, false, false)
                .addCriterion("has_diamond", InventoryChangeTrigger.TriggerInstance.hasItems(Items.DIAMOND))
                .save(consumer, modLoc("main/root"));

        Advancement.Builder.advancement()
                .parent(modLoc("main/root"))
                .display(ModItems.TINY_AESTHETIC_DUST_PILE,
                        Component.literal("Shiny!"),
                        Component.literal("Get your first piles of Aesthetic Dust by smelting a Diamond in a Furnace"),
                        null,
                        AdvancementType.TASK,
                        true, false, false)
                .addCriterion("has_tiny_dust", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.TINY_AESTHETIC_DUST_PILE))
                .save(consumer, modLoc("main/first_dust"));

        Advancement.Builder.advancement()
                .parent(modLoc("main/first_dust"))
                .display(ModBlocks.GEM_REFINERY_ITEM,
                        Component.literal("Better Deal"),
                        Component.literal("A Gem Refinery will get you one-to-one conversion of Diamonds and Glowstone Dust to Aesthetic Dust"),
                        null,
                        AdvancementType.TASK,
                        true, false, false)
                .addCriterion("has_tiny_dust", InventoryChangeTrigger.TriggerInstance.hasItems(ModBlocks.GEM_REFINERY_ITEM))
                .save(consumer, modLoc("main/gem_refinery"));

        // sidequest for shiny redstone
        Advancement.Builder.advancement()
                .parent(modLoc("main/gem_refinery"))
                .display(ModItems.SHINY_REDSTONE,
                        Component.literal("Beautiful Redstone"),
                        Component.literal("You can combine Aesthetic Dust and Redstone Dust to make Shiny Redstone"),
                        null,
                        AdvancementType.TASK,
                        true, false, false)
                .addCriterion("has_shiny_redstone", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.SHINY_REDSTONE))
                .save(consumer, modLoc("main/shiny_redstone"));

        Advancement.Builder.advancement()
                .parent(modLoc("main/gem_refinery"))
                .display(ModItems.AESTHETIC_CRYSTAL,
                        Component.literal("Crystals"),
                        Component.literal("Use a Crystallizer to get Aesthetic Crystals, the main source of Aesthetic in the mod"),
                        null,
                        AdvancementType.TASK,
                        true, false, false)
                .addCriterion("has_tiny_dust", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.AESTHETIC_CRYSTAL))
                .save(consumer, modLoc("main/crystals"));

        // sidequest for tools
        Advancement.Builder.advancement()
                .parent(modLoc("main/crystals"))
                .display(ModItems.AESTHETIC_HAMMER,
                        Component.literal("Tools"),
                        Component.literal("Create any Aesthetic tool (see LeetTech Tools tab)"),
                        null,
                        AdvancementType.GOAL,
                        true, false, false)
                .addCriterion("has_hammer", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.AESTHETIC_HAMMER))
                .requirements(AdvancementRequirements.allOf(List.of("has_hammer")))
                .save(consumer, modLoc("main/tools"));

        Advancement.Builder.advancement()
                .parent(modLoc("main/crystals"))
                .display(ModBlocks.CRYSTAL_INJECTOR_ITEM,
                        Component.literal("Even BETTER Deal!?"),
                        Component.literal("A Crystal Injector can convert Budding Amethyst into Budding Aesthetic, which can grow Aesthetic Crystals for you in dark areas"),
                        null,
                        AdvancementType.TASK,
                        true, false, false)
                .addCriterion("has_crystal_injector", InventoryChangeTrigger.TriggerInstance.hasItems(ModBlocks.CRYSTAL_INJECTOR_ITEM))
                .save(consumer, modLoc("main/crystal_injector"));

        // TOOLS TAB
        Advancement.Builder.advancement()
                .display(ModItems.AESTHETIC_HAMMER,
                        Component.literal("LeetTech Tools"),
                        Component.literal("The mod provides a bunch of cool tools you can explore"),
                        null,
                        AdvancementType.TASK,
                        true, false, false)
                .addCriterion("tool_recipe_unlocked", RecipeUnlockedTrigger.unlocked(ResourceKey.create(Registries.RECIPE, modLoc("aesthetic_hammer"))))
                .save(consumer, modLoc("tools/root"));

        Advancement.Builder.advancement()
                .parent(modLoc("tools/root"))
                .display(ModItems.AESTHETIC_HAMMER,
                        Component.literal("Aesthetic Hammer"),
                        Component.literal("Can mine a 3x3 area of blocks"),
                        null,
                        AdvancementType.TASK,
                        true, false, false)
                .addCriterion("has_hammer", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.AESTHETIC_HAMMER))
                .requirements(AdvancementRequirements.allOf(List.of("has_hammer")))
                .save(consumer, modLoc("tools/aesthetic_hammer"));
    }
}
