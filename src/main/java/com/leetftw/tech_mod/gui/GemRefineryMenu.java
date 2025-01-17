package com.leetftw.tech_mod.gui;

import com.leetftw.tech_mod.block.ModBlocks;
import com.leetftw.tech_mod.block.entity.BaseLeetBlockEntity;
import com.leetftw.tech_mod.block.entity.GemRefineryBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.level.block.Block;

public class GemRefineryMenu extends BaseLeetMenu
{
    private final ContainerData data;

    public GemRefineryMenu(int pContainerId, Inventory inv, FriendlyByteBuf buffer)
    {
        this(pContainerId, inv, ContainerLevelAccess.NULL, (GemRefineryBlockEntity)inv.player.level().getBlockEntity(buffer.readBlockPos()), new SimpleContainerData(2));
    }

    public GemRefineryMenu(int pContainerId, Inventory inv, ContainerLevelAccess access, GemRefineryBlockEntity blockEntity, SimpleContainerData simpleContainerData)
    {
        super(ModMenuTypes.GEM_REFINERY_MENU.get(), pContainerId, access);

        data = simpleContainerData;

        addPlayerInventory(inv, 8, 84);
        addPlayerHotbar(inv, 8, 142);

        addSlot(blockEntity.getUpgradeSlot(0, 176 + 9, 16 + 3, BaseLeetBlockEntity.FilterType.RESPECT_EXISTING, BaseLeetBlockEntity.FilterType.RESPECT_EXISTING));
        addSlot(blockEntity.getUpgradeSlot(1, 176 + 27, 16 + 3, BaseLeetBlockEntity.FilterType.RESPECT_EXISTING, BaseLeetBlockEntity.FilterType.RESPECT_EXISTING));
        addSlot(blockEntity.getUpgradeSlot(2, 176 + 9, 34 + 3, BaseLeetBlockEntity.FilterType.RESPECT_EXISTING, BaseLeetBlockEntity.FilterType.RESPECT_EXISTING));
        addSlot(blockEntity.getUpgradeSlot(3, 176 + 27, 34 + 3, BaseLeetBlockEntity.FilterType.RESPECT_EXISTING, BaseLeetBlockEntity.FilterType.RESPECT_EXISTING));

        addSlot(blockEntity.getInventorySlot(0, 56, 17, BaseLeetBlockEntity.FilterType.RESPECT_EXISTING, BaseLeetBlockEntity.FilterType.ALWAYS_ALLOW));
        addSlot(blockEntity.getInventorySlot(1, 56, 53, BaseLeetBlockEntity.FilterType.RESPECT_EXISTING, BaseLeetBlockEntity.FilterType.ALWAYS_ALLOW));
        addSlot(blockEntity.getInventorySlot(2, 116, 35, BaseLeetBlockEntity.FilterType.ALWAYS_DENY, BaseLeetBlockEntity.FilterType.ALWAYS_ALLOW));

        addDataSlots(data);
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

    @Override
    protected int getTeSlotCount() {
        return 7;
    }

    @Override
    protected Block getBlock() {
        return ModBlocks.GEM_REFINERY.get();
    }
}
