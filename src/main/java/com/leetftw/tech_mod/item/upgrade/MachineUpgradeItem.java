package com.leetftw.tech_mod.item.upgrade;

import com.leetftw.tech_mod.item.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
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
            return Component.translatable("item.leet_tech.leet_tech_machine_upgrade.base");
        }
        return Component.translatable("item." + id.getNamespace() + ".leet_tech_machine_upgrade." + id.getPath());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag)
    {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);

        // This is always a ResourceLocation, but we do a null check and save the variable
        // in the same line
        if (stack.get(ModDataComponents.MACHINE_UPGRADE) instanceof ResourceLocation id
            && context.level() != null)
        {
            Style red = Style.EMPTY.withColor(ChatFormatting.RED);
            Style green = Style.EMPTY.withColor(ChatFormatting.DARK_GREEN);

            MachineUpgrade upgrade = MachineUpgrade.fromId(context.level().registryAccess(), id);
            String translationKey = String.format("tooltip.%s.leet_tech_machine_upgrade.%s", id.getNamespace(), id.getPath());
            if (upgrade.hasTooltip())
                tooltipComponents.add(Component.translatable(translationKey).withStyle(ChatFormatting.GRAY));

            if (upgrade.getSpeedMultiplier() >= 0f
            && upgrade.getSpeedMultiplier() != 1f)
                tooltipComponents.add(
                        Component.translatable(upgrade.getSpeedMultiplier() < 1f ?
                                "tooltip.leet_tech.machine_upgrade.decrease" : "tooltip.leet_tech.machine_upgrade.increase")
                        .append(" ")
                        .append(Component.translatable("tooltip.leet_tech.machine_upgrade.speed"))
                        .append(" ")
                        .withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(String.format("%.1f%%", Math.abs((1f - upgrade.getSpeedMultiplier()) * 100)))
                                .withStyle(upgrade.getSpeedMultiplier() > 1f ? red : green)));

            if (upgrade.getEfficiencyMultiplier() >= 0
            && upgrade.getEfficiencyMultiplier() != 1f)
                tooltipComponents.add(
                        Component.translatable(upgrade.getEfficiencyMultiplier() < 1f ?
                                "tooltip.leet_tech.machine_upgrade.decrease" : "tooltip.leet_tech.machine_upgrade.increase")
                        .append(" ")
                        .append(Component.translatable("tooltip.leet_tech.machine_upgrade.efficiency"))
                        .append(" ")
                        .withStyle(ChatFormatting.GRAY)
                        .append(Component.literal(String.format("%.1f%%", Math.abs((1f - upgrade.getEfficiencyMultiplier()) * 100)))
                                .withStyle(upgrade.getEfficiencyMultiplier() > 1f ? red : green)));

            if (tooltipFlag.isAdvanced()) tooltipComponents.add(Component.literal("Upgrade ID: " + id).withStyle(ChatFormatting.DARK_GRAY));
        }
        else tooltipComponents.add(Component.literal("ID: §kUnknown"));

    }

    @Override
    public int getMaxStackSize(ItemStack stack)
    {
        return 16;
    }
}
