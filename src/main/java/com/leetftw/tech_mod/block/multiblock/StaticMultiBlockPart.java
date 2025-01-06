package com.leetftw.tech_mod.block.multiblock;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class StaticMultiBlockPart extends Block
{
    public static final BooleanProperty FORMED = BooleanProperty.create("formed");
    private final boolean invisibleWhenFormed;

    public StaticMultiBlockPart(Properties properties, boolean invisibleWhenFormed)
    {
        super(properties);
        this.invisibleWhenFormed = invisibleWhenFormed;
        registerDefaultState(getStateDefinition().any().setValue(FORMED, false));
    }

    public StaticMultiBlockPart(Properties properties)
    {
        this(properties, true);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(FORMED);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state)
    {
        return invisibleWhenFormed && state.getValue(FORMED) ? RenderShape.INVISIBLE : RenderShape.MODEL;
    }
}
