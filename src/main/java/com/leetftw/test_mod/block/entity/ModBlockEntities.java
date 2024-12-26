package com.leetftw.test_mod.block.entity;

import com.leetftw.test_mod.LeetTechMod;
import com.leetftw.test_mod.block.EnergyStorageBlock;
import com.leetftw.test_mod.block.ModBlocks;
import com.leetftw.test_mod.block.multiblock.energy_ring.EnergyRingControllerBlockEntity;
import com.leetftw.test_mod.block.multiblock.energy_ring.EnergyRingIOBlockEntity;
import com.leetftw.test_mod.client.render.block.CrystalInjectorRenderer;
import com.leetftw.test_mod.client.render.block.EnergyRingRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Supplier;

@EventBusSubscriber(modid = LeetTechMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ModBlockEntities
{
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, LeetTechMod.MOD_ID);
    public static final Supplier<BlockEntityType<GemRefineryBlockEntity>> GEM_REFINERY_BE = BLOCK_ENTITY_TYPES.register(
            "gem_refinery_be",
            // The block entity type.
            () -> new BlockEntityType<>(
                    // The supplier to use for constructing the block entity instances.
                    new BlockEntityType.BlockEntitySupplier<>() {
                        @Override
                        @ParametersAreNonnullByDefault
                        public @NotNull GemRefineryBlockEntity create(BlockPos blockPos, BlockState blockState) {
                            return new GemRefineryBlockEntity(GEM_REFINERY_BE.get(), blockPos, blockState);
                        }
                    },
                    // A vararg of blocks that can have this block entity.
                    // This assumes the existence of the referenced blocks as DeferredBlock<Block>s.
                    ModBlocks.GEM_REFINERY.get()
            ));

    public static final Supplier<BlockEntityType<CrystallizerBlockEntity>> CRYSTALLIZER_BE = BLOCK_ENTITY_TYPES.register(
            "crystallizer_be",
            // The block entity type.
            () -> new BlockEntityType<>(
                    // The supplier to use for constructing the block entity instances.
                    new BlockEntityType.BlockEntitySupplier<>() {
                        @Override
                        @ParametersAreNonnullByDefault
                        public @NotNull CrystallizerBlockEntity create(BlockPos blockPos, BlockState blockState) {
                            return new CrystallizerBlockEntity(CRYSTALLIZER_BE.get(), blockPos, blockState);
                        }
                    },
                    // A vararg of blocks that can have this block entity.
                    // This assumes the existence of the referenced blocks as DeferredBlock<Block>s.
                    ModBlocks.CRYSTALLIZER.get()
            ));

    public static final Supplier<BlockEntityType<EnergyStorageBlockEntity>> ENERGY_STORAGE_BE = BLOCK_ENTITY_TYPES.register(
            "energy_storage_be",
            // The block entity type.
            () -> new BlockEntityType<>(
                    // The supplier to use for constructing the block entity instances.
                    new BlockEntityType.BlockEntitySupplier<>() {
                        @Override
                        @ParametersAreNonnullByDefault
                        public @NotNull EnergyStorageBlockEntity create(BlockPos blockPos, BlockState blockState) {
                            if (blockState.getBlock() instanceof EnergyStorageBlock energyStorageBlock)
                                return new EnergyStorageBlockEntity(ENERGY_STORAGE_BE.get(), blockPos, blockState, energyStorageBlock.getCapacity(), energyStorageBlock.getTransfer());
                            else return new EnergyStorageBlockEntity(ENERGY_STORAGE_BE.get(), blockPos, blockState, 0, 0);
                            //else throw new InvalidClassException("Class \"" + blockState.getBlock().getClass().getCanonicalName() + "\" does not implement or extend EnergyStorageBlock!");
                        }
                    },
                    // A vararg of blocks that can have this block entity.
                    // This assumes the existence of the referenced blocks as DeferredBlock<Block>s.
                    ModBlocks.ENERGY_CELL.get(),
                    ModBlocks.CREATIVE_ENERGY_CELL.get()
            ));

    public static final Supplier<BlockEntityType<CrystalInjectorBlockEntity>> CRYSTAL_INJECTOR_BE = BLOCK_ENTITY_TYPES.register(
            "crystal_injector_be",
            // The block entity type.
            () -> new BlockEntityType<>(
                    // The supplier to use for constructing the block entity instances.
                    new BlockEntityType.BlockEntitySupplier<>() {
                        @Override
                        @ParametersAreNonnullByDefault
                        public @NotNull CrystalInjectorBlockEntity create(BlockPos blockPos, BlockState blockState) {
                            return new CrystalInjectorBlockEntity(CRYSTAL_INJECTOR_BE.get(), blockPos, blockState);
                        }
                    },
                    // A vararg of blocks that can have this block entity.
                    // This assumes the existence of the referenced blocks as DeferredBlock<Block>s.
                    ModBlocks.CRYSTAL_INJECTOR.get()
            ));

    public static final Supplier<BlockEntityType<EnergyRingControllerBlockEntity>> ENERGY_RING_CONTROLLER_BE = BLOCK_ENTITY_TYPES.register(
            "energy_ring_controller_be",
            // The block entity type.
            () -> new BlockEntityType<>(
                    // The supplier to use for constructing the block entity instances.
                    new BlockEntityType.BlockEntitySupplier<>() {
                        @Override
                        @ParametersAreNonnullByDefault
                        public @NotNull EnergyRingControllerBlockEntity create(BlockPos blockPos, BlockState blockState) {
                            return new EnergyRingControllerBlockEntity(ENERGY_RING_CONTROLLER_BE.get(), blockPos, blockState);
                        }
                    },
                    // A vararg of blocks that can have this block entity.
                    // This assumes the existence of the referenced blocks as DeferredBlock<Block>s.
                    ModBlocks.ENERGY_RING_CONTROLLER.get()
            ));

    public static final Supplier<BlockEntityType<EnergyRingIOBlockEntity>> ENERGY_RING_IO_PORT_BE = BLOCK_ENTITY_TYPES.register(
            "energy_ring_io_port_be",
            // The block entity type.
            () -> new BlockEntityType<>(
                    // The supplier to use for constructing the block entity instances.
                    new BlockEntityType.BlockEntitySupplier<>() {
                        @Override
                        @ParametersAreNonnullByDefault
                        public @NotNull EnergyRingIOBlockEntity create(BlockPos blockPos, BlockState blockState) {
                            return new EnergyRingIOBlockEntity(ENERGY_RING_IO_PORT_BE.get(), blockPos, blockState);
                        }
                    },
                    // A vararg of blocks that can have this block entity.
                    // This assumes the existence of the referenced blocks as DeferredBlock<Block>s.
                    ModBlocks.ENERGY_RING_INPUT_PORT.get(),
                    ModBlocks.ENERGY_RING_OUTPUT_PORT.get()
            ));

    public static void register(IEventBus bus)
    {
        BLOCK_ENTITY_TYPES.register(bus);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event)
    {
        event.registerBlockEntityRenderer(CRYSTAL_INJECTOR_BE.get(), CrystalInjectorRenderer::new);
        event.registerBlockEntityRenderer(ENERGY_RING_CONTROLLER_BE.get(), EnergyRingRenderer::new);
    }

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event)
    {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK, // capability to register for
                ModBlockEntities.GEM_REFINERY_BE.get(), // block entity type to register for
                (blockEntity, side) -> blockEntity.getItemHandler()
        );

        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK, // capability to register for
                ModBlockEntities.CRYSTALLIZER_BE.get(), // block entity type to register for
                (blockEntity, side) -> blockEntity.getItemHandler()
        );

        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK, // capability to register for
                ModBlockEntities.CRYSTALLIZER_BE.get(), // block entity type to register for
                (blockEntity, side) -> blockEntity.getFluidHandler()
        );

        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK, // capability to register for
                ModBlockEntities.CRYSTALLIZER_BE.get(), // block entity type to register for
                (blockEntity, side) -> blockEntity.getEnergyStorage()
        );

        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK, // capability to register for
                ModBlockEntities.ENERGY_STORAGE_BE.get(), // block entity type to register for
                (blockEntity, side) -> blockEntity.getEnergyStorage()
        );

        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK, // capability to register for
                ModBlockEntities.CRYSTAL_INJECTOR_BE.get(), // block entity type to register for
                (blockEntity, side) -> blockEntity.getFluidHandler()
        );

        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK, // capability to register for
                ModBlockEntities.CRYSTAL_INJECTOR_BE.get(), // block entity type to register for
                (blockEntity, side) -> blockEntity.getEnergyStorage()
        );

        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                ModBlockEntities.ENERGY_RING_CONTROLLER_BE.get(),
                (blockEntity, side) -> blockEntity.getEnergyStorage()
        );

        event.registerBlockEntity(
                Capabilities.EnergyStorage.BLOCK,
                ModBlockEntities.ENERGY_RING_IO_PORT_BE.get(),
                (blockEntity, side) -> blockEntity.getEnergyStorage()
        );
    }
}
