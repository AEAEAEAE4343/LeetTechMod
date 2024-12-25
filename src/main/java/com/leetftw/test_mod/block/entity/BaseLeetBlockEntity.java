package com.leetftw.test_mod.block.entity;

import com.leetftw.test_mod.LeetTechMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;

import static com.leetftw.test_mod.block.entity.codec.BaseLeetBlockEntityCodecs.*;
import static java.lang.Math.min;

/*
 * Names of functions in this class are composed like this:
 * internal prefix: specifies private function
 * items prefix: specifies that the function is item related
 * fluids prefix: specifies that the function is fluid related
 * energy prefix: specifies that the function is energy related
 * Function name: (internal) (items/fluids/energy) (action)
 */
// NOTE TO SELF: Some of the functions here are dependent on certain restrictions. There should be
//               public functions which are bound to those restrictions and protected functions
//               which are not. That way the BlockEntity itself can still ignore these restrictions.
public abstract class BaseLeetBlockEntity extends BlockEntity implements MenuProvider  /*implements Container*/
{
    private final ArrayList<ItemStack> ITEMS_INVENTORY = new ArrayList<>(itemsGetSlotCount());
    private final ArrayList<FluidStack> FLUID_INVENTORY = new ArrayList<>(fluidsGetSlotCount());
    private int energyInventory = 0;

    private final ItemStackHandler ITEMS_NBT_HANDLER = new ItemStackHandler(itemsGetSlotCount());

    //region INTERFACE IMPLEMENTATION
    private final Container container = new Container()
    {
        @Override
        public void clearContent()
        {
            itemsClear();
        }

        @Override
        public int getContainerSize()
        {
            return itemsGetSlotCount();
        }

        @Override
        public boolean isEmpty()
        {
            return itemsIsEmpty();
        }

        @Override
        public @NotNull ItemStack getItem(int i)
        {
            return itemsGetSlot(i);
        }

        @Override
        public @NotNull ItemStack removeItem(int i, int i1)
        {
            return internalItemsRemoveFromSlot(i, i1, false);
        }

        @Override
        public @NotNull ItemStack removeItemNoUpdate(int i)
        {
            ItemStack returnVal = itemsGetSlot(i);
            internalItemsSetSlot(i, ItemStack.EMPTY, false);
            return returnVal;
        }

        @Override
        public void setItem(int i, @NotNull ItemStack itemStack)
        {
            itemsSetSlot(i, itemStack);
        }

        @Override
        public void setChanged()
        {
            BaseLeetBlockEntity.this.setChanged();
        }

        @Override
        public boolean stillValid(@NotNull Player player)
        {
            return Container.stillValidBlockEntity(BaseLeetBlockEntity.this, player);
        }

        @Override
        public boolean canPlaceItem(int slot, @NotNull ItemStack stack)
        {
            return itemsCanInsertIntoSlot(slot, stack);
        }

        @Override
        public boolean canTakeItem(@NotNull Container target, int slot, @NotNull ItemStack stack)
        {
            return true;
        }
    };

    private final IItemHandlerModifiable itemHandler = new IItemHandlerModifiable()
    {
        @Override
        public int getSlots()
        {
            return itemsGetSlotCount();
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int i)
        {
            return itemsGetSlot(i);
        }

        @Override
        public @NotNull ItemStack insertItem(int i, @NotNull ItemStack itemStack, boolean b)
        {
            return itemsInsertIntoSlot(i, itemStack, b);
        }

        @Override
        public @NotNull ItemStack extractItem(int i, int i1, boolean b)
        {
            return itemsRemoveFromSlot(i, i1, b);
        }

        @Override
        public int getSlotLimit(int i)
        {
            return itemsGetSlotCapacity(i);
        }

        @Override
        public boolean isItemValid(int i, @NotNull ItemStack itemStack)
        {
            return itemsCanInsertIntoSlot(i, itemStack);
        }

        @Override
        public void setStackInSlot(int i, @NotNull ItemStack itemStack)
        {
            itemsSetSlot(i, itemStack);
        }
    };

    private final IFluidHandler fluidHandler = new IFluidHandler()
    {
        @Override
        public int getTanks()
        {
            return fluidsGetSlotCount();
        }

        @Override
        public @NotNull FluidStack getFluidInTank(int i)
        {
            return fluidsGetSlot(i);
        }

        @Override
        public int getTankCapacity(int i)
        {
            return fluidsGetSlotCapacity(i);
        }

        @Override
        public boolean isFluidValid(int i, FluidStack fluidStack)
        {
            return fluidsAllowInsert(i, fluidStack.getFluid());
        }

        @Override
        public int fill(@NotNull FluidStack fluidStack, @NotNull FluidAction fluidAction)
        {
            return fluidsInsert(fluidStack, fluidAction);
        }

        @Override
        public @NotNull FluidStack drain(@NotNull FluidStack fluidStack, @NotNull FluidAction fluidAction)
        {
            return fluidsExtract(fluidStack, fluidAction);
        }

        @Override
        public @NotNull FluidStack drain(int i, @NotNull FluidAction fluidAction)
        {
            return fluidsExtract(i, fluidAction);
        }
    };

    private final IEnergyStorage energyStorage = new IEnergyStorage()
    {
        @Override
        public int receiveEnergy(int i, boolean b)
        {
            return energyInsert(i, b);
        }

        @Override
        public int extractEnergy(int i, boolean b)
        {
            return energyExtract(i, b);
        }

        @Override
        public int getEnergyStored()
        {
            return energyGetStored();
        }

        @Override
        public int getMaxEnergyStored()
        {
            return energyGetCapacity();
        }

        @Override
        public boolean canExtract()
        {
            return energyAllowExtract();
        }

        @Override
        public boolean canReceive()
        {
            return energyAllowInsert();
        }
    };

    public enum FilterType
    {
        /// Always allow
        ALWAYS_ALLOW,
        /// Always deny
        ALWAYS_DENY,
        /// Respects the existing filters
        RESPECT_EXISTING,
    }

    /// Retrieves a Slot representing an item slot of this block.
    /// insertionFilter and extractionFilter are options that allow for changing the behavior of the slot
    /// inside a GUI versus when using hoppers, pipes, etc. When they are set to RESPECT_EXISTING, the
    /// behaviors specified in itemsAllowInsert and itemsAllowExtract are used.
    public Slot getInventorySlot(int slot, int x, int y, FilterType insertionFilter, FilterType extractionFilter)
    {
        return new Slot(container, slot, x, y)
        {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return switch (insertionFilter) {
                    case ALWAYS_ALLOW -> true;
                    case ALWAYS_DENY -> false;
                    case RESPECT_EXISTING -> itemsAllowInsert(slot, stack.getItem());
                };
            }

            @Override
            public boolean mayPickup(Player player) {
                return switch (extractionFilter) {
                    case ALWAYS_ALLOW -> true;
                    case ALWAYS_DENY -> false;
                    case RESPECT_EXISTING -> itemsAllowExtract(slot);
                };
            }
        };
    }

    public ContainerData getFluidContainerData()
    {
        return new SimpleContainerData(3 * fluidsGetSlotCount())
        {
            @Override
            public int get(int index)
            {
                int actualIndex = index / 3;
                boolean isAmount = index % 3 == 1;
                boolean isCapacity = index % 3 == 2;
                if (isAmount)
                    return fluidsGetSlot(actualIndex).getAmount();
                else if (isCapacity)
                    return fluidsGetSlotCapacity(actualIndex);
                else return BuiltInRegistries.FLUID.getId(fluidsGetSlot(actualIndex).getFluid());
            }

            @Override
            public void set(int index, int value)
            {
                int actualIndex = index / 3;
                boolean isAmount = index % 3 == 1;
                boolean isCapacity = index % 3 == 2;
                if (isAmount)
                    fluidsGetSlot(actualIndex).setAmount(value);
                else if (isCapacity) {} // We cannot set the capacity.
                else fluidsSetSlot(actualIndex, new FluidStack(BuiltInRegistries.FLUID.get(value).get().value(), fluidsGetSlot(actualIndex).getAmount()));
            }
        };
    }

    /// Retrieves a SimpleContainerData representing the energy stored in the block.
    public ContainerData getEnergyContainerData()
    {
        return new SimpleContainerData(2)
        {
            @Override
            public int get(int index)
            {
                return switch (index)
                {
                    case 0 -> energyInventory;
                    case 1 -> energyGetCapacity();
                    default -> throw new IllegalStateException("Invalid index for energy container data: " + index);
                };
            }

            @Override
            public void set(int index, int value)
            {
                switch (index)
                {
                    case 0 -> energyInventory = value;
                    case 1 -> { }
                    default -> throw new IllegalStateException("Invalid index for energy container data: " + index);
                };
            }
        };
    }

    /// Retrieves a Container representing the inventory slots of this block.
    /// Should only be used for GUIs in situations where you want the player to be able
    /// to extract, but not things like hoppers and item cables.
    /// For other situations, use getItemHandler(), because the Container ignores extraction filters!
    /*public Container getContainer()
    {
        return container;
    }*/

    /// Retrieves an IItemHandlerModifiable representing the inventory slots of the block.
    /// The item handler respects any filters set.
    public IItemHandlerModifiable getItemHandler()
    {
        return itemHandler;
    }

    public IFluidHandler getFluidHandler()
    {
        return fluidHandler;
    }

    public IEnergyStorage getEnergyStorage()
    {
        return energyStorage;
    }
    //endregion

    //region ITEM FUNCTIONALITY
    /// Returns the item stack currently stored in the specified slot
    protected ItemStack itemsGetSlot(int slot)
    {
        return ITEMS_INVENTORY.get(slot);
    }

    /// Returns whether all slots are empty
    protected boolean itemsIsEmpty()
    {
        for (int slot = 0; slot < itemsGetSlotCount(); slot++)
            if (!itemsIsSlotEmpty(slot))
                return false;

        return true;
    }

    /// Returns whether the specified slot is empty
    protected boolean itemsIsSlotEmpty(int slot)
    {
        return itemsGetSlot(slot) == ItemStack.EMPTY;
    }

    /// Returns the maximum amount of items any slot can hold
    protected int itemsGetMaxSlotCapacity()
    {
        return 64;
    }

    /// Returns the maximum amount of items the specified slot can hold
    protected int itemsGetSlotCapacity(int slot)
    {
        if (itemsIsSlotEmpty(slot))
            return itemsGetMaxSlotCapacity();
        else return min(itemsGetSlot(slot).getMaxStackSize(), itemsGetMaxSlotCapacity());
    }

    /// Sets the item stack stored in the specified slot
    /// If update is set to true, the function causes a block update
    /// (takes into account: max stack size)
    private void internalItemsSetSlot(int slot, ItemStack stack, boolean update)
    {
        if (stack.getCount() > itemsGetMaxSlotCapacity())
        {
            LeetTechMod.LOGGER.warn("Attempted to set slot " + slot + " to an item stack larger than the maximum stack size (" + stack.getCount() + ")!");
            stack.setCount(itemsGetMaxSlotCapacity());
        }
        ITEMS_INVENTORY.set(slot, stack);
        if (update) setChanged();
    }

    /// Sets the item stack stored in the specified slot
    /// (takes into account: max stack size)
    protected void itemsSetSlot(int slot, ItemStack stack)
    {
        internalItemsSetSlot(slot, stack, true);
    }

    /// Returns whether the specified item stack can be placed into the specified slot
    /// (takes into account: current item)
    /// (can take into account depending on parameters: max stack size, item slot filters)
    protected boolean itemsCanInsertIntoSlot(int slot, ItemStack stack, boolean ignoreFilters, boolean ignoreStackLimit)
    {
        if (!ignoreFilters && !itemsAllowInsert(slot, stack.getItem()))
            return false;

        if (itemsGetSlot(slot).isEmpty())
            return true;

        if (!itemsGetSlot(slot).is(stack.getItem()))
            return false;

        return ignoreStackLimit || itemsGetSlot(slot).getCount() + stack.getCount() <= itemsGetSlot(slot).getMaxStackSize();
    }

    /// Returns whether the specified item stack can be placed into the specified slot
    /// (takes into account: max stack size, current item, item slot filters)
    protected boolean itemsCanInsertIntoSlot(int slot, ItemStack stack)
    {
        return itemsCanInsertIntoSlot(slot, stack, false, false);
    }

    private ItemStack internalItemsInsertIntoSlot(int slot, ItemStack stack, boolean simulate)
    {
        // If an empty stack is inserted do nothing
        if (stack.isEmpty())
            return ItemStack.EMPTY;

        ItemStack existingStack = itemsGetSlot(slot);
        ItemStack newStack = stack.copy();

        int newTotal = min(itemsGetSlotCapacity(slot), stack.getCount() + existingStack.getCount());
        int itemsToBeAdded = newTotal - existingStack.getCount();

        if (!simulate)
        {
            if (existingStack.isEmpty())
                existingStack = new ItemStack(stack.getItem(), newTotal);
            else existingStack.setCount(newTotal);
            itemsSetSlot(slot, existingStack);
            this.setChanged();
        }

        newStack.setCount(stack.getCount() - itemsToBeAdded);
        return newStack;
    }

    /// Inserts the specified item stack into the specified slot. If there is a remainder, an item stack containing the remaining items is returned, otherwise an empty item stack is returned.
    /// (takes into account: max stack size, current item, item slot filters)
    protected ItemStack itemsInsertIntoSlot(int slot, ItemStack stack, boolean simulate)
    {
        // If the item stack can't be inserted do nothing
        if (!itemsCanInsertIntoSlot(slot, stack, false, true)) {
            return stack;
        }

        return internalItemsInsertIntoSlot(slot, stack, simulate);
    }

    /// Removes the specified amount of items from the slot and returns an item stack containing the removed items
    /// If simulate is set to true, no items are actually removed from the slot, but the return value is only calculated
    protected ItemStack internalItemsRemoveFromSlot(int slot, int amount, boolean simulate)
    {
        // If the current stack is empty do nothing
        if (itemsGetSlot(slot).isEmpty())
            return ItemStack.EMPTY;

        // Determine how much we should extract in total
        int totalItems = itemsGetSlot(slot).getCount();
        int itemsToBeExtracted = min(totalItems, amount);

        // Get the extracted stack
        ItemStack extractedStack = itemsGetSlot(slot).copy();
        extractedStack.setCount(itemsToBeExtracted);

        // If we aren't simulating, decrease the item count in the container
        if (!simulate)
        {
            int newCount = itemsGetSlot(slot).getCount() - itemsToBeExtracted;
            if (newCount == 0)
                itemsSetSlot(slot, ItemStack.EMPTY);
            else itemsGetSlot(slot).setCount(newCount);
            this.setChanged();
        }

        // Return the extracted stack
        return extractedStack;
    }

    protected ItemStack itemsRemoveFromSlot(int slot, int amount, boolean simulate)
    {
        // If we can't extract from this slot do nothing
        if (!itemsAllowExtract(slot))
            return ItemStack.EMPTY;

        // Uncertain whether this function should be affected by filters

        // If the current stack is empty do nothing
        if (itemsGetSlot(slot).isEmpty())
            return ItemStack.EMPTY;

        return internalItemsRemoveFromSlot(slot, amount, simulate);
    }

    /// Clears the items in all slots and returns the previous contents
    public ArrayList<ItemStack> itemsClear()
    {
        ArrayList<ItemStack> oldItems = (ArrayList<ItemStack>) ITEMS_INVENTORY.clone();
        ITEMS_INVENTORY.clear();
        for (int i = 0; i < itemsGetSlotCount(); ++i)
            ITEMS_INVENTORY.add(ItemStack.EMPTY);

        setChanged();
        return oldItems;
    }
    //endregion

    //region FLUID FUNCTIONALITY
    /// Returns the fluid stack currently stored in the specified slot
    protected FluidStack fluidsGetSlot(int slot)
    {
        return FLUID_INVENTORY.get(slot);
    }

    /// Sets the fluid stack stored in the specified slot
    /// (takes into account: max capacity size)
    protected void fluidsSetSlot(int slot, FluidStack stack)
    {
        if (stack.getAmount() > fluidsGetSlotCapacity(slot))
        {
            LeetTechMod.LOGGER.warn("Attempted to set slot " + slot + " to a fluid stack larger than the maximum capacity (" + stack.getAmount() + ")!");
            stack.setAmount(fluidsGetSlotCapacity(slot));
        }
        FLUID_INVENTORY.set(slot, stack);
        setChanged();
    }

    // Returns whether all slots are empty
    protected boolean fluidsIsEmpty()
    {
        for (int slot = 0; slot < fluidsGetSlotCount(); slot++)
            if (!fluidsGetSlot(slot).isEmpty())
                return false;

        return true;
    }

    /// Returns whether the specified slot is empty
    protected boolean fluidsIsSlotEmpty(int slot)
    {
        return fluidsGetSlot(slot).isEmpty();
    }

    /// Attempts to fill the specified fluid into the specified slot.
    /// Returns the amount of fluid that has been filled into the slot.
    protected int fluidsInsertIntoSlot(int slot, FluidStack stack, IFluidHandler.FluidAction action)
    {
        if (stack.isEmpty())
            return 0;

        // Check if the fluid is allowed in the slot
        if (!fluidsAllowInsert(slot, stack.getFluid()))
            return 0;

        // Check if the fluid is the same as what is already in the slot OR slot is empty
        FluidStack existingFluid = fluidsGetSlot(slot);
        if (!fluidsIsSlotEmpty(slot) && stack.getFluidType() != existingFluid.getFluidType())
            return 0;

        // Calculate how much to fill
        int newTotal = min(fluidsGetSlotCapacity(slot), stack.getAmount() + existingFluid.getAmount());
        int fluidToBeAdded = newTotal - existingFluid.getAmount();

        // If action is not simulate, actually fill it
        if (action != IFluidHandler.FluidAction.SIMULATE)
        {
            // If it is empty, we set it to the target fluid first
            if (existingFluid.isEmpty())
                existingFluid = new FluidStack(stack.getFluid(), newTotal);
            else existingFluid.setAmount(newTotal);
            fluidsSetSlot(slot, existingFluid);
            this.setChanged();
        }

        return fluidToBeAdded;
    }

    /// Attempts to fill the specified fluid into the internal tanks of the block.
    /// Returns the amount of fluid that has been filled into the block.
    protected int fluidsInsert(FluidStack stack, IFluidHandler.FluidAction action)
    {
        if (stack.isEmpty())
            return 0;

        // We iterate through all the slots to see what we can fill
        int totalAmountFilled = 0;
        for (int slot = 0; slot < fluidsGetSlotCount(); slot++)
        {
            int amountFilled = fluidsInsertIntoSlot(slot, stack, action);
            stack.setAmount(stack.getAmount() - amountFilled);
            totalAmountFilled += amountFilled;
            if (stack.isEmpty()) break;
        }

        return totalAmountFilled;
    }

    private FluidStack internalFluidsExtractFromSlot(int slot, int fluidToBeExtracted, IFluidHandler.FluidAction action)
    {
        // Get the extracted stack
        FluidStack extractedStack = fluidsGetSlot(slot).copy();
        extractedStack.setAmount(fluidToBeExtracted);

        // If we aren't simulating, decrease the fluid amount in the container
        if (action != IFluidHandler.FluidAction.SIMULATE)
        {
            int newAmount = fluidsGetSlot(slot).getAmount() - fluidToBeExtracted;
            if (newAmount == 0)
                fluidsSetSlot(slot, FluidStack.EMPTY);
            else fluidsGetSlot(slot).setAmount(newAmount);
            this.setChanged();
        }

        // Return the extracted stack
        return extractedStack;
    }


    /// Attempts to extract the specified amount of fluid from the specified slot.
    /// Returns the fluid stack extracted from the slot.
    /// If action is FluidAction.SIMULATE, the return value is calculated, but no fluids are actually extracted.
    protected FluidStack fluidsExtractFromSlot(int slot, FluidStack stack, IFluidHandler.FluidAction action)
    {
        if (stack.isEmpty() || fluidsIsSlotEmpty(slot))
            return FluidStack.EMPTY;

        if (!fluidsAllowExtract(slot))
            return FluidStack.EMPTY;

        if (fluidsGetSlot(slot).getFluidType() != stack.getFluidType())
            return FluidStack.EMPTY;

        // Determine how much we should extract in total
        int totalFluidAvailable = fluidsGetSlot(slot).getAmount();
        int fluidToBeExtracted = min(totalFluidAvailable, stack.getAmount());

        // Return the extracted stack
        return internalFluidsExtractFromSlot(slot, fluidToBeExtracted, action);
    }

    /// Attempts to extract the specified amount of fluid from the specified slot.
    /// Returns the fluid stack extracted from the slot.
    /// If action is FluidAction.SIMULATE, the return value is calculated, but no fluids are actually extracted.
    protected FluidStack fluidsExtractFromSlot(int slot, int amount, IFluidHandler.FluidAction action)
    {
        if (amount <= 0 || fluidsIsSlotEmpty(slot))
            return FluidStack.EMPTY;

        if (!fluidsAllowExtract(slot))
            return FluidStack.EMPTY;

        // Determine how much we should extract in total
        int totalFluidAvailable = fluidsGetSlot(slot).getAmount();
        int fluidToBeExtracted = min(totalFluidAvailable, amount);

        // Return the extracted stack
        return internalFluidsExtractFromSlot(slot, fluidToBeExtracted, action);
    }

    /// Attempts to extract the specified fluid stack from the internal tanks of the block.
    /// Returns the fluid stack extracted from the internal tanks.
    /// If action is FluidAction.SIMULATE, the return value is calculated, but no fluids are actually extracted.
    protected FluidStack fluidsExtract(FluidStack stack, IFluidHandler.FluidAction action)
    {
        if (stack.isEmpty() || fluidsIsEmpty())
            return FluidStack.EMPTY;

        if (fluidsGetSlotCount() == 0)
            return FluidStack.EMPTY;

        // We iterate through all the slots to see what we can fill
        int totalAmountDrained = 0;
        int maxFluidToDrain = stack.getAmount();
        for (int slot = 0; slot < fluidsGetSlotCount(); slot++)
        {
            // Drain as much as we need - how much we already have
            stack.setAmount(maxFluidToDrain - totalAmountDrained);
            FluidStack fluidDrained = fluidsExtractFromSlot(slot, stack, action);

            // If we got no fluid we just continue
            if (fluidDrained.isEmpty())
                continue;

            // Add the amount we got to the total
            totalAmountDrained += fluidDrained.getAmount();

            // If we have reached our goal we can finish
            if (totalAmountDrained == maxFluidToDrain)
                break;
        }

        // This might break if totalAmountDrained is 0??
        return new FluidStack(stack.getFluid(), totalAmountDrained);
    }

    /// Attempts to extract the specified amount of fluid from the internal tanks of the block.
    /// Returns the fluid stack extracted from the internal tanks.
    /// If action is FluidAction.SIMULATE, the return value is calculated, but no fluids are actually extracted.
    protected FluidStack fluidsExtract(int amount, IFluidHandler.FluidAction action)
    {
        // Find the first drainable fluid in our inventory
        for (int slot = 0; slot < fluidsGetSlotCount(); slot++)
        {
            if (!fluidsIsSlotEmpty(slot) && fluidsAllowExtract(slot))
            {
                // Return whatever we can extract from that fluid
                return fluidsExtract(new FluidStack(fluidsGetSlot(slot).getFluid(), amount), action);
            }
        }

        // If there is nothing to be extracted, do nothing
        return FluidStack.EMPTY;
    }

    /// Clears the fluids in all slots
    protected void fluidsClear()
    {
        FLUID_INVENTORY.clear();
        for (int i = 0; i < fluidsGetSlotCount(); i++)
            FLUID_INVENTORY.add(FluidStack.EMPTY);
        setChanged();
    }
    //endregion

    //region ENERGY FUNCTIONALITY
    /// Retrieves how much energy is stored
    protected int energyGetStored()
    {
        return energyInventory;
    }

    /// Sets how much energy is stored
    protected void energySetStored(int amount)
    {
        if (amount > energyGetCapacity())
        {
            LeetTechMod.LOGGER.warn("Attempted to set energy stored to an amount larger than the maximum capacity (" + amount + ")!");
            amount = energyGetCapacity();
        }
        energyInventory = amount;
        setChanged();
    }

    /// Retrieves whether the energy storage is empty
    protected boolean energyIsEmpty()
    {
        return energyInventory == 0;
    }

    protected int internalEnergyInsert(int amount, boolean simulate)
    {
        amount = min(amount, energyGetTransferRate());

        int newTotal = min(energyGetStored() + amount, energyGetCapacity());
        int energyAccepted = newTotal - energyGetStored();
        if (!simulate) energySetStored(newTotal);
        return energyAccepted;
    }

    /// Tries to insert energy into the storage.
    /// Returns the amount of energy that was accepted by the storage.
    /// If simulate is set to true, the return value is calculated, but the energy stored is not modified.
    protected int energyInsert(int amount, boolean simulate)
    {
        if (!energyAllowInsert())
            return 0;

        return internalEnergyInsert(amount, simulate);
    }

    protected int internalEnergyExtract(int amount, boolean simulate)
    {
        amount = min(amount, energyGetTransferRate());

        int newTotal = Math.max(0, energyGetStored() - amount);
        int energyRemoved = energyGetStored() - newTotal;
        if (!simulate) energySetStored(newTotal);
        return energyRemoved;
    }

    /// Tries to extract energy into the storage.
    /// Returns the amount of energy that was extracted from the storage.
    /// If simulate is set to true, the return value is calculated, but the energy stored is not modified.
    protected int energyExtract(int amount, boolean simulate)
    {
        if (!energyAllowExtract())
            return 0;

        return internalEnergyExtract(amount, simulate);
    }

    /// Clears the energy from the storage
    protected void energyClear()
    {
        energySetStored(0);
    }
    //endregion

    public BaseLeetBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState)
    {
        super(type, pos, blockState);

        itemsClear();
        fluidsClear();
    }

    @Override
    @ParametersAreNonnullByDefault
    protected void saveAdditional(CompoundTag pTag, HolderLookup.Provider registries)
    {
        super.saveAdditional(pTag, registries);
        serializeNBT(registries, pTag);
    }

    @Override
    @ParametersAreNonnullByDefault
    public void loadAdditional(CompoundTag pTag, HolderLookup.Provider registries)
    {
        super.loadAdditional(pTag, registries);
        deserializeNBT(registries, pTag);
    }

    private void serializeNBT(HolderLookup.Provider provider, CompoundTag pTag)
    {
        ListTag itemList = new ListTag();
        for (int i = 0; i < ITEMS_INVENTORY.size(); ++i)
        {
            if (!ITEMS_INVENTORY.get(i).isEmpty())
            {
                CompoundTag itemTag = new CompoundTag();
                itemTag.putInt("Slot", i);
                itemList.add(this.ITEMS_INVENTORY.get(i).save(provider, itemTag));
            }
        }

        ListTag fluidList = new ListTag();
        for (int i = 0; i < FLUID_INVENTORY.size(); ++i)
        {
            if (!FLUID_INVENTORY.get(i).isEmpty())
            {
                CompoundTag fluidTag = new CompoundTag();
                fluidTag.putInt("Container", i);
                fluidList.add(this.FLUID_INVENTORY.get(i).save(provider, fluidTag));
            }
        }

        pTag.put("base_leet_be.items", itemList);
        pTag.put("base_leet_be.fluids", fluidList);
        pTag.putInt("base_leet_be.energy", energyGetStored());
    }

    private void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt)
    {
        ListTag itemList = nbt.getList("base_leet_be.items", 10);
        for(int i = 0; i < itemList.size(); ++i)
        {
            CompoundTag itemTags = itemList.getCompound(i);
            int slot = itemTags.getInt("Slot");
            if (slot >= 0 && slot < itemsGetSlotCount())
            {
                ItemStack.parse(provider, itemTags).ifPresent((stack) -> itemsSetSlot(slot, stack));
            }
        }

        ListTag fluidList = nbt.getList("base_leet_be.fluids", 10);
        for(int i = 0; i < fluidList.size(); ++i)
        {
            CompoundTag fluidTags = fluidList.getCompound(i);
            int slot = fluidTags.getInt("Container");
            if (slot >= 0 && slot < itemsGetSlotCount())
            {
                FluidStack.parse(provider, fluidTags).ifPresent((stack) -> fluidsSetSlot(slot, stack));
            }
        }

        energySetStored(nbt.getInt("base_leet_be.energy"));
        this.onLoad();
    }

    @Override
    protected void applyImplicitComponents(@NotNull DataComponentInput componentInput)
    {
        super.applyImplicitComponents(componentInput);
        if (itemsGetSlotCount() > 0)
        {
            ItemContainerContents container = componentInput.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.fromItems(ITEMS_INVENTORY));
            for (int i = 0; i < min(container.getSlots(), itemsGetSlotCount()); i++) {
                itemsSetSlot(i, container.getStackInSlot(i));
            }
        }
        if (energyGetCapacity() > 0) energyInventory = componentInput.getOrDefault(ENERGY_STORED, energyInventory);
        // Energy capacity is not applicable for loading into the BE.
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components)
    {
        super.collectImplicitComponents(components);
        if (itemsGetSlotCount() > 0) components.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(ITEMS_INVENTORY));
        if (energyGetCapacity() > 0)
        {
            components.set(ENERGY_STORED, energyInventory);
            components.set(ENERGY_CAPACITY, energyGetCapacity());
        }
    }

    @Override
    public void removeComponentsFromTag(CompoundTag tag)
    {
        super.removeComponentsFromTag(tag);
        tag.remove("base_leet_be.energy");
        tag.remove("base_leet_be.items");
    }

    /// Returns the amount of item slots the block has
    protected abstract int itemsGetSlotCount();

    /// Returns whether the specified item is allowed to be inserted into the specified slot
    /// Applies to 'insertions' and player interactions
    protected abstract boolean itemsAllowInsert(int slot, Item stack);

    /// Returns whether items are allowed to be extracted from the specified slot
    /// Applies to 'extractions', players can still take items out of the slot from a GUI
    protected abstract boolean itemsAllowExtract(int slot);

    /// Returns whether the block will save its contents when broken. If this is set to true,
    /// the container data is stored in the dropped ItemStack. Otherwise, the contents are
    /// dropped in the world.
    protected abstract boolean itemsSaveOnBreak();

    /// Returns the amount of fluid slots the block has
    protected abstract int fluidsGetSlotCount();

    /// Returns how many mB of fluid the specified slot can store
    protected abstract int fluidsGetSlotCapacity(int i);

    /// Returns whether the specified fluid is allowed to be inserted into the specified slot
    protected abstract boolean fluidsAllowInsert(int slot, Fluid fluid);

    /// Returns whether fluids are allowed to be extracted from the specified slot
    protected abstract boolean fluidsAllowExtract(int slot);

    /// Returns the maximum amount of energy that can be stored
    protected abstract int energyGetCapacity();

    /// Returns whether energy is allowed to be inserted
    protected abstract boolean energyAllowInsert();

    /// Returns whether energy is allowed to be extracted
    protected abstract boolean energyAllowExtract();

    /// Returns the maximum transfer rate of the block (FE/t)
    protected abstract int energyGetTransferRate();

    @Override
    public @NotNull Component getDisplayName()
    {
        return Component.translatable("block.test_mod." + getBlockState().getBlock().getName());
    }

    @Override
    public abstract @Nullable AbstractContainerMenu createMenu(int i, @NotNull Inventory inventory, @NotNull Player player);

    //protected abstract ? getStorageFluidTypes();
    //protected abstract int getStorageEnergy();
}
