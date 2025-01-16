package com.leetftw.tech_mod.util;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

public class ForgeEnergyHelper {
    public static void pushPower(IEnergyStorage from, IEnergyStorage to) {
        int extractedEnergy = from.extractEnergy(Integer.MAX_VALUE, true);
        int insertedEnergy = to.receiveEnergy(Integer.MAX_VALUE, true);

        int energyTransferred = Math.min(extractedEnergy, insertedEnergy);
        from.extractEnergy(energyTransferred, false);
        to.receiveEnergy(energyTransferred, false);
    }

    public static void pushPower(BlockEntity blockEntity)
    {
        pushPower(blockEntity, false);
    }

    public static void pushPower(BlockEntity blockEntity, boolean skipUp)
    {
        if (blockEntity.getLevel().getCapability(Capabilities.EnergyStorage.BLOCK,
                blockEntity.getBlockPos().relative(Direction.EAST), Direction.WEST) instanceof IEnergyStorage neighbour
                && blockEntity.getLevel().getCapability(Capabilities.EnergyStorage.BLOCK,
                blockEntity.getBlockPos(), Direction.EAST) instanceof  IEnergyStorage source)
            pushPower(source, neighbour);

        if (blockEntity.getLevel().getCapability(Capabilities.EnergyStorage.BLOCK,
                blockEntity.getBlockPos().relative(Direction.WEST), Direction.EAST) instanceof IEnergyStorage neighbour
                && blockEntity.getLevel().getCapability(Capabilities.EnergyStorage.BLOCK,
                blockEntity.getBlockPos(), Direction.WEST) instanceof  IEnergyStorage source)
            pushPower(source, neighbour);

        if (blockEntity.getLevel().getCapability(Capabilities.EnergyStorage.BLOCK,
                blockEntity.getBlockPos().relative(Direction.NORTH), Direction.SOUTH) instanceof IEnergyStorage neighbour
                && blockEntity.getLevel().getCapability(Capabilities.EnergyStorage.BLOCK,
                blockEntity.getBlockPos(), Direction.NORTH) instanceof  IEnergyStorage source)
            pushPower(source, neighbour);

        if (blockEntity.getLevel().getCapability(Capabilities.EnergyStorage.BLOCK,
                blockEntity.getBlockPos().relative(Direction.SOUTH), Direction.NORTH) instanceof IEnergyStorage neighbour
                && blockEntity.getLevel().getCapability(Capabilities.EnergyStorage.BLOCK,
                blockEntity.getBlockPos(), Direction.SOUTH) instanceof  IEnergyStorage source)
            pushPower(source, neighbour);

        if (!skipUp && blockEntity.getLevel().getCapability(Capabilities.EnergyStorage.BLOCK,
                blockEntity.getBlockPos().relative(Direction.UP), Direction.DOWN) instanceof IEnergyStorage neighbour
                && blockEntity.getLevel().getCapability(Capabilities.EnergyStorage.BLOCK,
                blockEntity.getBlockPos(), Direction.UP) instanceof  IEnergyStorage source)
            pushPower(source, neighbour);

        if (blockEntity.getLevel().getCapability(Capabilities.EnergyStorage.BLOCK,
                blockEntity.getBlockPos().relative(Direction.DOWN), Direction.UP) instanceof IEnergyStorage neighbour
                && blockEntity.getLevel().getCapability(Capabilities.EnergyStorage.BLOCK,
                blockEntity.getBlockPos(), Direction.DOWN) instanceof  IEnergyStorage source)
            pushPower(source, neighbour);
    }
}
