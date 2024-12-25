package com.leetftw.test_mod.block.multiblock.energy_ring;

import com.leetftw.test_mod.block.multiblock.StaticMultiBlockPart;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

public class EnergyRingIOBlockEntity extends BlockEntity
{
    BlockPos controller;
    boolean input;

    public EnergyRingIOBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState, boolean input)
    {
        super(type, pos, blockState);
        this.input = input;
        this.controller = BlockPos.ZERO;
    }

    @Override
    @ParametersAreNonnullByDefault
    protected void saveAdditional(CompoundTag pTag, HolderLookup.Provider registries)
    {
        super.saveAdditional(pTag, registries);
        pTag.putBoolean("energy_ring_io_port_be.is_input", input);
        pTag.putLong("energy_ring_io_port_be.controller", controller.asLong());
    }

    @Override
    @ParametersAreNonnullByDefault
    public void loadAdditional(CompoundTag pTag, HolderLookup.Provider registries)
    {
        super.loadAdditional(pTag, registries);
        input = pTag.getBoolean("energy_ring_io_port_be.is_input");
        if (pTag.contains("energy_ring_io_port_be.controller"))
            controller = BlockPos.of(pTag.getLong("energy_ring_io_port_be.controller"));
    }

    @Override
    public void setChanged()
    {
        super.setChanged();
        if (level != null && !level.isClientSide)
        {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
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

    public EnergyRingIOBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState)
    {
        this(type, pos, blockState, false);
    }

    void form(BlockPos controller)
    {
        if (level == null || level.isClientSide)
            return;

        this.controller = controller;
        invalidateCapabilities();
        setChanged();
    }

    public IEnergyStorage getEnergyStorage()
    {
        if (!getBlockState().getValue(StaticMultiBlockPart.FORMED)) return null;
        if (level == null) return null;
        if (controller == null) return null;
        if (controller.equals(BlockPos.ZERO)) return null;

        BlockEntity entity = level.getBlockEntity(controller);
        if (entity instanceof EnergyRingControllerBlockEntity controllerBE)
        {
            return controllerBE.getEnergyStorage(input, !input);
        }
        return null;
    }
}
