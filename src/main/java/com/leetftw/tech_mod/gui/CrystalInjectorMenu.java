package com.leetftw.tech_mod.gui;

import com.leetftw.tech_mod.block.ModBlocks;
import com.leetftw.tech_mod.block.entity.BaseLeetBlockEntity;
import com.leetftw.tech_mod.block.entity.CrystalInjectorBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.*;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.fluids.FluidStack;

public class CrystalInjectorMenu extends BaseLeetMenu
{
    private final ContainerData data;
    private final ContainerData fluidData;
    private final ContainerData energyData;

    public CrystalInjectorMenu(int pContainerId, Inventory inv, FriendlyByteBuf buffer)
    {
        this(pContainerId, inv, ContainerLevelAccess.NULL, (CrystalInjectorBlockEntity)inv.player.level().getBlockEntity(buffer.readBlockPos()), new SimpleContainerData(2));
    }

    public CrystalInjectorMenu(int pContainerId, Inventory inv, ContainerLevelAccess access, CrystalInjectorBlockEntity blockEntity, SimpleContainerData simpleContainerData)
    {
        super(ModMenuTypes.CRYSTAL_INJECTOR_MENU.get(), pContainerId, access);

        data = simpleContainerData;
        fluidData = blockEntity.getFluidContainerData();
        energyData = blockEntity.getEnergyContainerData();
        
        addPlayerInventory(inv, 8, 84);
        addPlayerHotbar(inv, 8, 142);

        addSlot(blockEntity.getUpgradeSlot(0, 176 + 9, 16 + 3, BaseLeetBlockEntity.FilterType.RESPECT_EXISTING, BaseLeetBlockEntity.FilterType.RESPECT_EXISTING));
        addSlot(blockEntity.getUpgradeSlot(1, 176 + 27, 16 + 3, BaseLeetBlockEntity.FilterType.RESPECT_EXISTING, BaseLeetBlockEntity.FilterType.RESPECT_EXISTING));
        addSlot(blockEntity.getUpgradeSlot(2, 176 + 9, 34 + 3, BaseLeetBlockEntity.FilterType.RESPECT_EXISTING, BaseLeetBlockEntity.FilterType.RESPECT_EXISTING));
        addSlot(blockEntity.getUpgradeSlot(3, 176 + 27, 34 + 3, BaseLeetBlockEntity.FilterType.RESPECT_EXISTING, BaseLeetBlockEntity.FilterType.RESPECT_EXISTING));

        addDataSlots(simpleContainerData);
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
    }

    public int getMaxFluid()
    {
        return fluidData.get(2);
    }

    @Override
    protected int getTeSlotCount()
    {
        return 4;
    }

    @Override
    protected Block getBlock()
    {
        return ModBlocks.CRYSTAL_INJECTOR.get();
    }
}
