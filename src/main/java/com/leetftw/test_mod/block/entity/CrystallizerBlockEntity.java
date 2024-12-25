package com.leetftw.test_mod.block.entity;

import com.leetftw.test_mod.item.ModItems;
import com.leetftw.test_mod.gui.CrystallizerMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

public class CrystallizerBlockEntity extends BaseLeetBlockEntity
{
    private static final int ITEM_SLOTS = 2;
    private static final int INPUT_SLOT = 0;
    private static final int OUTPUT_SLOT = 1;

    private static final int FLUID_CONTAINERS = 1;
    private static final int INPUT_CONTAINER = 0;

    protected final SimpleContainerData data;
    private int progress = 0;
    private int maxProgress = 200;

    public CrystallizerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState)
    {
        super(type, pos, blockState);

        this.data = new SimpleContainerData(2)
        {
            @Override
            public int get(int pIndex)
            {
                return switch (pIndex)
                {
                    case 0 -> progress;
                    case 1 -> maxProgress;
                    default -> 0;
                };
            }

            @Override
            public void set(int pIndex, int pValue)
            {
                switch (pIndex)
                {
                    case 0 -> progress = pValue;
                    case 1 -> maxProgress = pValue;
                }
            }
        };
    }

    public void tick(Level level, BlockPos pos, BlockState state)
    {
        if (hasRecipe())
        {
            if (energyGetStored() >= 128)
            {
                energySetStored(energyGetStored() - 128);
                setProgress(progress + 1);
                setChanged(level, pos, state);
            }

            if (progress >= maxProgress) {
                craftItem();
                setProgress(0);
                setChanged(level, pos, state);
            }
        }
        else setProgress(0);
    }

    private void craftItem()
    {
        ItemStack result = new ItemStack(ModItems.AESTHETIC_CRYSTAL.get(), 1);

        itemsGetSlot(INPUT_SLOT).shrink(1);
        fluidsGetSlot(INPUT_CONTAINER).shrink(1000);
        itemsSetSlot(OUTPUT_SLOT, new ItemStack(result.getItem(),
                itemsGetSlot(OUTPUT_SLOT).getCount() + result.getCount()));
    }

    private boolean hasRecipe()
    {
        boolean hasCraftingItem = itemsGetSlot(INPUT_SLOT).getItem() == ModItems.AESTHETIC_DUST.get()
                && fluidsGetSlot(INPUT_CONTAINER).getFluid() == Fluids.WATER
                && fluidsGetSlot(INPUT_CONTAINER).getAmount() >= 1000;

        ItemStack result = new ItemStack(ModItems.AESTHETIC_CRYSTAL.get());
        return hasCraftingItem && itemsCanInsertIntoSlot(OUTPUT_SLOT, result, true, false);
    }

    private void setProgress(int newProg)
    {
        progress = newProg;

        /*if (progress == 0) setItem(8, ItemStack.EMPTY);
        else setItem(8, new ItemStack(Items.DIRT, progress / 2));*/
    }

    @Override
    protected int itemsGetSlotCount()
    {
        return ITEM_SLOTS;
    }

    @Override
    protected boolean itemsAllowInsert(int slot, Item stack)
    {
        return slot == INPUT_SLOT;
    }

    @Override
    protected boolean itemsAllowExtract(int slot)
    {
        return slot == OUTPUT_SLOT;
    }

    @Override
    protected boolean itemsSaveOnBreak() {
        return false;
    }

    @Override
    protected int fluidsGetSlotCount()
    {
        return FLUID_CONTAINERS;
    }

    @Override
    protected int fluidsGetSlotCapacity(int i)
    {
        return 4000;
    }

    @Override
    protected boolean fluidsAllowInsert(int slot, Fluid fluid)
    {
        return fluid.isSame(Fluids.WATER);
    }

    @Override
    protected boolean fluidsAllowExtract(int slot)
    {
        return false;
    }

    @Override
    protected int energyGetCapacity() {
        return 25000;
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
        return 256;
    }

    @Override
    @ParametersAreNonnullByDefault
    protected void saveAdditional(CompoundTag pTag, HolderLookup.Provider registries)
    {
        super.saveAdditional(pTag, registries);
        pTag.putInt("crystallizer.progress", progress);
    }

    @Override
    @ParametersAreNonnullByDefault
    public void loadAdditional(CompoundTag pTag, HolderLookup.Provider registries)
    {
        super.loadAdditional(pTag, registries);
        progress = pTag.getInt("crystallizer.progress");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int i, Inventory inventory, Player player)
    {
        return new CrystallizerMenu(i, inventory, ContainerLevelAccess.create(player.level(), getBlockPos()), this, data);
    }
}
