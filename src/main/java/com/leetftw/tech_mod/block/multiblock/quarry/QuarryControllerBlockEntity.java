package com.leetftw.tech_mod.block.multiblock.quarry;

import com.leetftw.tech_mod.LeetTechMod;
import com.leetftw.tech_mod.block.entity.BaseLeetBlockEntity;
import com.leetftw.tech_mod.block.entity.UpgradeableLeetBlockEntity;
import com.leetftw.tech_mod.block.multiblock.StaticMultiBlockPart;
import com.leetftw.tech_mod.item.upgrade.MachineUpgrade;
import com.leetftw.tech_mod.util.DebugHelper;
import com.leetftw.tech_mod.util.ItemHelper;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class QuarryControllerBlockEntity extends UpgradeableLeetBlockEntity
{
    private final static int BASE_ENERGY_USAGE = 512;
    private final static int BASE_PROCESSING_TIME = 20;
    private final static ItemStack HARVEST_TOOL = new ItemStack(Items.NETHERITE_PICKAXE, 1);

    private final static ResourceLocation SILK_TOUCH_UPGRADE = ResourceLocation.fromNamespaceAndPath(LeetTechMod.MOD_ID, "quarry_silk_touch");

    private static final int UPGRADE_SLOTS = 4;

    private BlockPos cornerOne = BlockPos.ZERO;
    private BlockPos cornerTwo = BlockPos.ZERO;

    private int progress = 0;
    private int currentY = 0;
    private int currentBlockRelX = 0;
    private int currentBlockRelZ = 0;
    private int movementTicks = 0;

    private int nextY = 0;
    private int nextBlockRelX = 0;
    private int nextBlockRelZ = 0;

    private boolean includeBlockHighlightInUpdate = false;
    private BlockPos badBlockPosition = BlockPos.ZERO;
    private int badBlockHighlightDuration = 0;

    // Client only, used for animations;
    private long lastSync = 0;

    private State currentState = State.Halted;
    private List<ItemStack> currentItems = List.of();

    public QuarryControllerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState)
    {
        super(type, pos, blockState);
    }

    // TODO: remove
    public String getState()
    {
        return currentState.name();
    }

    // All these functions use ABSOLUTE positions
    // All the private fields use RELATIVE positions
    public QuarryControllerBlockEntity setCornerOne(BlockPos cornerOne) {
        this.cornerOne = cornerOne.subtract(getBlockPos());
        setChangedAndUpdate();
        return this;
    }

    public QuarryControllerBlockEntity setCornerTwo(BlockPos cornerTwo) {
        this.cornerTwo = cornerTwo.subtract(getBlockPos());
        setChangedAndUpdate();
        return this;
    }

    public QuarryControllerBlockEntity setCurrentY(int currentY) {
        this.currentY = currentY;
        setChangedAndUpdate();
        return this;
    }

    public BlockPos getCornerOne()
    {
        return cornerOne.offset(getBlockPos());
    }

    public BlockPos getCornerTwo()
    {
        return cornerTwo.offset(getBlockPos());
    }

    public BlockPos getTargetPosition()
    {
        return getBlockPos().offset(cornerOne).offset(currentBlockRelX + 1, 0, currentBlockRelZ + 1).atY(currentY);
    }

    public BlockPos getNextPosition()
    {
        return getBlockPos().offset(cornerOne).offset(nextBlockRelX + 1, 0, nextBlockRelZ + 1).atY(nextY);
    }

    public int getMovementTicks()
    {
        return movementTicks;
    }

    public int getProgress() {
        // Game doesn't sync every tick, so progress must be fluent for rendering
        if (currentState == State.Moving && level instanceof ClientLevel clientLevel)
            return progress - (int)(clientLevel.getGameTime() - lastSync);

        return progress;
    }

    public boolean isMoving()
    {
        return currentState == State.Moving;
    }

    public void decreaseProgress()
    {
        int energyUsage = getEnergyUsage(BASE_ENERGY_USAGE);
        progress--;
        setChangedAndUpdate();
    }

    public void resetPos()
    {
        currentBlockRelX = 0;
        currentBlockRelZ = 0;
        currentState = State.Idling;
        calculateNextPosition();
        setChangedAndUpdate();
    }

    public void highlightBadBlock(BlockPos pos)
    {
        if (!(level instanceof ServerLevel serverLevel))
        {
            throw new IllegalArgumentException("Level must be a ServerLevel");
        }

        badBlockPosition = pos;
        includeBlockHighlightInUpdate = true;
        setChangedAndUpdate();
    }

    public int getBadBlockDuration()
    {
        return badBlockHighlightDuration;
    }

    public BlockPos getBadBlock()
    {
        return badBlockPosition;
    }

    public static List<ItemStack> getBlockDrops(BlockState blockState, BlockPos pos, Level level)
    {
        if (!(level instanceof ServerLevel serverLevel)) {
            throw new IllegalArgumentException("Level must be a ServerLevel");
        }

        // Build the LootContext
        LootParams.Builder lootParamsBuilder = new LootParams.Builder(serverLevel)
                .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
                .withParameter(LootContextParams.BLOCK_STATE, blockState);

        // TODO: Quarry upgrades
        //       Perhaps allow for an enchantment book to be used for stuff like Silk Touch and Fortune?
        ItemStack tool = HARVEST_TOOL; //.copy();
        //tool.enchant(serverLevel.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SILK_TOUCH), 1);
        //tool.enchant(serverLevel.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.FORTUNE), 3);
        lootParamsBuilder.withParameter(LootContextParams.TOOL, tool);

        // Get the drops
        return blockState.getDrops(lootParamsBuilder);
    }

    private void calculateNextPosition()
    {
        int maxX = cornerTwo.subtract(cornerOne).getX() - 1;
        int maxZ = cornerTwo.subtract(cornerOne).getZ() - 1;

        nextBlockRelX = currentBlockRelX + 1;
        nextY = currentY;
        nextBlockRelZ = currentBlockRelZ;
        if (nextBlockRelX == maxX)
        {
            nextBlockRelX = 0;
            nextBlockRelZ = currentBlockRelZ + 1;

            if (nextBlockRelZ == maxZ)
            {
                nextBlockRelZ = 0;
                nextY = currentY - 1;
            }
            else nextY = currentY;
        }

        setChangedAndUpdate();
    }

    private void advancePosition()
    {
        currentBlockRelX = nextBlockRelX;
        currentBlockRelZ = nextBlockRelZ;
        currentY = nextY;
        calculateNextPosition();
    }

    private void calculateMoveProgress()
    {
        double movementSpeed = 1.0 / getProcessingTime(BASE_PROCESSING_TIME / 4.0); // Meters / Tick
        double distance = Math.sqrt(
                Math.pow(nextBlockRelX - currentBlockRelX, 2) +
                        Math.pow(nextBlockRelZ - currentBlockRelZ, 2)
        );

        if (distance == 0)
        {
            advancePosition();
            currentState = State.Idling;
            return;
        }

        progress = (int) Math.ceil(distance / movementSpeed);
        movementTicks = progress;

        DebugHelper.chatOutput("Moving to: " + getNextPosition());
    }


    @Override
    public void tick(Level pLevel, BlockPos pPos, BlockState pState)
    {
        super.tick(pLevel, pPos, pState);

        if (badBlockHighlightDuration > 0) badBlockHighlightDuration--;
        if (level instanceof ClientLevel || currentState == State.Halted) return;

        if (!pState.getValue(StaticMultiBlockPart.FORMED)) {
            currentState = State.Idling;
            return;
        }

        // Find the block at the current position
        BlockPos targetPos = getTargetPosition();
        BlockState targetBlock = pLevel.getBlockState(targetPos);

        // My favorite CS concept: state machine
        switch (currentState)
        {
            case MiningEmpty:
                decreaseProgress();
                if (progress == 0)
                {
                    currentState = State.Moving;
                    calculateMoveProgress();
                }
                break;

            case MiningBlock:
                decreaseProgress();
                if (progress != 0) break;


                // TODO: Upgrade that allows the quarry to place dirt to fill in water?
                FluidState fluidState = targetBlock.getFluidState();
                if (!fluidState.isEmpty())
                {
                    level.setBlockAndUpdate(targetPos, Blocks.AIR.defaultBlockState());
                    currentItems = List.of();
                }
                else if ((!targetBlock.requiresCorrectToolForDrops() // Doesn't need correct tool
                        || HARVEST_TOOL.isCorrectToolForDrops(targetBlock)) // Correct tool
                        && targetBlock.getDestroySpeed(level, targetPos) >= 0) // Not unbreakable
                {
                    currentItems = getBlockDrops(targetBlock, pPos, level);
                    level.setBlockAndUpdate(targetPos, Blocks.AIR.defaultBlockState());
                }
                else currentItems = List.of();

                if (currentItems.isEmpty())
                {
                    currentState = State.Moving;
                    calculateMoveProgress();
                    break;
                }
                currentState = State.PushingItems;
            case PushingItems: //i.e. MiningBlock phase 2
                IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, pPos.relative(Direction.UP), Direction.DOWN);
                if (handler == null) break;
                currentItems = currentItems.stream().map(item -> {
                    for (int i = 0; i < handler.getSlots() && !item.isEmpty(); i++)
                    {
                        item = handler.insertItem(i, item.copy(), false);
                        if (item.isEmpty()) break;
                    }
                    return item;
                }).filter(item -> !item.isEmpty()).toList();
                if (!currentItems.isEmpty()) break;
                currentState = State.Moving;
                calculateMoveProgress();
                break;

            case Moving:
                decreaseProgress();
                if (progress == 0) {
                    advancePosition(); // Increment position
                    currentState = State.Idling;

                    // If we moved we need to refresh this
                    targetPos = getTargetPosition();
                    targetBlock = pLevel.getBlockState(targetPos);
                }
                break;

            case Halted:
            case Idling:
            default:
                break;
        }

        if (currentY < level.getMinY())
            currentState = State.Halted;

        if (currentState != State.Idling)
            return;


        if (targetBlock.isAir())
        {
            // Empty block, go into idling state
            DebugHelper.chatOutput("Mining air: " + getTargetPosition());
            currentState = State.MiningEmpty;
            progress = getProcessingTime(BASE_PROCESSING_TIME / 5);
            return;
        }

        // Real block, go into mining state
        DebugHelper.chatOutput("Mining block: " + getTargetPosition());
        currentState = State.MiningBlock;
        progress = getProcessingTime(BASE_PROCESSING_TIME);

        // TODO: Don't forget to consume power and Liquid Aesthetic
    }

    //region BASE BLOCK ENTITY IMPLEMENTATION
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
    //endregion

    private void saveData(CompoundTag pTag, HolderLookup.Provider registries, boolean save, boolean update)
    {
        if (cornerOne != null) pTag.putLong("quarry_c1", cornerOne.asLong());
        if (cornerOne != null) pTag.putLong("quarry_c2", cornerTwo.asLong());
        pTag.putInt("quarry_x", currentBlockRelX);
        pTag.putInt("quarry_y", currentY);
        pTag.putInt("quarry_z", currentBlockRelZ);

        pTag.putInt("quarry_nx", nextBlockRelX);
        pTag.putInt("quarry_ny", nextY);
        pTag.putInt("quarry_nz", nextBlockRelZ);

        pTag.putInt("quarry_move", movementTicks);
        pTag.putInt("quarry_progress", progress);
        pTag.putString("quarry_state", currentState.name());

        if (update)
        {
            pTag.putLong("quarry_sync", level.getGameTime());
            if (includeBlockHighlightInUpdate)
            {
                pTag.putLong("quarry_badblock_pos", badBlockPosition.asLong());
                includeBlockHighlightInUpdate = false;
            }
        }
        if (save) ItemHelper.saveItemsToTag(pTag, registries, "quarry_output_buffer", currentItems.stream());
    }

    //region DATA STORAGE / SYNCING
    @Override
    public void loadAdditional(CompoundTag pTag, HolderLookup.Provider registries) {
        //
        //    private int currentY = 0;
        //    private int currentBlockRelX = 0;
        //    private int currentBlockRelZ = 0;
        super.loadAdditional(pTag, registries);
        if (pTag.contains("quarry_c1"))
            cornerOne = BlockPos.of(pTag.getLong("quarry_c1"));
        if (pTag.contains("quarry_c2"))
            cornerTwo = BlockPos.of(pTag.getLong("quarry_c2"));

        if (pTag.contains("quarry_x"))
            currentBlockRelX = pTag.getInt("quarry_x");
        if (pTag.contains("quarry_y"))
            currentY = pTag.getInt("quarry_y");
        if (pTag.contains("quarry_z"))
            currentBlockRelZ = pTag.getInt("quarry_z");

        if (pTag.contains("quarry_nx"))
            nextBlockRelX = pTag.getInt("quarry_nx");
        if (pTag.contains("quarry_ny"))
            nextY = pTag.getInt("quarry_ny");
        if (pTag.contains("quarry_nz"))
            nextBlockRelZ = pTag.getInt("quarry_nz");

        if (pTag.contains("quarry_progress"))
            progress = pTag.getInt("quarry_progress");
        if (pTag.contains("quarry_move"))
            movementTicks = pTag.getInt("quarry_move");

        if (pTag.contains("quarry_state"))
            currentState = State.valueOf(pTag.getString("quarry_state"));

        if (pTag.contains("quarry_sync"))
            lastSync = pTag.getLong("quarry_sync");
        if (pTag.contains("quarry_output_buffer"))
        {
            // Make sure it is mutable
            currentItems = new ArrayList<>();
            ListTag itemList = pTag.getList("quarry_output_buffer", 10);
            for(int i = 0; i < itemList.size(); ++i)
            {
                CompoundTag itemTags = itemList.getCompound(i);
                ItemStack.parse(registries, itemTags).ifPresent((stack) -> currentItems.add(stack));
            }
        }

        if (pTag.contains("quarry_badblock_pos"))
        {
            badBlockPosition = BlockPos.of(pTag.getLong("quarry_badblock_pos"));
            badBlockHighlightDuration = 100;
        }
    }

    @Override
    public int upgradesGetSlotCount()
    {
        return UPGRADE_SLOTS;
    }

    @Override
    public boolean upgradesAllowUpgrade(MachineUpgrade upgradeItem)
    {
        return upgradeItem.getUpgradeId().compareNamespaced(SILK_TOUCH_UPGRADE) == 0;
    }

    @Override
    protected void saveAdditional(CompoundTag pTag, HolderLookup.Provider registries) {
        super.saveAdditional(pTag, registries);
        saveData(pTag, registries, true, false);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveData(tag, registries, false, true);
        return tag;
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
    //endregion

    @Override
    public @Nullable AbstractContainerMenu createMenu(int i, @NotNull Inventory inventory, @NotNull Player player) {
        return null;
    }

    private enum State {
        Idling,
        Moving,
        MiningEmpty,
        MiningBlock,
        PushingItems,
        Halted,
    }
}
