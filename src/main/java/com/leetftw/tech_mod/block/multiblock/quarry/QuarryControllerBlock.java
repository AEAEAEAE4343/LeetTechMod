package com.leetftw.tech_mod.block.multiblock.quarry;

import com.leetftw.tech_mod.block.BaseLeetEntityBlock;
import com.leetftw.tech_mod.block.entity.ModBlockEntities;
import com.leetftw.tech_mod.block.multiblock.StaticMultiBlockPart;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class QuarryControllerBlock extends BaseLeetEntityBlock
{
    public QuarryControllerBlock(Properties properties)
    {
        super(properties);

        registerDefaultState(getStateDefinition().any().setValue(StaticMultiBlockPart.FORMED, false));
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult)
    {
        InteractionResult returnVal = super.useWithoutItem(state, level, pos, player, hitResult);

        if (!level.isClientSide) {
            QuarryControllerBlockEntity be = (QuarryControllerBlockEntity) level.getBlockEntity(pos);
            boolean result = be.checkFormed(level, pos);

           level.getServer().getPlayerList().getPlayers().forEach(serverPlayer -> serverPlayer.sendSystemMessage(Component.literal("Formed: " + result)));
        }

        return returnVal;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(StaticMultiBlockPart.FORMED);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return null;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new QuarryControllerBlockEntity(ModBlockEntities.QUARRY_CONTROLLER_BE.get(), blockPos, blockState);
    }
}
