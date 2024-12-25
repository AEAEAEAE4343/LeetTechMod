package com.leetftw.test_mod.block;

import com.leetftw.test_mod.block.entity.GemRefineryBlockEntity;
import com.leetftw.test_mod.block.entity.ModBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

public class GemRefineryBlock extends HorizontalLeetEntityBlock
{
    public GemRefineryBlock(Properties properties)
    {
        super(properties);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return null;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult)
    {
        if (!level.isClientSide)
        {
            BlockEntity entity = level.getBlockEntity(pos);
            ServerPlayer serverPlayer = (ServerPlayer) player;
            if (entity instanceof GemRefineryBlockEntity blockEntity)
            {
                serverPlayer.openMenu(blockEntity, pos);
            }
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    @ParametersAreNonnullByDefault
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new GemRefineryBlockEntity(ModBlockEntities.GEM_REFINERY_BE.get(), blockPos, blockState);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType)
    {
        if (level.isClientSide)
            return null;

        if (blockEntityType != ModBlockEntities.GEM_REFINERY_BE.get())
            return null;

        return createTickerHelper(blockEntityType, ModBlockEntities.GEM_REFINERY_BE.get(),
                (pLevel, pPos, pState, pBlockEntity) -> pBlockEntity.tick(pLevel, pPos, pState));
    }
}
