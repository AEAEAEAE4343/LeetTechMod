package com.leetftw.tech_mod.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

public class BuddingAestheticBlock extends BuddingAmethystBlock
{
    private static final Direction[] DIRECTIONS = Direction.values();

    public BuddingAestheticBlock(Properties properties)
    {
        super(properties);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random)
    {
        // 1 in 3 random ticks make it grow
        if (random.nextInt(3) != 0)
            return;

        Direction direction = DIRECTIONS[random.nextInt(DIRECTIONS.length)];
        BlockPos neighbouringBlockPos = pos.relative(direction);
        BlockState currentBlockState = level.getBlockState(neighbouringBlockPos);

        // Only grow in light areas
        if (level.getRawBrightness(neighbouringBlockPos, 0) > 5)
            return;

        Block block = null;
        if (canClusterGrowAtState(currentBlockState))
        {
            block = ModBlocks.SMALL_AESTHETIC_BUD.get();
        }
        else if (currentBlockState.is(ModBlocks.SMALL_AESTHETIC_BUD) && currentBlockState.getValue(AmethystClusterBlock.FACING) == direction)
        {
            block = ModBlocks.MEDIUM_AESTHETIC_BUD.get();
        }
        else if (currentBlockState.is(ModBlocks.MEDIUM_AESTHETIC_BUD) && currentBlockState.getValue(AmethystClusterBlock.FACING) == direction)
        {
            block = ModBlocks.LARGE_AESTHETIC_BUD.get();
        }
        else if (currentBlockState.is(ModBlocks.LARGE_AESTHETIC_BUD) && currentBlockState.getValue(AmethystClusterBlock.FACING) == direction)
        {
            block = ModBlocks.AESTHETIC_CLUSTER.get();
        }

        if (block != null)
        {
            BlockState newBlockState = block.defaultBlockState()
                    .setValue(AmethystClusterBlock.FACING, direction)
                    .setValue(AmethystClusterBlock.WATERLOGGED, Boolean.valueOf(currentBlockState.getFluidState().getType() == Fluids.WATER));
            level.setBlockAndUpdate(neighbouringBlockPos, newBlockState);
        }
    }
}
