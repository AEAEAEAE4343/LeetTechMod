package com.leetftw.tech_mod.block.entity;

import com.leetftw.tech_mod.item.ModDataComponents;
import com.leetftw.tech_mod.item.upgrade.MachineUpgrade;
import com.leetftw.tech_mod.item.upgrade.MachineUpgradeItem;
import com.leetftw.tech_mod.util.ItemHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

public abstract class UpgradeableLeetBlockEntity extends BaseLeetBlockEntity
{
    private final NonNullList<ItemStack> UPGRADES_INVENTORY = NonNullList.withSize(upgradesGetSlotCount(), ItemStack.EMPTY);

    private final Container upgradesContainer = new Container()
    {
        @Override
        public void clearContent()
        {
            UPGRADES_INVENTORY.clear();
            setChangedAndUpdate();
        }

        @Override
        public int getContainerSize()
        {
            return UPGRADES_INVENTORY.size();
        }

        @Override
        public boolean isEmpty()
        {
            return UPGRADES_INVENTORY.stream().allMatch(ItemStack::isEmpty);
        }

        @Override
        public @NotNull ItemStack getItem(int i)
        {
            return UPGRADES_INVENTORY.get(i);
        }

        @Override
        public @NotNull ItemStack removeItem(int i, int i1)
        {
            ItemStack returnVal = UPGRADES_INVENTORY.get(i);
            UPGRADES_INVENTORY.set(i, ItemStack.EMPTY);
            setChangedAndUpdate();
            return returnVal;
        }

        @Override
        public @NotNull ItemStack removeItemNoUpdate(int i)
        {
            ItemStack returnVal = UPGRADES_INVENTORY.get(i);
            UPGRADES_INVENTORY.set(i, ItemStack.EMPTY);
            return returnVal;
        }

        @Override
        public void setItem(int i, @NotNull ItemStack itemStack)
        {
            UPGRADES_INVENTORY.set(i, itemStack);
            setChangedAndUpdate();
        }

        @Override
        public void setChanged()
        {
            setChangedAndUpdate();
        }

        @Override
        public boolean stillValid(@NotNull Player player)
        {
            return Container.stillValidBlockEntity(UpgradeableLeetBlockEntity.this, player);
        }

        @Override
        public boolean canPlaceItem(int slot, @NotNull ItemStack stack)
        {
            if (!(stack.getItem() instanceof MachineUpgradeItem upgradeItem))
                return false;

            MachineUpgrade upgrade = upgradeFromId(stack.get(ModDataComponents.MACHINE_UPGRADE));
            if (upgrade == null)
                return false;

            return !upgrade.isMachineSpecific() || upgradesAllowUpgrade(upgrade);
        }

        @Override
        public boolean canTakeItem(@NotNull Container target, int slot, @NotNull ItemStack stack)
        {
            return true;
        }
    };

    /// Retrieves a Slot representing an upgrade slot of this block.
    /// insertionFilter and extractionFilter are options that allow for changing the behavior of the slot
    /// inside a GUI. When they are set to RESPECT_EXISTING, the default behaviors are used:
    /// upgraadesAllowUpgrade() for insertion and always allow extraction.
    public Slot getUpgradeSlot(int slot, int x, int y, FilterType insertionFilter, FilterType extractionFilter)
    {
        return new Slot(upgradesContainer, slot, x, y)
        {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return switch (insertionFilter) {
                    case ALWAYS_ALLOW -> true;
                    case ALWAYS_DENY -> false;
                    case RESPECT_EXISTING -> container.canPlaceItem(slot, stack);
                };
            }

            @Override
            public boolean mayPickup(Player player) {
                return switch (extractionFilter) {
                    case ALWAYS_ALLOW, RESPECT_EXISTING -> true;
                    case ALWAYS_DENY -> false;
                };
            }

            @Override
            public int getMaxStackSize()
            {
                return 1;
            }
        };
    }

    public UpgradeableLeetBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState)
    {
        super(type, pos, blockState);
    }

    protected MachineUpgrade upgradeFromId(ResourceLocation id)
    {
        return MachineUpgrade.fromId(level.registryAccess(), id);
    }

    protected float getSpeedMultiplier()
    {
        return UPGRADES_INVENTORY.stream().filter(itemStack -> !itemStack.isEmpty())
                .map(a -> upgradeFromId(a.get(ModDataComponents.MACHINE_UPGRADE)).getSpeedMultiplier())
                .reduce(1.0f, (a,b) -> a * b);
    }

    protected int getProcessingTime(int baseValue)
    {
        return (int)Math.ceil(baseValue * getSpeedMultiplier());
    }

    protected int getEnergyUsage(int baseValue)
    {
        return (int)Math.ceil(baseValue * getEfficiencyMultiplier());
    }

    protected float getEfficiencyMultiplier()
    {
        return UPGRADES_INVENTORY.stream().filter(itemStack -> !itemStack.isEmpty())
                .map(a -> upgradeFromId(a.get(ModDataComponents.MACHINE_UPGRADE)).getEfficiencyMultiplier())
                .reduce(1.0f, (a, b) -> a * b);
    }

    protected boolean hasUpgradeType(MachineUpgrade type)
    {
        return UPGRADES_INVENTORY.stream().anyMatch(itemStack -> itemStack.get(ModDataComponents.MACHINE_UPGRADE).equals(type));
    }

    @Override
    protected void saveAdditional(CompoundTag pTag, HolderLookup.Provider registries)
    {
        super.saveAdditional(pTag, registries);
        ItemHelper.saveItemsToTag(pTag, registries, "upgradeable_leet_be.upgrades", UPGRADES_INVENTORY.stream());
    }

    @Override
    public void loadAdditional(CompoundTag pTag, HolderLookup.Provider registries)
    {
        super.loadAdditional(pTag, registries);
        if (pTag.contains("upgradeable_leet_be.upgrades"))
            Collections.copy(UPGRADES_INVENTORY, ItemHelper.loadItemsFromTag(pTag, registries, "upgradeable_leet_be.upgrades", upgradesGetSlotCount()));
    }

    /// Returns the amount of upgrade slots the machine has.
    public abstract int upgradesGetSlotCount();

    /// Returns whether the machine-specific upgrade is allowed in the machine.
    /// Note: all non-machine-specific upgrades are still allowed.
    public abstract boolean upgradesAllowUpgrade(MachineUpgrade upgradeItem);
}
