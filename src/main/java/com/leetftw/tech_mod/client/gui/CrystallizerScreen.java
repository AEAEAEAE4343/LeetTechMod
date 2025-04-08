package com.leetftw.tech_mod.client.gui;

import com.leetftw.tech_mod.LeetTechMod;
import com.leetftw.tech_mod.gui.CrystallizerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;

public class CrystallizerScreen extends UpgradeableScreen<CrystallizerMenu>
{
    private static final ResourceLocation BACKGROUND =
            ResourceLocation.fromNamespaceAndPath(LeetTechMod.MOD_ID, "textures/gui/container/crystallizer.png");
    private static final ResourceLocation PROGRESS_SPRITE =
            ResourceLocation.fromNamespaceAndPath(LeetTechMod.MOD_ID, "textures/gui/sprites/container/crystallizer/progress.png");

    private static final ResourceLocation ENERGY_SPRITE_OFF =
            ResourceLocation.fromNamespaceAndPath(LeetTechMod.MOD_ID, "container/common/rf_off");
    private static final ResourceLocation ENERGY_SPRITE_ON =
            ResourceLocation.fromNamespaceAndPath(LeetTechMod.MOD_ID, "container/common/rf_on");

    private static final int BG_WIDTH = 176;
    private static final int BG_HEIGHT = 166;

    private static final int PROGRESS_WIDTH = 24;
    private static final int PROGRESS_HEIGHT = 16;

    private static final int ENERGY_HEIGHT = 54;

    private static final int FLUID_HEIGHT = 54;

    public CrystallizerScreen(CrystallizerMenu menu, Inventory playerInventory, Component title)
    {
        super(menu, playerInventory, title);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float v, int i, int i1)
    {
        // Draw BG
        guiGraphics.blit(RenderType::guiTextured,
                BACKGROUND,
                leftPos, topPos,
                0, 0,
                BG_WIDTH, BG_HEIGHT,
                256, 256);

        // Draw progress
        if (menu.isCrafting())
        {
            guiGraphics.blit(RenderType::guiTextured,
                    PROGRESS_SPRITE,
                    leftPos + 92, topPos + 36,   // Target (x,y)
                    0, 0,                      // Source (x,y)
                    menu.getProgress() * PROGRESS_WIDTH / menu.getMaxProgress(), PROGRESS_HEIGHT,  // Size to render (cx,cy)
                    PROGRESS_WIDTH, PROGRESS_HEIGHT); // Full texture size (cx,cy);
        }

        // Render energy
        RenderUtils.drawEnergyBar(guiGraphics, leftPos + 15, topPos + 16, ENERGY_HEIGHT, menu.getEnergyStored(), menu.getMaxEnergy());

        // Render fluid
        RenderUtils.drawFluidBar(guiGraphics, getMinecraft(), leftPos + 31, topPos + 16, FLUID_HEIGHT, menu.getFluid(), menu.getMaxFluid());
    }
}
