package com.leetftw.tech_mod.gui;

import com.leetftw.tech_mod.block.ModBlocks;
import com.leetftw.tech_mod.block.entity.BaseLeetBlockEntity;
import com.leetftw.tech_mod.block.entity.CrystalInjectorBlockEntity;
import com.leetftw.tech_mod.block.multiblock.quarry.QuarryControllerBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.level.block.Block;

import java.util.Objects;

public class QuarryMenu extends BaseLeetMenu
{
    private final ContainerData data;
    private final ContainerData energyData;

    public QuarryMenu(int pContainerId, Inventory inv, FriendlyByteBuf buffer)
    {
        this(pContainerId, inv, ContainerLevelAccess.NULL, (QuarryControllerBlockEntity) Objects.requireNonNull(inv.player.level().getBlockEntity(buffer.readBlockPos())), new SimpleContainerData(6));
    }

    public QuarryMenu(int pContainerId, Inventory inv, ContainerLevelAccess access, QuarryControllerBlockEntity blockEntity, SimpleContainerData simpleContainerData)
    {
        super(ModMenuTypes.QUARRY_MENU.get(), pContainerId, access);

        data = simpleContainerData;
        energyData = blockEntity.getEnergyContainerData();

        addPlayerInventory(inv, 8, 84);
        addPlayerHotbar(inv, 8, 142);

        addSlot(blockEntity.getUpgradeSlot(0, 176 + 9, 16 + 3, BaseLeetBlockEntity.FilterType.RESPECT_EXISTING, BaseLeetBlockEntity.FilterType.RESPECT_EXISTING));
        addSlot(blockEntity.getUpgradeSlot(1, 176 + 27, 16 + 3, BaseLeetBlockEntity.FilterType.RESPECT_EXISTING, BaseLeetBlockEntity.FilterType.RESPECT_EXISTING));
        addSlot(blockEntity.getUpgradeSlot(2, 176 + 9, 34 + 3, BaseLeetBlockEntity.FilterType.RESPECT_EXISTING, BaseLeetBlockEntity.FilterType.RESPECT_EXISTING));
        addSlot(blockEntity.getUpgradeSlot(3, 176 + 27, 34 + 3, BaseLeetBlockEntity.FilterType.RESPECT_EXISTING, BaseLeetBlockEntity.FilterType.RESPECT_EXISTING));

        addDataSlots(simpleContainerData);
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

    public QuarryControllerBlockEntity.State getState()
    {
        return QuarryControllerBlockEntity.State.fromInteger(data.get(2));
    }

    public int getCurrentX()
    {
        return data.get(3);
    }

    public int getCurrentY()
    {
        return data.get(4);
    }

    public int getCurrentZ()
    {
        return data.get(5);
    }

    public int getEnergyStored()
    {
        return energyData.get(0);
    }

    public int getMaxEnergy()
    {
        return energyData.get(1);
    }

    @Override
    protected int getTeSlotCount()
    {
        return 0;
    }

    @Override
    protected Block getBlock()
    {
        return ModBlocks.QUARRY_CONTROLLER.get();
    }
}
