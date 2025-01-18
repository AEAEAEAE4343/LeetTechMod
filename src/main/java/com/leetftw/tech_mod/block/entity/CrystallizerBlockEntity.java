package com.leetftw.tech_mod.block.entity;

import com.leetftw.tech_mod.item.ModItems;
import com.leetftw.tech_mod.gui.CrystallizerMenu;
import com.leetftw.tech_mod.item.upgrade.MachineUpgrade;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
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
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

public class CrystallizerBlockEntity extends UpgradeableLeetBlockEntity
{
    private static final int ITEM_SLOTS = 2;
    private static final int INPUT_SLOT = 0;
    private static final int OUTPUT_SLOT = 1;

    private static final int FLUID_CONTAINERS = 1;
    private static final int INPUT_CONTAINER = 0;

    private static final int BASE_ENERGY_USAGE = 128;
    private static final int BASE_PROCESSING_TIME = 200;

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
                    case 1 -> getProcessingTime(BASE_PROCESSING_TIME);
                    default -> 0;
                };
            }

            @Override
            public void set(int pIndex, int pValue)
            {
                if (pIndex == 0) progress = pValue;
            }
        };
    }

    public void tick(Level level, BlockPos pos, BlockState state)
    {
        if (hasRecipe())
        {
            if (energyGetStored() >= getEnergyUsage(BASE_ENERGY_USAGE))
            {
                energySetStored(energyGetStored() - getEnergyUsage(BASE_ENERGY_USAGE));
                setProgress(progress + 1);
            }

            if (progress >= getProcessingTime(BASE_PROCESSING_TIME))
            {
                craftItem();
                setProgress(0);
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
        setChangedAndUpdate();
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
    protected boolean itemsSaveOnBreak()
    {
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
    protected int energyGetCapacity()
    {
        return 25000;
    }

    @Override
    protected boolean energyAllowInsert()
    {
        return true;
    }

    @Override
    protected boolean energyAllowExtract()
    {
        return false;
    }

    @Override
    protected int energyGetTransferRate()
    {
        return 2 * getEnergyUsage(BASE_ENERGY_USAGE);
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
    public int upgradesGetSlotCount()
    {
        return 4;
    }

    @Override
    public boolean upgradesAllowUpgrade(MachineUpgrade upgradeItem)
    {
        return false;
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int i, Inventory inventory, Player player)
    {
        return new CrystallizerMenu(i, inventory, ContainerLevelAccess.create(player.level(), getBlockPos()), this, data);
    }
}