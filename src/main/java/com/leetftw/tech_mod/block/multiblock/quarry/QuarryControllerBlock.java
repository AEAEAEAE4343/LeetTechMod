package com.leetftw.tech_mod.block.multiblock.quarry;

import com.leetftw.tech_mod.block.BaseLeetEntityBlock;
import com.leetftw.tech_mod.block.HorizontalLeetEntityBlock;
import com.leetftw.tech_mod.block.ModBlocks;
import com.leetftw.tech_mod.block.entity.BaseLeetBlockEntity;
import com.leetftw.tech_mod.block.entity.CrystallizerBlockEntity;
import com.leetftw.tech_mod.block.entity.ModBlockEntities;
import com.leetftw.tech_mod.block.multiblock.StaticMultiBlockPart;
import com.leetftw.tech_mod.util.DebugHelper;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Stream;

public class QuarryControllerBlock extends HorizontalLeetEntityBlock
{
    ArrayList<Triple<BooleanProperty, BooleanProperty, BooleanProperty>> cornerConnections = new ArrayList<>();
    ArrayList<Triple<Integer, Integer, Direction>> edgeConnections = new ArrayList<>();

    private static final int MAX_QUARRY_SIZE = 32;
    private static final int MAX_QUARRY_HEIGHT = 5;

    public QuarryControllerBlock(Properties properties)
    {
        super(properties);

        registerDefaultState(getStateDefinition().any()
                .setValue(StaticMultiBlockPart.FORMED, false)
                .setValue(FACING, Direction.NORTH));

        generateConnections();
    }

    private void generateConnections()
    {
        cornerConnections.add(Triple.of(QuarryFrameBlock.SOUTH_CON, QuarryFrameBlock.UP_CON, QuarryFrameBlock.EAST_CON));
        cornerConnections.add(Triple.of(QuarryFrameBlock.NORTH_CON, QuarryFrameBlock.UP_CON, QuarryFrameBlock.EAST_CON));
        cornerConnections.add(Triple.of(QuarryFrameBlock.SOUTH_CON, QuarryFrameBlock.DOWN_CON, QuarryFrameBlock.EAST_CON));
        cornerConnections.add(Triple.of(QuarryFrameBlock.NORTH_CON, QuarryFrameBlock.DOWN_CON, QuarryFrameBlock.EAST_CON));
        cornerConnections.add(Triple.of(QuarryFrameBlock.SOUTH_CON, QuarryFrameBlock.UP_CON, QuarryFrameBlock.WEST_CON));
        cornerConnections.add(Triple.of(QuarryFrameBlock.NORTH_CON, QuarryFrameBlock.UP_CON, QuarryFrameBlock.WEST_CON));
        cornerConnections.add(Triple.of(QuarryFrameBlock.SOUTH_CON, QuarryFrameBlock.DOWN_CON, QuarryFrameBlock.WEST_CON));
        cornerConnections.add(Triple.of(QuarryFrameBlock.NORTH_CON, QuarryFrameBlock.DOWN_CON, QuarryFrameBlock.WEST_CON));

        edgeConnections.add(Triple.of(0b000, 0b100, Direction.EAST));
        edgeConnections.add(Triple.of(0b000, 0b010, Direction.UP));
        edgeConnections.add(Triple.of(0b000, 0b001, Direction.SOUTH));
        edgeConnections.add(Triple.of(0b100, 0b110, Direction.UP));
        edgeConnections.add(Triple.of(0b100, 0b101, Direction.SOUTH));
        edgeConnections.add(Triple.of(0b010, 0b110, Direction.EAST));
        edgeConnections.add(Triple.of(0b010, 0b011, Direction.SOUTH));
        edgeConnections.add(Triple.of(0b001, 0b101, Direction.EAST));
        edgeConnections.add(Triple.of(0b001, 0b011, Direction.UP));
        edgeConnections.add(Triple.of(0b110, 0b111, Direction.SOUTH));
        edgeConnections.add(Triple.of(0b101, 0b111, Direction.UP));
        edgeConnections.add(Triple.of(0b011, 0b111, Direction.EAST));
    }

    private boolean isCorner(BlockState blockState)
    {
        if (!blockState.is(ModBlocks.QUARRY_FRAME))
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
        if (!blockState.is(ModBlocks.QUARRY_FRAME))
            return List.of();

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

    // As complicated as this function looks, it's complexity is at max O(frame block count)
    // This is as optimized as I can get it. It only gets ran once when the player right clicks the block
    // so this shouldn't be a hot path.
    // TODO: Component.translatable should be used here
    private FormationResult checkFormed(Level level, BlockPos pos, List<BlockPos> cornerPositions)
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
            return new FormationResult(false, "Found no neighbouring frame corner blocks!", pos);

        // We go along each direction and check:
        // - Is it an edge that has two connections along the same axis? continue to next position
        // - Is it a corner that has at least a connection with the axis we are traversing? save it and finish the traversal of the current direction
        // - Is it anything else? FAIL
        List<Direction> directions = getConnections(level.getBlockState(startingPosition));
        ArrayList<BlockPos> corners = new ArrayList<>();
        corners.add(startingPosition);
        for (Direction direction : directions)
        {
            int length = 1;
            BlockPos currentPos = startingPosition;
            while (true)
            {
                currentPos = currentPos.relative(direction);

                if (length == MAX_QUARRY_SIZE)
                    return new FormationResult(false, "Too big! Max size is 32x5x32", currentPos);
                else if (direction == Direction.UP && length == MAX_QUARRY_HEIGHT)
                    return new FormationResult(false, "Too big! Max size is 32x5x32", currentPos);

                BlockState currentState = level.getBlockState(currentPos);
                if (currentState.getBlock() instanceof QuarryFrameBlock)
                {
                    List<Direction> connections = getConnections(level.getBlockState(currentPos));
                    // Valid edge piece
                    if (connections.size() == 2
                            && connections.contains(direction)
                            && connections.contains(direction.getOpposite()))
                    {
                        length++;
                        continue;
                    }

                    // Valid corner piece
                    if (connections.size() == 3 && connections.contains(direction.getOpposite()))
                        break;

                    return new FormationResult(false, "Invalid connection!", currentPos);
                }

                // Unexpected state: invalid formation.
                return new FormationResult(false, "Expected frame block!", currentPos);
            }

            // If the loop succesfully exits, we know we have found a corner
            corners.add(currentPos);
        }

        if (corners.get(0).distManhattan(corners.get(1)) < 3) return new FormationResult(false, "Corner too close! Minimum size: 4x4x3!", corners.get(1));
        if (corners.get(0).distManhattan(corners.get(2)) < 3) return new FormationResult(false, "Corner too close! Minimum size: 4x4x3!", corners.get(2));
        if (corners.get(0).distManhattan(corners.get(3)) < 2) return new FormationResult(false, "Corner too close! Minimum size: 4x4x3!", corners.get(3));

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

        // 3. Now with all 8 corners, we check each corner.
        // Fail if that is not the case.
        for (int i = 0; i < cornerConnections.size(); i++)
        {
            BlockState cornerState = level.getBlockState(corners.get(i));
            Triple<BooleanProperty, BooleanProperty, BooleanProperty> properties = cornerConnections.get(i);
            if (!cornerState.is(ModBlocks.QUARRY_FRAME)
                    || !cornerState.getValue(properties.getLeft())
                    || !cornerState.getValue(properties.getMiddle())
                    || !cornerState.getValue(properties.getRight()))
                return new FormationResult(false, "Expected frame corner!", corners.get(i));
        }

        // 4. We traverse the 12 edges of the prism
        // This assumes the corners have at least 1 edge in between.
        ArrayList<BlockPos> edges = new ArrayList<>();
        for (Triple<Integer, Integer, Direction> directionTriple : edgeConnections)
        {
            for (BlockPos edge = corners.get(directionTriple.getLeft()).relative(directionTriple.getRight());
                 !edge.equals(corners.get(directionTriple.getMiddle()));
                 edge = edge.relative(directionTriple.getRight()))
            {
                BlockState edgeState = level.getBlockState(edge);
                List<Direction> connections = getConnections(edgeState);
                if (connections.size() != 2
                        || !connections.contains(directionTriple.getRight())
                        || !connections.contains(directionTriple.getRight().getOpposite()))
                    return new FormationResult(false, "Expected frame edge!", edge);
                edges.add(edge);
            }
        }

        // Finally we now have a correct formation
        // Set FORMED to true for all blocks
        ArrayList<BlockPos> blocks = new ArrayList<>();
        blocks.addAll(corners);
        blocks.addAll(edges);
        for (BlockPos blockPos : blocks)
        {
            level.setBlock(blockPos, level.getBlockState(blockPos).setValue(StaticMultiBlockPart.FORMED, true), Block.UPDATE_CLIENTS);
            //level.getServer().getPlayerList().getPlayers().forEach(serverPlayer -> serverPlayer.sendSystemMessage(Component.literal("Block: " + blockPos)));
        }

        // Save the corners
        if (cornerPositions != null)
        {
            cornerPositions.add(corners.get(0b000));
            cornerPositions.add(corners.get(0b111));
        }

        //level.getServer().getPlayerList().getPlayers().forEach(serverPlayer -> serverPlayer.sendSystemMessage(Component.literal("Block count: " + blocks.size())));
        return new FormationResult(true, null, null);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, @Nullable Orientation orientation, boolean movedByPiston)
    {
        // Only check formation if already formed
        // Otherwise, the formation is only checked when the player right clicks the machine
        if (state.getValue(StaticMultiBlockPart.FORMED))
        {
            FormationResult formed = checkFormed(level, pos, null);
            level.setBlockAndUpdate(pos, state.setValue(StaticMultiBlockPart.FORMED, formed.success));
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult)
    {
        InteractionResult returnVal = super.useWithoutItem(state, level, pos, player, hitResult);
        if (level.isClientSide) return returnVal;

        if (!state.getValue(StaticMultiBlockPart.FORMED))
        {
            List<BlockPos> corners = new ArrayList<>();
            FormationResult formation = checkFormed(level, pos, corners);
            Optional<QuarryControllerBlockEntity> beOptional = level.getBlockEntity(pos, ModBlockEntities.QUARRY_CONTROLLER_BE.get());
            if (formation.success)
            {
                beOptional.ifPresent(be ->
                        be.setCornerOne(corners.get(0)).setCornerTwo(corners.get(1)).setCurrentY(corners.get(0).getY() - 1).resetPos());
            }

            level.setBlockAndUpdate(pos, state.setValue(StaticMultiBlockPart.FORMED, formation.success));

            if (!formation.success)
            {
                beOptional.ifPresent(be -> be.highlightBadBlock(formation.mistakePos));
                ((ServerPlayer) player).sendSystemMessage(Component.literal(formation.mistakeText + " " + formation.mistakePos));
            }
            ((ServerPlayer) player).sendSystemMessage(Component.literal("Formed: " + formation.success));
        }
        else
        {
            BlockEntity entity = level.getBlockEntity(pos);
            ServerPlayer serverPlayer = (ServerPlayer) player;
            if (entity instanceof QuarryControllerBlockEntity blockEntity)
            {
                serverPlayer.openMenu(blockEntity, pos);
            }
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(StaticMultiBlockPart.FORMED);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston)
    {
        Stream.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST).map(pos::relative)
                .filter(neighbourPos -> level.getBlockState(neighbourPos).is(ModBlocks.QUARRY_FRAME))
                .forEach(neighbourPos -> level.setBlockAndUpdate(neighbourPos, level.getBlockState(neighbourPos).setValue(StaticMultiBlockPart.FORMED, false)));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return null;
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType)
    {
        return createTickerHelper(blockEntityType, blockEntityType,
                (pLevel, pPos, pState, pBlockEntity) ->
                {
                    if (pBlockEntity instanceof BaseLeetBlockEntity baseLeetBlockEntity)
                        baseLeetBlockEntity.tick(pLevel, pPos, pState);
                });
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new QuarryControllerBlockEntity(ModBlockEntities.QUARRY_CONTROLLER_BE.get(), blockPos, blockState);
    }

    private class FormationResult
    {
        public boolean success;
        public String mistakeText;
        public BlockPos mistakePos;

        public FormationResult(boolean success, String mistakeText, BlockPos mistakePos)
        {
            this.success = success;
            this.mistakeText = mistakeText;
            this.mistakePos = mistakePos;
        }
    }
}
