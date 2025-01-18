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
import net.minecraft.world.inventory.AbstractContainerMenu;

public abstract class UpgradeableScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T>
{
    private static final ResourceLocation UPGRADE_OVERLAY =
            ResourceLocation.fromNamespaceAndPath(LeetTechMod.MOD_ID, "textures/gui/sprites/container/common/upgrade_overlay.png");

    private static final int UPGRADE_OVERLAY_WIDTH = 55;
    private static final int UPGRADE_OVERLAY_HEIGHT = 58;

    private final int BG_WIDTH;

    public UpgradeableScreen(T menu, Inventory playerInventory, Component title, int width)
    {
        super(menu, playerInventory, title);
        BG_WIDTH = width;
        imageWidth = BG_WIDTH + UPGRADE_OVERLAY_WIDTH;
    }

    public UpgradeableScreen(T menu, Inventory playerInventory, Component title)
    {
        this(menu, playerInventory, title, 176);
    }

    /*public static <T extends AbstractContainerMenu> UpgradeableScreen<T> standalone(T menu, Inventory playerInventory, Component title)
    {
        UpgradeableScreen<T> result = new UpgradeableScreen<>(menu, playerInventory, Component.empty(), 0)
        {
            @Override
            protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) { }
        };
        result.imageHeight = UPGRADE_OVERLAY_HEIGHT;
        return result;
    }*/

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick)
    {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick)
    {
        super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        // draw upgrade overlay
        guiGraphics.blit(RenderType::guiTextured,
                UPGRADE_OVERLAY,
                leftPos + BG_WIDTH, topPos + 3,
                0, 0,
                UPGRADE_OVERLAY_WIDTH, UPGRADE_OVERLAY_HEIGHT,
                UPGRADE_OVERLAY_WIDTH, UPGRADE_OVERLAY_HEIGHT);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);

        Component text = Component.translatable("gui.leet_tech.block_entity_upgrade_section");
        FormattedCharSequence formattedcharsequence = text.getVisualOrderText();

        guiGraphics.drawString(font, text, (BG_WIDTH + 25) - font.width(formattedcharsequence) / 2,
                titleLabelY + 2, 4210752, false);
    }
}
