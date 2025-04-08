package com.leetftw.tech_mod.client.gui;

import com.leetftw.tech_mod.LeetTechMod;
import com.leetftw.tech_mod.gui.CrystalInjectorMenu;
import com.leetftw.tech_mod.gui.QuarryMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class QuarryScreen extends UpgradeableScreen<QuarryMenu>
{
    private static final ResourceLocation BACKGROUND =
            ResourceLocation.fromNamespaceAndPath(LeetTechMod.MOD_ID, "textures/gui/container/gem_refinery.png");

    private static final int BG_WIDTH = 176;
    private static final int BG_HEIGHT = 166;

    public QuarryScreen(QuarryMenu menu, Inventory playerInventory, Component title)
    {
        super(menu, playerInventory, title, BG_WIDTH);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float v, int i, int i1)
    {
        guiGraphics.blit(RenderType::guiTextured,
                BACKGROUND,
                leftPos, topPos,
                0, 0,
                BG_WIDTH, BG_HEIGHT,
                256, 256);

        // Draw energy bar
        RenderUtils.drawEnergyBar(guiGraphics, 0, 0, 0, menu.getEnergyStored(), menu.getMaxEnergy());

        // Draw greyed out buffer slot

    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY)
    {
        super.renderLabels(guiGraphics, mouseX, mouseY);

        // Draw status label
        guiGraphics.drawString(font, "Status: " + menu.getState().name(), 0, 0, 0, false);

        // Draw coordinates
        guiGraphics.drawString(font, "Position: %d, %d, %d".formatted(menu.getCurrentX(), menu.getCurrentY(), menu.getCurrentZ()), 0, 10, 0, false);

        // Potentially draw statistics?
        // Movement speed + mining speed
    }
}
