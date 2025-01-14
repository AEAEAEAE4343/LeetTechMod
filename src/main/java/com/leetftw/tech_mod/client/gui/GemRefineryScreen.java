package com.leetftw.tech_mod.client.gui;

import com.leetftw.tech_mod.LeetTechMod;
import com.leetftw.tech_mod.gui.GemRefineryMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;

public class GemRefineryScreen extends AbstractContainerScreen<GemRefineryMenu>
{
    private static final ResourceLocation BACKGROUND =
            ResourceLocation.fromNamespaceAndPath(LeetTechMod.MOD_ID, "textures/gui/container/gem_refinery.png");
    private static final ResourceLocation PROGRESS_SPRITE =
            ResourceLocation.fromNamespaceAndPath(LeetTechMod.MOD_ID, "textures/gui/sprites/container/gem_refinery/progress.png");

    private static final ResourceLocation UPGRADE_OVERLAY =
            ResourceLocation.fromNamespaceAndPath(LeetTechMod.MOD_ID, "textures/gui/sprites/container/common/upgrade_overlay.png");

    private static final int PROGRESS_WIDTH = 40;
    private static final int PROGRESS_HEIGHT = 16;

    private static final int BG_WIDTH = 176;
    private static final int BG_HEIGHT = 166;

    // TODO: Make base Screen class for upgradeable blocks
    private static final int UPGRADE_OVERLAY_WIDTH = 55;
    private static final int UPGRADE_OVERLAY_HEIGHT = 58;

    public GemRefineryScreen(GemRefineryMenu menu, Inventory playerInventory, Component title)
    {
        super(menu, playerInventory, title);
        imageWidth = BG_WIDTH + UPGRADE_OVERLAY_WIDTH;
        // these are default already
        //imageWidth = 176;
        //imageHeight = 166;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);

        Component text = Component.translatable("gui.leet_tech.block_entity_upgrade_section");
        FormattedCharSequence formattedcharsequence = text.getVisualOrderText();

        guiGraphics.drawString(font, text, (BG_WIDTH + 25) - font.width(formattedcharsequence) / 2,
                titleLabelY + 2, 4210752, false);
        //guiGraphics.drawCenteredString(font, Component.translatable("gui.leet_tech.block_entity_upgrade_section"),
        //        titleLabelX + BG_WIDTH + 25, titleLabelY + 3, 4210752);
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

        // draw upgrade overlay
        guiGraphics.blit(RenderType::guiTextured,
                UPGRADE_OVERLAY,
                leftPos + BG_WIDTH, topPos + 3,
                0, 0,
                UPGRADE_OVERLAY_WIDTH, UPGRADE_OVERLAY_HEIGHT,
                UPGRADE_OVERLAY_WIDTH, UPGRADE_OVERLAY_HEIGHT);

        if (menu.isCrafting())
        {
            guiGraphics.blit(RenderType::guiTextured,
                    PROGRESS_SPRITE,
                    leftPos + 62, topPos + 35,   // Target (x,y)
                    0, 0,                      // Source (x,y)
                    menu.getProgress() * PROGRESS_WIDTH / menu.getMaxProgress(), PROGRESS_HEIGHT,  // Size to render (cx,cy)
                    PROGRESS_WIDTH, PROGRESS_HEIGHT); // Full texture size (cx,cy);
        }
    }
}
