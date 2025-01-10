package com.leetftw.tech_mod.block.multiblock.quarry;

import com.leetftw.tech_mod.block.entity.BaseLeetBlockEntity;
import com.leetftw.tech_mod.block.multiblock.StaticMultiBlockPart;
import com.leetftw.tech_mod.item.MachineUpgradeItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.plaf.basic.BasicComboBoxUI;
import java.util.*;

public class QuarryControllerBlockEntity extends BaseLeetBlockEntity
{
    private final int BASE_ENERGY_USAGE = 2048;
    private final int BASE_PROCESSING_TIME = 20;

    private BlockPos cornerOne = null;
    private BlockPos cornerTwo = null;

    private int progress = 0;
    private int currentY = 0;
    private int currentBlockRelX = 0;
    private int currentBlockRelZ = 0;
    private State currentState = State.Halted;
    private List<ItemStack> currentItems = null;

    private enum State {
        Idling,
        MiningEmpty,
        MiningBlock,
        PushingItems,
        Halted,
    }

    public QuarryControllerBlockEntity setCornerOne(BlockPos cornerOne) {
        this.cornerOne = cornerOne.subtract(getBlockPos());
        return this;
    }

    public QuarryControllerBlockEntity setCornerTwo(BlockPos cornerTwo) {
        this.cornerTwo = cornerTwo.subtract(getBlockPos());
        return this;
    }

    public QuarryControllerBlockEntity setCurrentY(int currentY) {
        this.currentY = currentY;
        return this;
    }

    public void resetPos() {
        currentBlockRelX = 0;
        currentBlockRelZ = 0;
        currentState = State.Idling;
    }

    public QuarryControllerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState)
    {
        super(type, pos, blockState);
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

        // TODO: Silk touch upgrade :)
        // Add silk touch?
        ItemStack tool = new ItemStack(Items.DIAMOND_PICKAXE, 1);
        //tool.enchant(serverLevel.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SILK_TOUCH), 1);
        lootParamsBuilder.withParameter(LootContextParams.TOOL, tool);

        // Get the drops
        return blockState.getDrops(lootParamsBuilder);
    }

    private void advancePosition()
    {
        int maxX = cornerTwo.subtract(cornerOne).getX() - 1;
        int maxZ = cornerTwo.subtract(cornerOne).getZ() - 1;

        currentBlockRelX++;
        if (currentBlockRelX == maxX)
        {
            currentBlockRelX = 0;
            currentBlockRelZ++;

            if (currentBlockRelZ == maxZ)
            {
                currentBlockRelZ = 0;
                currentY--;
            }
        }
    }

    @Override
    public void tick(Level pLevel, BlockPos pPos, BlockState pState)
    {
        super.tick(pLevel, pPos, pState);
        if (level.isClientSide || currentState == State.Halted) return;

        if (!pState.getValue(StaticMultiBlockPart.FORMED)) {
            currentState = State.Idling;
            return;
        }

        // Find the block at the current position
        BlockPos targetPos = pPos.offset(cornerOne).offset(currentBlockRelX + 1, 0, currentBlockRelZ + 1).atY(currentY);
        BlockState targetBlock = pLevel.getBlockState(targetPos);

        // My favorite CS concept: state machine
        switch (currentState)
        {
            case MiningEmpty:
                progress--;
                if (progress == 0)
                {
                    advancePosition();
                    currentState = State.Idling;
                }
                break;

            case MiningBlock:
                progress--;
                if (progress != 0) break;

                currentItems = getBlockDrops(targetBlock, pPos, level);
                level.setBlockAndUpdate(targetPos, Blocks.AIR.defaultBlockState());
                advancePosition();

                if (currentItems.isEmpty())
                {
                    currentState = State.Idling;
                    break;
                }
                currentState = State.PushingItems;

            case PushingItems:
                IItemHandler handler = level.getCapability(Capabilities.ItemHandler.BLOCK, pPos.relative(Direction.UP), Direction.DOWN);
                if (handler == null) break;
                currentItems = currentItems.stream().map(item -> {
                    for (int i = 0; i < handler.getSlots(); i++) {
                        item = handler.insertItem(i, item, false);
                    }
                    return item;
                }).filter(item -> !item.isEmpty()).toList();
                if (!currentItems.isEmpty()) break;
                currentState = State.Idling;
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
            //level.getServer().getPlayerList().getPlayers().forEach(serverPlayer -> serverPlayer.sendSystemMessage(Component.literal("Mining air: " + targetPos)));
            // Empty block, go into idling state
            currentState = State.Idling;
            progress = 5;
            return;
        }

        //level.getServer().getPlayerList().getPlayers().forEach(serverPlayer -> serverPlayer.sendSystemMessage(Component.literal("Mining block: " + targetPos)));
        // Real block, go into mining state
        currentState = State.MiningBlock;
        progress = 20;

        // TODO: Don't forget to consume power and Liquid Aesthetic
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
