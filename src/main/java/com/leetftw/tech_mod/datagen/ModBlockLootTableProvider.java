package com.leetftw.tech_mod.datagen;

import com.leetftw.tech_mod.block.ModBlocks;
import com.leetftw.tech_mod.item.ModItems;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.CopyComponentsFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.Set;

public class ModBlockLootTableProvider extends BlockLootSubProvider
{
    HolderLookup.Provider registryLookup;
    public ModBlockLootTableProvider(HolderLookup.Provider registries)
    {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
        registryLookup = registries;
    }

    private void blockEntityLootTable(DeferredHolder<Block, ? extends Block> registeredBlock)
    {
        // Block entities should drop with component data
        add(registeredBlock.get(),
                block -> LootTable.lootTable()
                        .withPool(LootPool.lootPool()
                                .add(LootItem.lootTableItem(registeredBlock.get())
                                        .apply(CopyComponentsFunction.copyComponents(CopyComponentsFunction.Source.BLOCK_ENTITY)))));
    }

    @Override
    protected void generate()
    {
        HolderLookup.RegistryLookup<Enchantment> enchantmentRegistry = this.registries.lookupOrThrow(Registries.ENCHANTMENT);
        dropSelf(ModBlocks.AESTHETIC_BLOCK.get());
        dropSelf(ModBlocks.BUDDING_AESTHETIC_BLOCK.get());
        dropSelf(ModBlocks.CREATIVE_ENERGY_CELL.get());

        dropSelf(ModBlocks.ENERGY_RING_CONTROLLER.get());
        dropSelf(ModBlocks.ENERGY_RING_INPUT_PORT.get());
        dropSelf(ModBlocks.ENERGY_RING_OUTPUT_PORT.get());
        dropSelf(ModBlocks.ENERGY_RING_CASING.get());
        dropSelf(ModBlocks.QUARRY_FRAME.get());

        blockEntityLootTable(ModBlocks.ENERGY_CELL);
        blockEntityLootTable(ModBlocks.GEM_REFINERY);
        blockEntityLootTable(ModBlocks.CRYSTALLIZER);
        blockEntityLootTable(ModBlocks.CRYSTAL_INJECTOR);

        // 1-2 aesthetic crystals
        // 1 aesthetic cluster (silk touch)
        // fortune
        add(ModBlocks.AESTHETIC_CLUSTER.get(),
                block -> createSilkTouchDispatchTable(block,
                        LootItem.lootTableItem(ModItems.AESTHETIC_CRYSTAL.get())
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 2)))
                        .apply(ApplyBonusCount.addUniformBonusCount(enchantmentRegistry.getOrThrow(Enchantments.FORTUNE)))));

        // These all can only be mined with silk touch
        add(ModBlocks.SMALL_AESTHETIC_BUD.get(),
                block -> createSilkTouchOnlyTable(ModBlocks.SMALL_AESTHETIC_BUD_ITEM.get()));
        add(ModBlocks.MEDIUM_AESTHETIC_BUD.get(),
                block -> createSilkTouchOnlyTable(ModBlocks.MEDIUM_AESTHETIC_BUD_ITEM.get()));
        add(ModBlocks.LARGE_AESTHETIC_BUD.get(),
                block -> createSilkTouchOnlyTable(ModBlocks.LARGE_AESTHETIC_BUD_ITEM.get()));
    }

    @Override
    protected Iterable<Block> getKnownBlocks()
    {
        return ModBlocks.BLOCKS.getEntries().stream().map(Holder::value)::iterator;
    }
}
