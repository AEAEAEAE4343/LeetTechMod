package com.leetftw.tech_mod.block.multiblock.energy_ring;

import com.leetftw.tech_mod.block.HorizontalLeetEntityBlock;
import com.leetftw.tech_mod.block.entity.ModBlockEntities;
import com.leetftw.tech_mod.block.multiblock.StaticMultiBlockPart;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.jetbrains.annotations.Nullable;

public class EnergyRingIOBlock extends HorizontalLeetEntityBlock
{
    final boolean input;

    public EnergyRingIOBlock(Properties properties)
    {
        this(properties, false);
    }

    public EnergyRingIOBlock(Properties properties, boolean input)
    {
        super(properties);
        this.input = input;
        registerDefaultState(getStateDefinition().any().setValue(StaticMultiBlockPart.FORMED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(StaticMultiBlockPart.FORMED);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state)
    {
        //return state.getValue(StaticMultiBlockPart.FORMED) ? RenderShape.INVISIBLE : RenderShape.MODEL;
        return RenderShape.MODEL;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return null;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState)
    {
        return new EnergyRingIOBlockEntity(ModBlockEntities.ENERGY_RING_IO_PORT_BE.get(), blockPos, blockState, input);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType)
    {
        if (level.isClientSide)
            return null;

        if (blockEntityType != ModBlockEntities.ENERGY_RING_IO_PORT_BE.get())
            return null;

        return createTickerHelper(blockEntityType, ModBlockEntities.ENERGY_RING_IO_PORT_BE.get(),
                (pLevel, pPos, pState, pBlockEntity) -> pBlockEntity.tick(pLevel, pPos, pState));
    }
}
