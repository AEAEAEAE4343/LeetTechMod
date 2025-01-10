package com.leetftw.tech_mod.datagen;

import com.leetftw.tech_mod.LeetTechMod;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@EventBusSubscriber(modid = LeetTechMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ModDataGenerators
{
    @SubscribeEvent
    public static void onGatherData(GatherDataEvent.Client event) throws ExecutionException, InterruptedException
    {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        generator.addProvider(true, new ModModelProvider(packOutput));
        generator.addProvider(true, new ModRecipeProvider(packOutput, lookupProvider));
        generator.addProvider(true, new LootTableProvider(packOutput, Set.of(), List.of(new LootTableProvider.SubProviderEntry(
                ModBlockLootTableProvider::new,
                LootContextParamSets.BLOCK // it makes sense to use BLOCK here
        )), lookupProvider));

        // Some merged textures need to be generated in the datagen pass
        // This is not intended, but makes it easier to share parts of textures
        // without having to modify all textures manually.
        generator.addProvider(true, new ModTextureProvider(packOutput));
    }
}
