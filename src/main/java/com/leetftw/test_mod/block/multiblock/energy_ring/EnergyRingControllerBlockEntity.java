package com.leetftw.test_mod.block.multiblock.energy_ring;

import com.leetftw.test_mod.block.ModBlocks;
import com.leetftw.test_mod.block.entity.BaseLeetBlockEntity;
import com.leetftw.test_mod.block.multiblock.StaticMultiBlockPart;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class EnergyRingControllerBlockEntity extends BaseLeetBlockEntity
{
    private boolean formed;
    private List<Tuple<BlockPos, BlockState>> formation = new ArrayList<>();

    public EnergyRingControllerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState)
    {
        super(type, pos, blockState);
    }

    public void tick(Level pLevel, BlockPos pPos, BlockState pState)
    {
        if (!formed) tryForm();
        else checkFormation();
    }

    private void tryForm()
    {
        List<Vec3i> offsets =
                List.of(new Vec3i(2, 0, 0),
                        new Vec3i(2, 0, 1),
                        new Vec3i(1, 0, 2),
                        new Vec3i(0, 0, 2),
                        new Vec3i(-1, 0, 2),
                        new Vec3i(-2, 0, 0),
                        new Vec3i(-2, 0, 1),
                        new Vec3i(-2, 0, -1),
                        new Vec3i(-1, 0, -2),
                        new Vec3i(0, 0, -2),
                        new Vec3i(1, 0, -2),
                        new Vec3i(2, 0, -1));

        List<Block> acceptedBlocks =
                List.of(ModBlocks.ENERGY_RING_CASING.get(),
                        ModBlocks.ENERGY_RING_INPUT_PORT.get(),
                        ModBlocks.ENERGY_RING_OUTPUT_PORT.get());

        assert level != null;
        boolean formationValid = true;
        for (Vec3i offset : offsets)
        {
            if (!acceptedBlocks.contains(level.getBlockState(getBlockPos().offset(offset)).getBlock()))
            {
                formationValid = false;
            }
        }

        if (!formationValid)
            return;

        for (Vec3i offset : offsets)
        {
            BlockPos absolutePos = getBlockPos().offset(offset);
            BlockState stateAtOffset = level.getBlockState(absolutePos);
            level.setBlockAndUpdate(absolutePos, stateAtOffset.setValue(StaticMultiBlockPart.FORMED, true));
            if (stateAtOffset.getBlock() instanceof EnergyRingIOBlock ioBlock)
            {
                EnergyRingIOBlockEntity ioBE = (EnergyRingIOBlockEntity) level.getBlockEntity(absolutePos);
                assert ioBE != null;
                ioBE.form(getBlockPos());
            }
        }

        formed = true;
        level.setBlockAndUpdate(getBlockPos(), getBlockState().setValue(StaticMultiBlockPart.FORMED, true));
    }

    private void checkFormation()
    {
        List<Vec3i> offsets =
                List.of(new Vec3i(2, 0, 0),
                        new Vec3i(2, 0, 1),
                        new Vec3i(1, 0, 2),
                        new Vec3i(0, 0, 2),
                        new Vec3i(-1, 0, 2),
                        new Vec3i(-2, 0, 0),
                        new Vec3i(-2, 0, 1),
                        new Vec3i(-2, 0, -1),
                        new Vec3i(-1, 0, -2),
                        new Vec3i(0, 0, -2),
                        new Vec3i(1, 0, -2),
                        new Vec3i(2, 0, -1));

        List<Block> acceptedBlocks =
                List.of(ModBlocks.ENERGY_RING_CASING.get(),
                        ModBlocks.ENERGY_RING_INPUT_PORT.get(),
                        ModBlocks.ENERGY_RING_OUTPUT_PORT.get());

        assert level != null;
        boolean formationValid = true;
        for (Vec3i offset : offsets)
        {
            if (!acceptedBlocks.contains(level.getBlockState(getBlockPos().offset(offset)).getBlock()))
            {
                formationValid = false;
            }
        }

        if (formationValid)
            return;

        for (Vec3i offset : offsets)
        {
            BlockPos absolutePos = getBlockPos().offset(offset);
            BlockState stateAtOffset = level.getBlockState(absolutePos);
            if (acceptedBlocks.contains(stateAtOffset.getBlock()))
                level.setBlockAndUpdate(absolutePos, stateAtOffset.setValue(StaticMultiBlockPart.FORMED, false));
        }

        formed = false;
        level.setBlockAndUpdate(getBlockPos(), getBlockState().setValue(StaticMultiBlockPart.FORMED, false));
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
        return getBlockState().getValue(StaticMultiBlockPart.FORMED) ? 500_000_000 : 0;
    }

    @Override
    protected boolean energyAllowInsert() {
        return false;
    }

    @Override
    protected boolean energyAllowExtract() {
        return false;
    }

    @Override
    protected int energyGetTransferRate() {
        return energyGetCapacity();
    }

    public IEnergyStorage getEnergyStorage(boolean input, boolean output)
    {
        return new IEnergyStorage()
        {
            @Override
            public int receiveEnergy(int i, boolean b) {
                return input ? internalEnergyInsert(i, b) : 0;
            }

            @Override
            public int extractEnergy(int i, boolean b) {
                return output ? internalEnergyExtract(i, b) : 0;
            }

            @Override
            public int getEnergyStored() {
                return energyGetStored();
            }

            @Override
            public int getMaxEnergyStored() {
                return energyGetCapacity();
            }

            @Override
            public boolean canExtract() {
                return true;
                //return output;
            }

            @Override
            public boolean canReceive() {
                return true;
                //return input;
            }
        };
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int i, @NotNull Inventory inventory, @NotNull Player player) {
        return null;
    }
}
