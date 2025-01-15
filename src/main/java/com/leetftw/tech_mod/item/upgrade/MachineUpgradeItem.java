package com.leetftw.tech_mod.item.upgrade;

import com.leetftw.tech_mod.item.ModDataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public class MachineUpgradeItem extends Item
{
    public MachineUpgradeItem(Properties properties)
    {
        super(properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        ResourceLocation id = stack.get(ModDataComponents.MACHINE_UPGRADE);
        if (id == null) {
            return Component.translatable("item.leet_tech.leet_tech_machine_upgrade_base");
        }
        return Component.translatable("item." + id.getNamespace() + ".leet_tech_machine_upgrade." + id.getPath());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag)
    {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);

        tooltipComponents.add(Component.literal("ID: " + stack.get(ModDataComponents.MACHINE_UPGRADE).toString()));
    }
}
