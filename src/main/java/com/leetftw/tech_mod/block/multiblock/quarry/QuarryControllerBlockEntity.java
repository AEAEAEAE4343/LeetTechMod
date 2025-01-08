package com.leetftw.tech_mod.block.multiblock.quarry;

import com.leetftw.tech_mod.block.entity.BaseLeetBlockEntity;
import com.leetftw.tech_mod.item.MachineUpgradeItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class QuarryControllerBlockEntity extends BaseLeetBlockEntity
{
    private final int BASE_ENERGY_USAGE = 2048;
    private final int BASE_PROCESSING_TIME = 20;
    private int progress = 0;
    //private bool formed = false;

    public QuarryControllerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState)
    {
        super(type, pos, blockState);
    }

    private boolean isCorner(BlockState blockState)
    {
        if (!(blockState.getBlock() instanceof QuarryFrameBlock))
            return false;

        boolean northCon = blockState.getValue(QuarryFrameBlock.NORTH_CON);
        boolean eastCon = blockState.getValue(QuarryFrameBlock.EAST_CON);
        boolean southCon = blockState.getValue(QuarryFrameBlock.SOUTH_CON);
        boolean westCon = blockState.getValue(QuarryFrameBlock.WEST_CON);
        boolean upCon = blockState.getValue(QuarryFrameBlock.UP_CON);
        boolean downCon = blockState.getValue(QuarryFrameBlock.DOWN_CON);

        return (northCon ^ southCon)
                && (eastCon ^ westCon)
                && (upCon ^ downCon);
    }

    private List<Direction> getConnections(BlockState blockState)
    {
        List<Direction> connections = new ArrayList<>();
        boolean northCon = blockState.getValue(QuarryFrameBlock.NORTH_CON);
        if (northCon) connections.add(Direction.NORTH);
        boolean eastCon = blockState.getValue(QuarryFrameBlock.EAST_CON);
        if (eastCon) connections.add(Direction.EAST);
        boolean southCon = blockState.getValue(QuarryFrameBlock.SOUTH_CON);
        if (southCon) connections.add(Direction.SOUTH);
        boolean westCon = blockState.getValue(QuarryFrameBlock.WEST_CON);
        if (westCon) connections.add(Direction.WEST);
        boolean upCon = blockState.getValue(QuarryFrameBlock.UP_CON);
        if (upCon) connections.add(Direction.UP);
        boolean downCon = blockState.getValue(QuarryFrameBlock.DOWN_CON);
        if (downCon) connections.add(Direction.DOWN);
        return connections;
    }

    // As complicated as this function looks, it's complexity is at max O(max edge length)
    public boolean checkFormed(Level level, BlockPos pos)
    {
        // First check if we have a neighbouring corner piece facing up
        // corner piece = 3 connections
        BlockPos startingPosition = null;

        BlockState north = level.getBlockState(pos.relative(Direction.NORTH));
        BlockState east = level.getBlockState(pos.relative(Direction.EAST));
        BlockState south = level.getBlockState(pos.relative(Direction.SOUTH));
        BlockState west = level.getBlockState(pos.relative(Direction.WEST));

        if (isCorner(north) && north.getValue(QuarryFrameBlock.UP_CON))
            startingPosition = pos.relative(Direction.NORTH);
        if (isCorner(east) && east.getValue(QuarryFrameBlock.UP_CON))
            startingPosition = pos.relative(Direction.EAST);
        if (isCorner(south) && south.getValue(QuarryFrameBlock.UP_CON))
            startingPosition = pos.relative(Direction.SOUTH);
        if (isCorner(west) && west.getValue(QuarryFrameBlock.UP_CON))
            startingPosition = pos.relative(Direction.WEST);

        if (startingPosition == null)
            return false;

        // We go along each direction and check:
        // - Is it an edge that has two connections along the same axis? continue to next position
        // - Is it a corner that has at least a connection with the axis we are traversing? save it and finish the traversal of the current direction
        // - Is it anything else? FAIL
        List<Direction> directions = getConnections(level.getBlockState(startingPosition));
        ArrayList<BlockPos> corners = new ArrayList<>();
        corners.add(startingPosition);
        for (Direction direction : directions)
        {
            BlockPos currentPos = startingPosition;
            while (true)
            {
                currentPos = currentPos.relative(direction);
                BlockState currentState = level.getBlockState(currentPos);
                if (currentState.getBlock() instanceof QuarryFrameBlock)
                {
                    List<Direction> connections = getConnections(level.getBlockState(currentPos));
                    // Valid edge piece
                    if (connections.size() == 2
                        && connections.contains(direction)
                        && connections.contains(direction.getOpposite()))
                        continue;

                    // Valid corner piece
                    if (connections.size() == 3 && connections.contains(direction.getOpposite()))
                        break;
                }

                // Unexpected state: invalid formation.
                return false;
            }

            // If the loop succesfully exits, we know we have found a corner
            corners.add(currentPos);
        }

        // 2. If we haven't failed we now have 4 corners (the original starting position + one for each connection of the starting position).
        // Since it is a known shape (a rectangular prism) we can determine the four other corners easily. We check if those exist.
        Set<Integer> xValues = new HashSet<>();
        Set<Integer> yValues = new HashSet<>();
        Set<Integer> zValues = new HashSet<>();

        for (BlockPos corner : corners)
        {
            xValues.add(corner.getX());
            yValues.add(corner.getY());
            zValues.add(corner.getZ());
        }

        Integer[] xArray = xValues.toArray(new Integer[0]);
        Integer[] yArray = yValues.toArray(new Integer[0]);
        Integer[] zArray = zValues.toArray(new Integer[0]);

        Arrays.sort(xArray);
        Arrays.sort(yArray);
        Arrays.sort(zArray);

        corners.clear();
        corners.add(new BlockPos(xArray[0], yArray[0], zArray[0]));
        corners.add(new BlockPos(xArray[0], yArray[0], zArray[1]));
        corners.add(new BlockPos(xArray[0], yArray[1], zArray[0]));
        corners.add(new BlockPos(xArray[0], yArray[1], zArray[1]));
        corners.add(new BlockPos(xArray[1], yArray[0], zArray[0]));
        corners.add(new BlockPos(xArray[1], yArray[0], zArray[1]));
        corners.add(new BlockPos(xArray[1], yArray[1], zArray[0]));
        corners.add(new BlockPos(xArray[1], yArray[1], zArray[1]));

        ArrayList<BlockPos> edges = new ArrayList<>();

        // 3. Now with all 8 corners, we check each corner.
        // Fail if that is not the case.
        // 0, 0, 0: south up east
        BlockState cornerState = level.getBlockState(corners.get(0));
        if (!cornerState.getValue(QuarryFrameBlock.SOUTH_CON)
                || !cornerState.getValue(QuarryFrameBlock.UP_CON)
                || !cornerState.getValue(QuarryFrameBlock.EAST_CON))
            return false;
        // 0, 0, 1: north up east
        cornerState = level.getBlockState(corners.get(1));
        if (!cornerState.getValue(QuarryFrameBlock.NORTH_CON)
                || !cornerState.getValue(QuarryFrameBlock.UP_CON)
                || !cornerState.getValue(QuarryFrameBlock.EAST_CON))
            return false;
        // 0, 1, 0: south down east
        cornerState = level.getBlockState(corners.get(2));
        if (!cornerState.getValue(QuarryFrameBlock.SOUTH_CON)
                || !cornerState.getValue(QuarryFrameBlock.DOWN_CON)
                || !cornerState.getValue(QuarryFrameBlock.EAST_CON))
            return false;
        // 0, 1, 1: north down east
        cornerState = level.getBlockState(corners.get(3));
        if (!cornerState.getValue(QuarryFrameBlock.NORTH_CON)
                || !cornerState.getValue(QuarryFrameBlock.DOWN_CON)
                || !cornerState.getValue(QuarryFrameBlock.EAST_CON))
            return false;
        // 1, 0, 0: south up west
        cornerState = level.getBlockState(corners.get(4));
        if (!cornerState.getValue(QuarryFrameBlock.SOUTH_CON)
                || !cornerState.getValue(QuarryFrameBlock.UP_CON)
                || !cornerState.getValue(QuarryFrameBlock.WEST_CON))
            return false;
        // 1, 0, 1: north up west
        cornerState = level.getBlockState(corners.get(5));
        if (!cornerState.getValue(QuarryFrameBlock.NORTH_CON)
                || !cornerState.getValue(QuarryFrameBlock.UP_CON)
                || !cornerState.getValue(QuarryFrameBlock.WEST_CON))
            return false;
        // 1, 1, 0: south down west
        cornerState = level.getBlockState(corners.get(6));
        if (!cornerState.getValue(QuarryFrameBlock.SOUTH_CON)
                || !cornerState.getValue(QuarryFrameBlock.DOWN_CON)
                || !cornerState.getValue(QuarryFrameBlock.WEST_CON))
            return false;
        // 1, 1, 1: north down west
        cornerState = level.getBlockState(corners.get(7));
        if (!cornerState.getValue(QuarryFrameBlock.NORTH_CON)
                || !cornerState.getValue(QuarryFrameBlock.DOWN_CON)
                || !cornerState.getValue(QuarryFrameBlock.WEST_CON))
            return false;

        // 4. We traverse the 12 edges of the prism
        // This assumes the corners have at least 1 edge in between.
        // (0, 0, 0) -> (1, 0, 0)
        for (BlockPos edge = corners.get(0b000).relative(Direction.EAST);
             edge.getX() < corners.get(0b100).getX();
             edge = edge.relative(Direction.EAST))
        {
            BlockState edgeState = level.getBlockState(edge);
            List<Direction> connections = getConnections(edgeState);
            if (connections.size() != 2
                    || !connections.contains(Direction.EAST)
                    || !connections.contains(Direction.WEST))
                return false;
            edges.add(edge);
        }

        // (0, 0, 0) -> (0, 1, 0)
        for (BlockPos edge = corners.get(0b000).relative(Direction.UP);
             edge.getY() < corners.get(0b010).getY();
             edge = edge.relative(Direction.UP))
        {
            BlockState edgeState = level.getBlockState(edge);
            List<Direction> connections = getConnections(edgeState);
            if (connections.size() != 2
                    || !connections.contains(Direction.UP)
                    || !connections.contains(Direction.DOWN))
                return false;
            edges.add(edge);
        }

        // (0, 0, 0) -> (0, 0, 1)
        for (BlockPos edge = corners.get(0b000).relative(Direction.SOUTH);
             edge.getZ() < corners.get(0b001).getZ();
             edge = edge.relative(Direction.SOUTH))
        {
            BlockState edgeState = level.getBlockState(edge);
            List<Direction> connections = getConnections(edgeState);
            if (connections.size() != 2
                    || !connections.contains(Direction.SOUTH)
                    || !connections.contains(Direction.NORTH))
                return false;
            edges.add(edge);
        }

        // (1, 0, 0) -> (1, 1, 0)
        for (BlockPos edge = corners.get(0b100).relative(Direction.UP);
             edge.getY() < corners.get(0b110).getY();
             edge = edge.relative(Direction.UP))
        {
            BlockState edgeState = level.getBlockState(edge);
            List<Direction> connections = getConnections(edgeState);
            if (connections.size() != 2
                    || !connections.contains(Direction.UP)
                    || !connections.contains(Direction.DOWN))
                return false;
            edges.add(edge);
        }

        // (1, 0, 0) -> (1, 0, 1)
        for (BlockPos edge = corners.get(0b100).relative(Direction.SOUTH);
             edge.getZ() < corners.get(0b101).getZ();
             edge = edge.relative(Direction.SOUTH))
        {
            BlockState edgeState = level.getBlockState(edge);
            List<Direction> connections = getConnections(edgeState);
            if (connections.size() != 2
                    || !connections.contains(Direction.SOUTH)
                    || !connections.contains(Direction.NORTH))
                return false;
            edges.add(edge);
        }

        // (0, 1, 0) -> (1, 1, 0)
        for (BlockPos edge = corners.get(0b010).relative(Direction.EAST);
             edge.getX() < corners.get(0b110).getX();
             edge = edge.relative(Direction.EAST))
        {
            BlockState edgeState = level.getBlockState(edge);
            List<Direction> connections = getConnections(edgeState);
            if (connections.size() != 2
                    || !connections.contains(Direction.EAST)
                    || !connections.contains(Direction.WEST))
                return false;
            edges.add(edge);
        }

        // (0, 1, 0) -> (0, 1, 1)
        for (BlockPos edge = corners.get(0b010).relative(Direction.SOUTH);
             edge.getZ() < corners.get(0b011).getZ();
             edge = edge.relative(Direction.SOUTH))
        {
            BlockState edgeState = level.getBlockState(edge);
            List<Direction> connections = getConnections(edgeState);
            if (connections.size() != 2
                    || !connections.contains(Direction.SOUTH)
                    || !connections.contains(Direction.NORTH))
                return false;
            edges.add(edge);
        }

        // (0, 0, 1) -> (1, 0, 1)
        for (BlockPos edge = corners.get(0b001).relative(Direction.EAST);
             edge.getX() < corners.get(0b101).getX();
             edge = edge.relative(Direction.EAST))
        {
            BlockState edgeState = level.getBlockState(edge);
            List<Direction> connections = getConnections(edgeState);
            if (connections.size() != 2
                    || !connections.contains(Direction.EAST)
                    || !connections.contains(Direction.WEST))
                return false;
            edges.add(edge);
        }

        // (0, 0, 1) -> (0, 1, 1)
        for (BlockPos edge = corners.get(0b001).relative(Direction.UP);
             edge.getY() < corners.get(0b011).getY();
             edge = edge.relative(Direction.UP))
        {
            BlockState edgeState = level.getBlockState(edge);
            List<Direction> connections = getConnections(edgeState);
            if (connections.size() != 2
                    || !connections.contains(Direction.UP)
                    || !connections.contains(Direction.DOWN))
                return false;
            edges.add(edge);
        }

        // (1, 1, 0) -> (1, 1, 1)
        for (BlockPos edge = corners.get(0b110).relative(Direction.SOUTH);
             edge.getZ() < corners.get(0b111).getZ();
             edge = edge.relative(Direction.SOUTH))
        {
            BlockState edgeState = level.getBlockState(edge);
            List<Direction> connections = getConnections(edgeState);
            if (connections.size() != 2
                    || !connections.contains(Direction.SOUTH)
                    || !connections.contains(Direction.NORTH))
                return false;
            edges.add(edge);
        }

        // (1, 0, 1) -> (1, 1, 1)
        for (BlockPos edge = corners.get(0b101).relative(Direction.UP);
             edge.getY() < corners.get(0b111).getY();
             edge = edge.relative(Direction.UP))
        {
            BlockState edgeState = level.getBlockState(edge);
            List<Direction> connections = getConnections(edgeState);
            if (connections.size() != 2
                    || !connections.contains(Direction.UP)
                    || !connections.contains(Direction.DOWN))
                return false;
            edges.add(edge);
        }

        // (0, 1, 1) -> (1, 1, 1)
        for (BlockPos edge = corners.get(0b011).relative(Direction.EAST);
             edge.getX() < corners.get(0b111).getX();
             edge = edge.relative(Direction.EAST))
        {
            BlockState edgeState = level.getBlockState(edge);
            List<Direction> connections = getConnections(edgeState);
            if (connections.size() != 2
                    || !connections.contains(Direction.EAST)
                    || !connections.contains(Direction.WEST))
                return false;
            edges.add(edge);
        }

        // Finally we now we have a correct formation
        // Set FORMED to true for all blocks
        ArrayList<BlockPos> blocks = new ArrayList<>();
        blocks.addAll(corners);
        blocks.addAll(edges);
        for (BlockPos blockPos : blocks)
        {
            level.setBlockAndUpdate(blockPos, level.getBlockState(blockPos).setValue(QuarryFrameBlock.FORMED, true));
            level.getServer().getPlayerList().getPlayers().forEach(serverPlayer -> serverPlayer.sendSystemMessage(Component.literal("Block: " + blockPos)));
        }
        level.getServer().getPlayerList().getPlayers().forEach(serverPlayer -> serverPlayer.sendSystemMessage(Component.literal("Block count: " + blocks.size())));

        return true;
    }

    @Override
    public void tick(Level pLevel, BlockPos pPos, BlockState pState)
    {
        super.tick(pLevel, pPos, pState);

        // First check formation
        // NOT RUNNING THAT EVERY TICK LMAO
        /*if (!checkFormed())
            return;*/
    }

    @Override
    protected int itemsGetSlotCount() {
        return 1;
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
        return 1;
    }

    @Override
    protected int fluidsGetSlotCapacity(int i) {
        return 4000;
    }

    @Override
    protected boolean fluidsAllowInsert(int slot, Fluid fluid) {
        return true;
    }

    @Override
    protected boolean fluidsAllowExtract(int slot) {
        return false;
    }

    @Override
    protected int energyGetCapacity() {
        return 100000;
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
        return 10000;
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int i, @NotNull Inventory inventory, @NotNull Player player) {
        return null;
    }
}
