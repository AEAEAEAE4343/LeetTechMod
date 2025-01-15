package com.leetftw.tech_mod.block;

import com.leetftw.tech_mod.LeetTechMod;
import com.leetftw.tech_mod.block.multiblock.StaticMultiBlockPart;
import com.leetftw.tech_mod.block.multiblock.energy_ring.EnergyRingControllerBlock;
import com.leetftw.tech_mod.block.multiblock.energy_ring.EnergyRingIOBlock;
import com.leetftw.tech_mod.block.multiblock.quarry.QuarryControllerBlock;
import com.leetftw.tech_mod.block.multiblock.quarry.QuarryFrameBlock;
import com.leetftw.tech_mod.fluid.ModFluids;
import com.leetftw.tech_mod.item.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

import static com.leetftw.tech_mod.item.ModDataComponents.ENERGY_CAPACITY;
import static com.leetftw.tech_mod.item.ModDataComponents.ENERGY_STORED;

public class ModBlocks
{
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(LeetTechMod.MOD_ID);

    private static Item.Properties createBEProperties(int stored, int capacity)
    {
        return new Item.Properties()
                .component(ENERGY_STORED.get(), stored)
                .component(ENERGY_CAPACITY.get(), capacity)
                .useBlockDescriptionPrefix();
    }
    private static Item.Properties createBEProperties(int capacity)
    {
        return createBEProperties(0, capacity);
    }
    private static Item.Properties createBEProperties()
    {
        return createBEProperties(0);
    }

    public static final DeferredBlock<Block> AESTHETIC_BLOCK = BLOCKS.registerSimpleBlock("aesthetic_block");
    public static final DeferredItem<BlockItem> AESTHETIC_BLOCK_ITEM = ModItems.ITEMS.registerSimpleBlockItem(AESTHETIC_BLOCK);

    public static final DeferredBlock<GemRefineryBlock> GEM_REFINERY = BLOCKS.registerBlock("gem_refinery",
            GemRefineryBlock::new);
    public static final DeferredItem<BlockItem> GEM_REFINERY_ITEM = ModItems.ITEMS.register("gem_refinery",
            registryName -> new BlockItem(GEM_REFINERY.get(),
                    createBEProperties().setId(ResourceKey.create(Registries.ITEM, registryName))));

    public static final DeferredBlock<CrystallizerBlock> CRYSTALLIZER = BLOCKS.registerBlock("crystallizer",
            CrystallizerBlock::new);
    public static final DeferredItem<BlockItem> CRYSTALLIZER_ITEM = ModItems.ITEMS.register("crystallizer",
            registryName -> new BlockItem(CRYSTALLIZER.get(),
                    createBEProperties().setId(ResourceKey.create(Registries.ITEM, registryName))));

    public static final DeferredBlock<CrystalInjectorBlock> CRYSTAL_INJECTOR = BLOCKS.registerBlock("crystal_injector",
            CrystalInjectorBlock::new);
    public static final DeferredItem<BlockItem> CRYSTAL_INJECTOR_ITEM = ModItems.ITEMS.register("crystal_injector",
            registryName -> new BlockItem(CRYSTAL_INJECTOR.get(),
                    createBEProperties().setId(ResourceKey.create(Registries.ITEM, registryName))));

    public static final DeferredBlock<EnergyStorageBlock> ENERGY_CELL = BLOCKS.registerBlock("energy_cell",
            properties -> new EnergyStorageBlock(properties, 1_000_000, 1000));
    public static final DeferredItem<BlockItem> ENERGY_CELL_ITEM = ModItems.ITEMS.register("energy_cell",
            registryName -> new BlockItem(ENERGY_CELL.get(),
                   createBEProperties(1_000_000).setId(ResourceKey.create(Registries.ITEM, registryName))));

    public static final DeferredBlock<EnergyStorageBlock> CREATIVE_ENERGY_CELL = BLOCKS.registerBlock("creative_energy_cell",
            properties -> new EnergyStorageBlock(properties, Integer.MAX_VALUE, 0));
    public static final DeferredItem<BlockItem> CREATIVE_ENERGY_CELL_ITEM = ModItems.ITEMS.register("creative_energy_cell",
            registryName -> new BlockItem(CREATIVE_ENERGY_CELL.get(),
                    createBEProperties(Integer.MAX_VALUE, Integer.MAX_VALUE).setId(ResourceKey.create(Registries.ITEM, registryName))));

    public static final DeferredBlock<BuddingAestheticBlock> BUDDING_AESTHETIC_BLOCK = BLOCKS.registerBlock("budding_aesthetic", BuddingAestheticBlock::new,
            BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_PINK)
                    .randomTicks()
                    .strength(1.5F)
                    .sound(SoundType.AMETHYST)
                    .requiresCorrectToolForDrops()
                    .pushReaction(PushReaction.DESTROY));
    public static final DeferredItem<BlockItem> BUDDING_AESTHETIC_BLOCK_ITEM = ModItems.ITEMS.registerSimpleBlockItem(BUDDING_AESTHETIC_BLOCK);

    public static final BlockBehaviour.Properties AESTHETIC_CLUSTER_BEHAVIOR = BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_PINK)
            .forceSolidOn()
            .noOcclusion()
            .sound(SoundType.AMETHYST_CLUSTER)
            .strength(1.5F)
            .lightLevel(blockState -> 5)
            .pushReaction(PushReaction.DESTROY);

    public static final DeferredBlock<AestheticClusterBlock> AESTHETIC_CLUSTER = BLOCKS.registerBlock("aesthetic_cluster",
            properties -> new AestheticClusterBlock(7, 3, properties), AESTHETIC_CLUSTER_BEHAVIOR);
    public static final DeferredItem<BlockItem> AESTHETIC_CLUSTER_ITEM = ModItems.ITEMS.registerSimpleBlockItem(AESTHETIC_CLUSTER);

    public static final DeferredBlock<AestheticClusterBlock> SMALL_AESTHETIC_BUD = BLOCKS.registerBlock("small_aesthetic_bud",
            properties -> new AestheticClusterBlock(3, 4, properties),
            AESTHETIC_CLUSTER_BEHAVIOR
                    .sound(SoundType.SMALL_AMETHYST_BUD)
                    .lightLevel(blockState -> 1));
    public static final DeferredItem<BlockItem> SMALL_AESTHETIC_BUD_ITEM = ModItems.ITEMS.registerSimpleBlockItem(SMALL_AESTHETIC_BUD);

    public static final DeferredBlock<AestheticClusterBlock> MEDIUM_AESTHETIC_BUD = BLOCKS.registerBlock("medium_aesthetic_bud",
            properties -> new AestheticClusterBlock(4, 3, properties),
            AESTHETIC_CLUSTER_BEHAVIOR
                    .sound(SoundType.MEDIUM_AMETHYST_BUD)
                    .lightLevel(blockState -> 2));
    public static final DeferredItem<BlockItem> MEDIUM_AESTHETIC_BUD_ITEM = ModItems.ITEMS.registerSimpleBlockItem(MEDIUM_AESTHETIC_BUD);

    public static final DeferredBlock<AestheticClusterBlock> LARGE_AESTHETIC_BUD = BLOCKS.registerBlock("large_aesthetic_bud",
            properties -> new AestheticClusterBlock(5, 3, properties),
            AESTHETIC_CLUSTER_BEHAVIOR
                    .sound(SoundType.LARGE_AMETHYST_BUD)
                    .lightLevel(blockState -> 4));
    public static final DeferredItem<BlockItem> LARGE_AESTHETIC_BUD_ITEM = ModItems.ITEMS.registerSimpleBlockItem(LARGE_AESTHETIC_BUD);

    public static final DeferredBlock<LiquidBlock> LIQUID_AESTHETIC_BLOCK = BLOCKS.registerBlock("liquid_aesthetic",
            properties -> new LiquidBlock(ModFluids.LIQUID_AESTHETIC.get(), properties),
            BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).noCollission().strength(100.0F).noLootTable());

    public static final DeferredBlock<EnergyRingControllerBlock> ENERGY_RING_CONTROLLER = BLOCKS.registerBlock("energy_ring_controller",
            EnergyRingControllerBlock::new);
    public static final DeferredItem<BlockItem> ENERGY_RING_CONTROLLER_ITEM = ModItems.ITEMS.register("energy_ring_controller",
            registryName -> new BlockItem(ENERGY_RING_CONTROLLER.get(),
                    createBEProperties().setId(ResourceKey.create(Registries.ITEM, registryName))));

    public static final DeferredBlock<StaticMultiBlockPart> ENERGY_RING_CASING = BLOCKS.registerBlock("energy_ring_casing",
            properties -> new StaticMultiBlockPart(properties.noOcclusion()));
    public static final DeferredItem<BlockItem> ENERGY_RING_CASING_ITEM = ModItems.ITEMS.registerSimpleBlockItem(ENERGY_RING_CASING);

    public static final DeferredBlock<EnergyRingIOBlock> ENERGY_RING_INPUT_PORT = BLOCKS.registerBlock("energy_ring_input_port",
            properties -> new EnergyRingIOBlock(properties.noOcclusion(), true));
    public static final DeferredItem<BlockItem> ENERGY_RING_INPUT_PORT_ITEM = ModItems.ITEMS.registerSimpleBlockItem(ENERGY_RING_INPUT_PORT);

    public static final DeferredBlock<EnergyRingIOBlock> ENERGY_RING_OUTPUT_PORT = BLOCKS.registerBlock("energy_ring_output_port",
            properties -> new EnergyRingIOBlock(properties.noOcclusion(), false));
    public static final DeferredItem<BlockItem> ENERGY_RING_OUTPUT_PORT_ITEM = ModItems.ITEMS.registerSimpleBlockItem(ENERGY_RING_OUTPUT_PORT);

    public static final DeferredBlock<QuarryControllerBlock> QUARRY_CONTROLLER = BLOCKS.registerBlock("quarry_controller",
            QuarryControllerBlock::new);
    public static final DeferredItem<BlockItem> QUARRY_CONTROLLER_ITEM = ModItems.ITEMS.register("quarry_controller",
            registryName -> new BlockItem(QUARRY_CONTROLLER.get(),
                    createBEProperties(100_000).setId(ResourceKey.create(Registries.ITEM, registryName))));

    public static final DeferredBlock<QuarryFrameBlock> QUARRY_FRAME = BLOCKS.registerBlock("quarry_frame",
            properties -> new QuarryFrameBlock(properties.noOcclusion().dynamicShape()));
    public static final DeferredItem<BlockItem> QUARRY_FRAME_ITEM = ModItems.ITEMS.registerSimpleBlockItem(QUARRY_FRAME);

    public static final List<DeferredBlock<? extends Block>> HAS_CUSTOM_ITEM_MODEL = List.of(AESTHETIC_CLUSTER, SMALL_AESTHETIC_BUD, MEDIUM_AESTHETIC_BUD, LARGE_AESTHETIC_BUD, LARGE_AESTHETIC_BUD);

    public static void register(IEventBus event)
    {
        BLOCKS.register(event);
    }
}
