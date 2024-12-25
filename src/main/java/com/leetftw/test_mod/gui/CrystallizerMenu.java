package com.leetftw.test_mod.gui;

import com.leetftw.test_mod.block.ModBlocks;
import com.leetftw.test_mod.block.entity.BaseLeetBlockEntity;
import com.leetftw.test_mod.block.entity.CrystallizerBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.fluids.FluidStack;

public class CrystallizerMenu extends BaseLeetMenu
{
    private final ContainerData data;
    private final ContainerData fluidData;
    private final ContainerData energyData;
    private final CrystallizerBlockEntity blockEntity;

    public CrystallizerMenu(int pContainerId, Inventory inv, FriendlyByteBuf buffer)
    {
        this(pContainerId, inv, ContainerLevelAccess.NULL, (CrystallizerBlockEntity)inv.player.level().getBlockEntity(buffer.readBlockPos()), new SimpleContainerData(2));
    }

    public CrystallizerMenu(int pContainerId, Inventory inv, ContainerLevelAccess access, CrystallizerBlockEntity blockEntity, SimpleContainerData simpleContainerData)
    {
        super(ModMenuTypes.CRYSTALLIZER_MENU.get(), pContainerId, access);

        this.blockEntity = blockEntity;
        data = simpleContainerData;
        fluidData = blockEntity.getFluidContainerData();
        energyData = blockEntity.getEnergyContainerData();

        addPlayerInventory(inv, 8, 84);
        addPlayerHotbar(inv, 8, 142);

        addSlot(blockEntity.getInventorySlot(0, 69, 35, BaseLeetBlockEntity.FilterType.RESPECT_EXISTING, BaseLeetBlockEntity.FilterType.ALWAYS_ALLOW));
        addSlot(blockEntity.getInventorySlot(1, 129, 35, BaseLeetBlockEntity.FilterType.ALWAYS_DENY, BaseLeetBlockEntity.FilterType.ALWAYS_ALLOW));

        addDataSlots(data);
        addDataSlots(fluidData);
        addDataSlots(energyData);
    }

    public boolean isCrafting()
    {
        return data.get(0) > 0;
    }

    public int getProgress()
    {
        return data.get(0);
    }

    public int getMaxProgress()
    {
        return data.get(1);
    }

    public int getEnergyStored()
    {
        return energyData.get(0);
    }

    public int getMaxEnergy()
    {
        return energyData.get(1);
    }

    public FluidStack getFluid()
    {
        int fluidId = fluidData.get(0);
        int fluidAmount = fluidData.get(1);
        return new FluidStack(BuiltInRegistries.FLUID.get(fluidId).get().value(), fluidAmount);
        //return blockEntity.getFluidHandler().getFluidInTank(0);
    }

    public int getMaxFluid()
    {
        return fluidData.get(2);
    }

    @Override
    protected int getTeSlotCount()
    {
        return 2;
    }

    @Override
    protected Block getBlock() {
        return ModBlocks.CRYSTALLIZER.get();
    }
}
