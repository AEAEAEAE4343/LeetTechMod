package com.leetftw.tech_mod.block.entity;

import com.leetftw.tech_mod.item.ModItems;
import com.leetftw.tech_mod.gui.GemRefineryMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

public class GemRefineryBlockEntity extends BaseLeetBlockEntity
{
    private static final int INVENTORY_SIZE = 3;
    private static final int INPUT_DIAMOND_SLOT = 0;
    private static final int INPUT_GLOWSTONE_SLOT = 1;
    private static final int OUTPUT_SLOT = 2;

    protected final SimpleContainerData data;
    private int progress = 0;
    private int maxProgress = 120;

    public GemRefineryBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState)
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
                    case 1 -> maxProgress;
                    default -> 0;
                };
            }

            @Override
            public void set(int pIndex, int pValue)
            {
                switch (pIndex)
                {
                    case 0 -> progress = pValue;
                    case 1 -> maxProgress = pValue;
                }
            }
        };
    }

    @Override
    protected int itemsGetSlotCount()
    {
        return INVENTORY_SIZE;
    }

    @Override
    protected boolean itemsAllowInsert(int slot, Item stack)
    {
        if (slot == INPUT_DIAMOND_SLOT && stack == Items.DIAMOND)
            return true;

        if (slot == INPUT_GLOWSTONE_SLOT && stack == Items.GLOWSTONE_DUST)
            return true;

        return false;
    }

    @Override
    protected boolean itemsAllowExtract(int slot)
    {
        return slot == OUTPUT_SLOT;
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
        return 10000;
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
        return 100;
    }

    // Thanks for the reference KaupenJoe!
    // https://github.com/Tutorials-By-Kaupenjoe/Forge-Tutorial-1.20.X/tree/30-blockEntity
    // MIT License
    public void tick(Level level, BlockPos pos, BlockState state)
    {
        if (hasRecipe())
        {
            setProgress(progress + 1);
            setChanged(level, pos, state);

            if (progress >= maxProgress) {
                craftItem();
                setProgress(0);
            }
        }
        else setProgress(0);
    }

    private void craftItem()
    {
        ItemStack result = new ItemStack(ModItems.AESTHETIC_DUST.get(), 1);
        itemsGetSlot(INPUT_DIAMOND_SLOT).shrink(1);
        itemsGetSlot(INPUT_GLOWSTONE_SLOT).shrink(1);

        itemsSetSlot(OUTPUT_SLOT, new ItemStack(result.getItem(),
                itemsGetSlot(OUTPUT_SLOT).getCount() + result.getCount()));
    }

    private boolean hasRecipe()
    {
        boolean hasCraftingItem = itemsGetSlot(INPUT_DIAMOND_SLOT).getItem() == Items.DIAMOND
                && itemsGetSlot(INPUT_GLOWSTONE_SLOT).getItem() == Items.GLOWSTONE_DUST;
        ItemStack result = new ItemStack(ModItems.AESTHETIC_DUST.get());

        return hasCraftingItem && itemsCanInsertIntoSlot(OUTPUT_SLOT, result, true, false);
    }

    private void setProgress(int newProg)
    {
        progress = newProg;

        /*if (progress == 0) setItem(8, ItemStack.EMPTY);
        else setItem(8, new ItemStack(Items.DIRT, progress / 2));*/
    }

    // BlockEntity
    @Override
    @ParametersAreNonnullByDefault
    protected void saveAdditional(CompoundTag pTag, HolderLookup.Provider registries)
    {
        super.saveAdditional(pTag, registries);
        pTag.putInt("gem_refinery.progress", progress);
    }

    @Override
    @ParametersAreNonnullByDefault
    public void loadAdditional(CompoundTag pTag, HolderLookup.Provider registries)
    {
        super.loadAdditional(pTag, registries);
        progress = pTag.getInt("gem_refinery.progress");
    }

    @Override
    @ParametersAreNonnullByDefault
    public @Nullable AbstractContainerMenu createMenu(int i, Inventory inventory, Player player)
    {
        return new GemRefineryMenu(i, inventory, ContainerLevelAccess.create(player.level(), getBlockPos()), this, data);
    }
}
