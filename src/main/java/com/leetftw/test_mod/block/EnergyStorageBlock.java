package com.leetftw.test_mod.block;

import com.leetftw.test_mod.LeetTechMod;
import com.leetftw.test_mod.block.entity.EnergyStorageBlockEntity;
import com.leetftw.test_mod.block.entity.ModBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;

public class EnergyStorageBlock extends HorizontalLeetEntityBlock
{
    private final int capacity;
    private final int transfer;
    public static final IntegerProperty FILL_STATE = IntegerProperty.create("fill_state", 0, 12);

    public EnergyStorageBlock(Properties properties, int capacity, int transfer)
    {
        super(properties);
        this.capacity = capacity;
        this.transfer = transfer;

        registerDefaultState(getStateDefinition().any()
                .setValue(FILL_STATE, capacity == Integer.MAX_VALUE ? 12 : 0)
                .setValue(FACING, Direction.NORTH));
    }

    public int getCapacity() { return capacity; }
    public int getTransfer() { return transfer; }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult)
    {
        EnergyStorageBlockEntity blockEntity = (EnergyStorageBlockEntity) level.getBlockEntity(pos);
        LeetTechMod.LOGGER.debug("Energy stored in tile entity:" + blockEntity.getEnergyStorage().getEnergyStored());

        IEnergyStorage energyStorage = level.getCapability(Capabilities.EnergyStorage.BLOCK, pos, Direction.NORTH);
        if (energyStorage != null)
        {
            LeetTechMod.LOGGER.debug("Energy stored in IEnergyStorage:" + energyStorage.getEnergyStored());
        }

        return super.useWithoutItem(state, level, pos, player, hitResult);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(FILL_STATE);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state)
    {
        return RenderShape.MODEL;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec()
    {
        return null;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState)
    {
        return new EnergyStorageBlockEntity(ModBlockEntities.ENERGY_STORAGE_BE.get(), blockPos, blockState, capacity, transfer);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType)
    {
        if (level.isClientSide)
            return null;

        if (blockEntityType != ModBlockEntities.ENERGY_STORAGE_BE.get())
            return null;

        return createTickerHelper(blockEntityType, ModBlockEntities.ENERGY_STORAGE_BE.get(),
                (pLevel, pPos, pState, pBlockEntity) -> pBlockEntity.tick(pLevel, pPos, pState));
    }
}
