package com.leetftw.tech_mod.block.entity;

import com.leetftw.tech_mod.block.ModBlocks;
import com.leetftw.tech_mod.fluid.ModFluids;
import com.leetftw.tech_mod.gui.CrystalInjectorMenu;
import com.leetftw.tech_mod.gui.CrystallizerMenu;
import com.leetftw.tech_mod.item.upgrade.MachineUpgrade;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

public class CrystalInjectorBlockEntity extends UpgradeableLeetBlockEntity
{
    private static final int MAX_DISTANCE = 5;
    private BlockPos targetBlock = null;
    private int laserLength = 0;
    private int progress = 0;

    private static final int BASE_PROCESSING_TIME = 6000;
    private static final int BASE_ENERGY_USAGE = 256;
    private static final int FLUID_USAGE = 6000;

    protected final SimpleContainerData data;

    public CrystalInjectorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState)
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

            }
        };
    }

    public void resetCraft()
    {
        targetBlock = null;
        progress = 0;
        laserLength = 0;
        setChangedAndUpdate();
    }

    private void setProgress(int progress)
    {
        this.progress = progress;
        setChangedAndUpdate();
    }

    private void finishCraft(Level level)
    {
        level.setBlockAndUpdate(targetBlock, ModBlocks.BUDDING_AESTHETIC_BLOCK.get().defaultBlockState());
        resetCraft();
    }

    public boolean isCrafting()
    {
        return progress > 0;
    }

    public int getLaserLength()
    {
        return laserLength;
    }

    public void tick(Level level, BlockPos pos, BlockState state)
    {
        // In-world crafting recipe
        // The Crystal Injector shoots a laser towards a budding amethyst block
        // and converts it into a budding aesthetic block while consuming liquified
        // aesthetic.

        // Check closest block towards direction (max dist 5)
        Direction direction = state.getValue(HorizontalDirectionalBlock.FACING);
        BlockPos targetPos = pos;
        boolean foundBlock = false;
        for (int i = 0; i < MAX_DISTANCE; i++)
        {
            targetPos = targetPos.relative(direction);
            foundBlock = level.getBlockState(targetPos).is(Blocks.BUDDING_AMETHYST);
            if (foundBlock) break;
        }

        // Changing the closest block restarts the conversion
        if (!foundBlock || (targetBlock != null && !targetBlock.equals(targetPos))) resetCraft();
        if (!foundBlock) return;

        // Save target position for next iteration
        targetBlock = targetPos;

        // Set the laser length to the closest budding amethyst block (for renderer)
        laserLength = targetPos.distManhattan(pos) - 1;

        // Consume fluid before crafting
        FluidStack storedFluid = fluidsGetSlot(0);
        if (progress == 0)
        {
            if (!storedFluid.is(ModFluids.LIQUID_AESTHETIC) || storedFluid.getAmount() < FLUID_USAGE) return;
            storedFluid.setAmount(storedFluid.getAmount() - FLUID_USAGE);
            fluidsSetSlot(0, storedFluid);
        }

        // CRAFTING PROCESS
        // If a conversion is still going, just wait
        if (energyGetStored() >= getEnergyUsage(BASE_ENERGY_USAGE))
        {
            energySetStored(energyGetStored() - getEnergyUsage(BASE_ENERGY_USAGE));

            setProgress(progress + 1);

            // If the conversion is done, we change the block into budding aesthetic
            if (progress >= getProcessingTime(BASE_PROCESSING_TIME))
            {
                finishCraft(level);
            }
        }
    }

    @Override
    protected int itemsGetSlotCount()
    {
        return 0;
    }

    @Override
    protected boolean itemsAllowInsert(int slot, Item stack)
    {
        return false;
    }

    @Override
    protected boolean itemsAllowExtract(int slot)
    {
        return false;
    }

    @Override
    protected boolean itemsSaveOnBreak()
    {
        return false;
    }

    @Override
    protected int fluidsGetSlotCount()
    {
        return 1;
    }

    @Override
    protected int fluidsGetSlotCapacity(int i)
    {
        return 6000;
    }

    @Override
    protected boolean fluidsAllowInsert(int slot, Fluid fluid)
    {
        return fluid.isSame(ModFluids.LIQUID_AESTHETIC.get());
    }

    @Override
    protected boolean fluidsAllowExtract(int slot)
    {
        return false;
    }

    @Override
    protected int energyGetCapacity()
    {
        return 1_000_000;
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
        return 1000;
    }

    // Create an update tag here, like above.
    @Override
    public @NotNull CompoundTag getUpdateTag(@NotNull HolderLookup.Provider registries)
    {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    // Return our packet here. This method returning a non-null result tells the game to use this packet for syncing.
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket()
    {
        // The packet uses the CompoundTag returned by #getUpdateTag. An alternative overload of #create exists
        // that allows you to specify a custom update tag, including the ability to omit data the client might not need.
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    @ParametersAreNonnullByDefault
    protected void saveAdditional(CompoundTag pTag, HolderLookup.Provider registries)
    {
        super.saveAdditional(pTag, registries);
        pTag.putInt("crystal_injector.progress", progress);
        pTag.putInt("crystal_injector.laser_length", laserLength);
    }

    @Override
    @ParametersAreNonnullByDefault
    public void loadAdditional(CompoundTag pTag, HolderLookup.Provider registries)
    {
        super.loadAdditional(pTag, registries);
        progress = pTag.getInt("crystal_injector.progress");
        laserLength = pTag.getInt("crystal_injector.laser_length");
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
    public @Nullable AbstractContainerMenu createMenu(int i, @NotNull Inventory inventory, @NotNull Player player)
    {
        return new CrystalInjectorMenu(i, inventory, ContainerLevelAccess.create(player.level(), getBlockPos()), this, data);
    }
}