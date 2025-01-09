package com.leetftw.tech_mod.block.multiblock.quarry;

import com.leetftw.tech_mod.block.entity.BaseLeetBlockEntity;
import com.leetftw.tech_mod.block.multiblock.StaticMultiBlockPart;
import com.leetftw.tech_mod.item.MachineUpgradeItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class QuarryControllerBlockEntity extends BaseLeetBlockEntity
{
    private final int BASE_ENERGY_USAGE = 2048;
    private final int BASE_PROCESSING_TIME = 20;
    private int progress = 0;
    //private bool formed = false;

    public QuarryControllerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState)
    {
        super(type, pos, blockState);
    }

    @Override
    public void tick(Level pLevel, BlockPos pPos, BlockState pState)
    {
        super.tick(pLevel, pPos, pState);

        if (!pState.getValue(StaticMultiBlockPart.FORMED))
            return;

        // TODO: Upon formation set y-level to highest block
        //       Also save working area
        //       Then for each y level try mine every block
        //       Each block should take 20 ticks, except for minecraft:air (maybe a tenth of the time i.e. 2 ticks?)
        //       Also don't forget to consume power and Liquid Aesthetic
    }

    @Override
    protected int itemsGetSlotCount() {
        return 1;
    }

    @Override
    protected boolean itemsAllowInsert(int slot, Item stack) {
        return false;
    }

    @Override
    protected boolean itemsAllowExtract(int slot) {
        return false;
    }

    @Override
    protected boolean itemsSaveOnBreak() {
        return false;
    }

    @Override
    protected int fluidsGetSlotCount() {
        return 1;
    }

    @Override
    protected int fluidsGetSlotCapacity(int i) {
        return 4000;
    }

    @Override
    protected boolean fluidsAllowInsert(int slot, Fluid fluid) {
        return true;
    }

    @Override
    protected boolean fluidsAllowExtract(int slot) {
        return false;
    }

    @Override
    protected int energyGetCapacity() {
        return 100000;
    }

    @Override
    protected boolean energyAllowInsert() {
        return true;
    }

    @Override
    protected boolean energyAllowExtract() {
        return false;
    }

    @Override
    protected int energyGetTransferRate() {
        return 10000;
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int i, @NotNull Inventory inventory, @NotNull Player player) {
        return null;
    }
}
