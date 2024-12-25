package com.leetftw.test_mod.item;

import com.leetftw.test_mod.LeetTechMod;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;

import java.util.*;

@EventBusSubscriber(modid = LeetTechMod.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
class HammerBreakingHandler
{
    private static final HashSet<BlockPos> POSITIONS_MINED = new HashSet<>();
    private static final int HAMMER_EXTENDED_RANGE = 1;

    @SubscribeEvent
    public static void onBreakBlock(BlockEvent.BreakEvent event)
    {
        // If the position was already selected for mining by this function we skip it
        if (POSITIONS_MINED.contains(event.getPos()))
            return;

        // We are only interested in server events
        Player player = event.getPlayer();
        if (!(player instanceof ServerPlayer serverPlayer))
            return;

        // We are only interested in the hammer item
        Item item = serverPlayer.getMainHandItem().getItem();
        if (!(item instanceof AestheticHammerItem hammerItem))
            return;

        // Do hit detection
        BlockHitResult hitResult = event.getLevel().clip(
                // We do a ray-cast from the players eye position
                new ClipContext(serverPlayer.getEyePosition(1f),
                // To 6 blocks in the direction the player is facing
                serverPlayer.getEyePosition(1f).add(serverPlayer.getViewVector(1f).scale(serverPlayer.blockInteractionRange())),
                // And we only collide with blocks
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, serverPlayer));

        // Check if we actually hit something
        if (hitResult.getType() != HitResult.Type.BLOCK)
            return;

        // Determine neighbouring blocks based on direction and range
        switch (((BlockHitResult) hitResult).getDirection())
        {
            case UP:
            case DOWN:
                for (int dx = -HAMMER_EXTENDED_RANGE; dx <= HAMMER_EXTENDED_RANGE; dx++)
                    for (int dz = -HAMMER_EXTENDED_RANGE; dz <= HAMMER_EXTENDED_RANGE; dz++)
                    {
                        POSITIONS_MINED.add(event.getPos().offset(dx, 0, dz));
                    }
            break;

            case NORTH:
            case SOUTH:
                for (int dx = -HAMMER_EXTENDED_RANGE; dx <= HAMMER_EXTENDED_RANGE; dx++)
                    for (int dy = -HAMMER_EXTENDED_RANGE; dy <= HAMMER_EXTENDED_RANGE; dy++)
                    {
                        POSITIONS_MINED.add(event.getPos().offset(dx, dy, 0));
                    }
            break;

            case EAST:
            case WEST:
                for (int dz = -HAMMER_EXTENDED_RANGE; dz <= HAMMER_EXTENDED_RANGE; dz++)
                    for (int dy = -HAMMER_EXTENDED_RANGE; dy <= HAMMER_EXTENDED_RANGE; dy++)
                    {
                        POSITIONS_MINED.add(event.getPos().offset(0, dy, dz));
                    }
            break;
        }

        for (BlockPos pos : POSITIONS_MINED)
        {
            if (pos == event.getPos() || !hammerItem.isCorrectToolForDrops(serverPlayer.getMainHandItem(), event.getLevel().getBlockState(pos)))
                continue;

            serverPlayer.gameMode.destroyBlock(pos);
        }

        POSITIONS_MINED.clear();
    }
}

public class AestheticHammerItem extends DiggerItem
{
    public AestheticHammerItem(ToolMaterial material, float attackDamage, float attackSpeed, Properties properties)
    {
        super(material, BlockTags.MINEABLE_WITH_PICKAXE, attackDamage, attackSpeed, properties);
    }
}
