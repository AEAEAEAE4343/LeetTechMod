package com.leetftw.tech_mod.block.entity;

import com.leetftw.tech_mod.block.EnergyStorageBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EnergyStorageBlockEntity extends BaseLeetBlockEntity
{
    private int capacity;
    private int transfer;
    private boolean creative;

    public EnergyStorageBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState, int capacity, int transfer)
    {
        super(type, pos, blockState);
        this.capacity = capacity;
        this.transfer = transfer;
        creative = capacity == Integer.MAX_VALUE;
        energySetStored(creative ? Integer.MAX_VALUE : 0);
    }

    private void setBlockFillState()
    {
        // Should not be possible but wth
        if (level == null || level.isClientSide) return;

        int retVal = (int)((energyGetStored() * 12L + (energyGetCapacity() / 2)) / energyGetCapacity());
        level.setBlockAndUpdate(worldPosition, getBlockState().setValue(EnergyStorageBlock.FILL_STATE, retVal));
    }

    @Override
    public void setChanged()
    {
        super.setChanged();

        if (energyGetCapacity() > 0)
            setBlockFillState();
    }

    @Override
    protected int itemsGetSlotCount() {
        return 0;
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
        return 0;
    }

    @Override
    protected int fluidsGetSlotCapacity(int i) {
        return 0;
    }

    @Override
    protected boolean fluidsAllowInsert(int slot, Fluid fluid) {
        return false;
    }

    @Override
    protected boolean fluidsAllowExtract(int slot) {
        return false;
    }

    @Override
    protected int energyGetCapacity() {
        return capacity;
    }

    @Override
    protected int energyGetStored() {
        return creative ? capacity : super.energyGetStored();
    }

    @Override
    protected boolean energyAllowInsert() {
        return !creative;
    }

    @Override
    protected boolean energyAllowExtract() {
        return true;
    }

    @Override
    protected int energyGetTransferRate() {
        return creative ? capacity : transfer;
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int i, @NotNull Inventory inventory, @NotNull Player player) {
        return null;
    }
}
