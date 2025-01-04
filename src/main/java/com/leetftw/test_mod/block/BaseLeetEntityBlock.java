package com.leetftw.test_mod.block;

import com.leetftw.test_mod.LeetTechMod;
import com.leetftw.test_mod.block.entity.BaseLeetBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.leetftw.test_mod.block.entity.codec.BaseLeetBlockEntityCodecs.*;

public abstract class BaseLeetEntityBlock extends BaseEntityBlock
{
    public BaseLeetEntityBlock(Properties properties)
    {
        super(properties);
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player)
    {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof BaseLeetBlockEntity leetBlockEntity)
        {
            ArrayList<ItemStack> drops = leetBlockEntity.itemsClear();
            SimpleContainer inventory = new SimpleContainer(drops.size());
            for(int i = 0; i < drops.size(); i++)
            {
                inventory.setItem(i, drops.get(i));
            }
            Containers.dropContents(level, pos, inventory);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    public static String energyToString(int fe)
    {
        // We assume billion FE is the largest for int
        final List<String> strings = List.of("FE", "kFE", "mFE", "bFE");
        int index = 0;
        float feFloat = fe;
        while (feFloat >= 1000)
        {
            index++;
            feFloat /= 1000;
        }
        feFloat = Math.round(feFloat * 100.0f) / 100.0f;
        return feFloat + strings.get(index);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag)
    {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        int energyStored = stack.getOrDefault(ENERGY_STORED, -1);
        int energyCapacity = stack.getOrDefault(ENERGY_CAPACITY, -1);
        if (energyStored != -1 && energyCapacity >= 1)
            tooltipComponents.add(Component.literal(
                    Component.translatable("tooltip." + LeetTechMod.MOD_ID + ".block_entity_energy_tooltip").getString()
                            + energyToString(energyStored) + " / " + energyToString(energyCapacity)));
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType)
    {
        if (level.isClientSide)
            return null;

        return createTickerHelper(blockEntityType, blockEntityType,
                (pLevel, pPos, pState, pBlockEntity) ->
                {
                    if (pBlockEntity instanceof BaseLeetBlockEntity baseLeetBlockEntity)
                        baseLeetBlockEntity.tick(pLevel, pPos, pState);
                });
    }
}
