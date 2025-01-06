package com.leetftw.tech_mod.fluid;


import com.leetftw.tech_mod.block.ModBlocks;
import com.leetftw.tech_mod.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.fluids.FluidType;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public abstract class LiquidAestheticFluid extends FlowingFluid
{
    @Override
    public @NotNull Fluid getFlowing()
    {
        return ModFluids.LIQUID_AESTHETIC_FLOWING.get();
    }

    @Override
    public @NotNull Fluid getSource()
    {
        return ModFluids.LIQUID_AESTHETIC.get();
    }

    @Override
    public @NotNull Item getBucket()
    {
        return ModItems.LIQUID_AESTHETIC_BUCKET.get();
    }

    @Override
    protected boolean canConvertToSource(ServerLevel pLevel)
    {
        return false;
    }

    @Override
    protected void beforeDestroyingBlock(LevelAccessor level, BlockPos pos, BlockState state)
    {
        BlockEntity blockentity = state.hasBlockEntity() ? level.getBlockEntity(pos) : null;
        Block.dropResources(state, level, pos, blockentity);
    }

    @Override
    public int getSlopeFindDistance(LevelReader level)
    {
        return 4;
    }

    @Override
    public int getDropOff(LevelReader level)
    {
        return 1;
    }

    @Override
    public int getTickDelay(LevelReader level)
    {
        return 5;
    }

    @Override
    protected float getExplosionResistance()
    {
        return 100.0F;
    }

    @Override
    public boolean canBeReplacedWith(FluidState fluidState, BlockGetter blockReader, BlockPos pos, Fluid fluid, Direction direction)
    {
        return direction == Direction.DOWN && fluid.getFluidType() == ModFluids.LIQUID_AESTHETIC_TYPE.get();
    }

    @Override
    public Optional<SoundEvent> getPickupSound()
    {
        return Optional.of(SoundEvents.BUCKET_FILL);
    }

    @Override
    public BlockState createLegacyBlock(FluidState state)
    {
        return ModBlocks.LIQUID_AESTHETIC_BLOCK.get().defaultBlockState().setValue(LiquidBlock.LEVEL, getLegacyLevel(state));
    }

    @Override
    public boolean isSame(Fluid fluid)
    {
        return fluid == ModFluids.LIQUID_AESTHETIC.get() || fluid == ModFluids.LIQUID_AESTHETIC_FLOWING.get();
    }

    @Override
    public FluidType getFluidType()
    {
        return ModFluids.LIQUID_AESTHETIC_TYPE.get();
    }

    public static class Flowing extends LiquidAestheticFluid
    {
        protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder)
        {
            super.createFluidStateDefinition(builder);
            builder.add(new Property[]{LEVEL});
        }

        public int getAmount(FluidState state)
        {
            return (Integer)state.getValue(LEVEL);
        }

        public boolean isSource(FluidState state)
        {
            return false;
        }
    }

    public static class Source extends LiquidAestheticFluid
    {
        public int getAmount(FluidState state)
        {
            return 8;
        }

        public boolean isSource(FluidState state)
        {
            return true;
        }
    }
}