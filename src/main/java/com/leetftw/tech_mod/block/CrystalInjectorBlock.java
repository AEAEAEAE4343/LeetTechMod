package com.leetftw.tech_mod.block;

import com.leetftw.tech_mod.block.entity.CrystalInjectorBlockEntity;
import com.leetftw.tech_mod.block.entity.ModBlockEntities;
import com.leetftw.tech_mod.fluid.ModFluids;
import com.leetftw.tech_mod.item.ModItems;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;

public class CrystalInjectorBlock extends HorizontalLeetEntityBlock
{
    public CrystalInjectorBlock(Properties properties)
    {
        super(properties);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult)
    {
        if (!level.isClientSide)
        {
            BlockEntity entity = level.getBlockEntity(pos);
            ServerPlayer serverPlayer = (ServerPlayer) player;
            if (entity instanceof CrystalInjectorBlockEntity blockEntity)
            {
                serverPlayer.openMenu(blockEntity, pos);
            }
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult)
    {
        if (stack.is(ModItems.LIQUID_AESTHETIC_BUCKET.get()) && hand == InteractionHand.MAIN_HAND)
        {
            IFluidHandler fluidHandler = level.getCapability(Capabilities.FluidHandler.BLOCK, pos, Direction.NORTH);
            if (fluidHandler != null)
            {
                int filledStack = fluidHandler.fill(new FluidStack(ModFluids.LIQUID_AESTHETIC.get(), 1000), IFluidHandler.FluidAction.SIMULATE);
                if (filledStack == 1000)
                {
                    if (level.isClientSide)
                        return InteractionResult.SUCCESS;

                    fluidHandler.fill(new FluidStack(ModFluids.LIQUID_AESTHETIC.get(), 1000), IFluidHandler.FluidAction.EXECUTE);
                    player.setItemInHand(hand, new ItemStack(Items.BUCKET, 1));
                    return InteractionResult.SUCCESS;
                }
            }
        }

        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
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
        return new CrystalInjectorBlockEntity(ModBlockEntities.CRYSTAL_INJECTOR_BE.get(), blockPos, blockState);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType)
    {
        if (level.isClientSide)
            return null;

        if (blockEntityType != ModBlockEntities.CRYSTAL_INJECTOR_BE.get())
            return null;

        return createTickerHelper(blockEntityType, ModBlockEntities.CRYSTAL_INJECTOR_BE.get(),
                (pLevel, pPos, pState, pBlockEntity) -> pBlockEntity.tick(pLevel, pPos, pState));
    }
}
