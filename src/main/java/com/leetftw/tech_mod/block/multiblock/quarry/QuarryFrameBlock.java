package com.leetftw.tech_mod.block.multiblock.quarry;

import com.leetftw.tech_mod.block.multiblock.StaticMultiBlockPart;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.DiscreteVoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class QuarryFrameBlock extends StaticMultiBlockPart
{
    public static final BooleanProperty NORTH_CON = BooleanProperty.create("north");
    public static final BooleanProperty EAST_CON = BooleanProperty.create("east");
    public static final BooleanProperty SOUTH_CON = BooleanProperty.create("south");
    public static final BooleanProperty WEST_CON = BooleanProperty.create("west");
    public static final BooleanProperty UP_CON = BooleanProperty.create("up");
    public static final BooleanProperty DOWN_CON = BooleanProperty.create("down");

    public QuarryFrameBlock(Properties properties)
    {
        super(properties);

        registerDefaultState(defaultBlockState()
                .setValue(FORMED, false)
                .setValue(NORTH_CON, false)
                .setValue(EAST_CON, false)
                .setValue(SOUTH_CON, false)
                .setValue(WEST_CON, false)
                .setValue(UP_CON, false)
                .setValue(DOWN_CON, false));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context)
    {
        VoxelShape shape = Shapes.box(5 / 16.0, 5 / 16.0, 5 / 16.0,
                11 / 16.0, 11 / 16.0, 11 / 16.0);
        if (state.getValue(NORTH_CON))
            shape = Shapes.or(shape, Shapes.box(5 / 16.0, 5 / 16.0, 0f,
                    11 / 16.0, 11 / 16.0, 5 / 16.0));
        if (state.getValue(EAST_CON))
            shape = Shapes.or(shape, Shapes.box(11 / 16.0, 5 / 16.0, 5 / 16.0,
                    1.0, 11 / 16.0, 11 / 16.0));
        if (state.getValue(SOUTH_CON))
            shape = Shapes.or(shape, Shapes.box(5 / 16.0, 5 / 16.0, 11 / 16.0,
                    11 / 16.0, 11 / 16.0, 1.0));
        if (state.getValue(WEST_CON))
            shape = Shapes.or(shape, Shapes.box(0, 5 / 16.0, 5 / 16.0,
                    5 / 16.0, 11 / 16.0, 11 / 16.0));
        if (state.getValue(UP_CON))
            shape = Shapes.or(shape, Shapes.box(5 / 16.0, 11 / 16.0, 5 / 16.0,
                    11 / 16.0, 1.0, 11 / 16.0));
        if (state.getValue(DOWN_CON))
            shape = Shapes.or(shape, Shapes.box(5 / 16.0, 0, 5 / 16.0,
                    11 / 16.0, 5 / 16.0, 11 / 16.0));
        return shape;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(NORTH_CON);
        builder.add(EAST_CON);
        builder.add(SOUTH_CON);
        builder.add(WEST_CON);
        builder.add(UP_CON);
        builder.add(DOWN_CON);
    }

    private BlockState getStateForPos(BlockState state, Level level, BlockPos pos)
    {
        if (level.getBlockState(pos.relative(Direction.NORTH)).getBlock() instanceof QuarryFrameBlock)
            state = state.setValue(NORTH_CON, true);
        if (level.getBlockState(pos.relative(Direction.EAST)).getBlock() instanceof QuarryFrameBlock)
            state = state.setValue(EAST_CON, true);
        if (level.getBlockState(pos.relative(Direction.SOUTH)).getBlock() instanceof QuarryFrameBlock)
            state = state.setValue(SOUTH_CON, true);
        if (level.getBlockState(pos.relative(Direction.WEST)).getBlock() instanceof QuarryFrameBlock)
            state = state.setValue(WEST_CON, true);
        if (level.getBlockState(pos.relative(Direction.UP)).getBlock() instanceof QuarryFrameBlock)
            state = state.setValue(UP_CON, true);
        if (level.getBlockState(pos.relative(Direction.DOWN)).getBlock() instanceof QuarryFrameBlock)
            state = state.setValue(DOWN_CON, true);
        return state;
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, @Nullable Orientation orientation, boolean movedByPiston)
    {
        level.setBlockAndUpdate(pos, getStateForPos(state, level, pos));
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context)
    {
        return getStateForPos(defaultBlockState(), context.getLevel(), context.getClickedPos());
    }
}
