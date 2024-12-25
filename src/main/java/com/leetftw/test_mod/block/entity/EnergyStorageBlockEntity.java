package com.leetftw.test_mod.block.entity;

import com.leetftw.test_mod.block.EnergyStorageBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EnergyStorageBlockEntity extends BaseLeetBlockEntity
{
    private int capacity;
    private int transfer;
    private boolean creative;

    public void pushPower(IEnergyStorage storage)
    {
        int insertedStorage = storage.receiveEnergy(energyGetStored(), false);
        if (!creative) energySetStored(energyGetStored() - insertedStorage);
    }

    public void tick(Level pLevel, BlockPos pPos, BlockState pState)
    {
        if (!pLevel.isClientSide() && level.getCapability(Capabilities.EnergyStorage.BLOCK, pPos.relative(Direction.EAST), Direction.WEST) instanceof IEnergyStorage storage) pushPower(storage);
        if (!pLevel.isClientSide() && level.getCapability(Capabilities.EnergyStorage.BLOCK, pPos.relative(Direction.WEST), Direction.EAST) instanceof IEnergyStorage storage) pushPower(storage);
        if (!pLevel.isClientSide() && level.getCapability(Capabilities.EnergyStorage.BLOCK, pPos.relative(Direction.NORTH), Direction.SOUTH) instanceof IEnergyStorage storage) pushPower(storage);
        if (!pLevel.isClientSide() && level.getCapability(Capabilities.EnergyStorage.BLOCK, pPos.relative(Direction.SOUTH), Direction.NORTH) instanceof IEnergyStorage storage) pushPower(storage);
    }

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
