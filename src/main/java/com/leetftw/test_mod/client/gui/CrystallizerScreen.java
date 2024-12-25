package com.leetftw.test_mod.client.gui;

import com.leetftw.test_mod.LeetTechMod;
import com.leetftw.test_mod.gui.CrystallizerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;

public class CrystallizerScreen extends AbstractContainerScreen<CrystallizerMenu>
{
    private static final ResourceLocation BACKGROUND =
            ResourceLocation.fromNamespaceAndPath(LeetTechMod.MOD_ID, "textures/gui/container/crystallizer.png");
    private static final ResourceLocation PROGRESS_SPRITE =
            ResourceLocation.fromNamespaceAndPath(LeetTechMod.MOD_ID, "textures/gui/sprites/container/crystallizer/progress.png");

    private static final ResourceLocation ENERGY_SPRITE_OFF =
            ResourceLocation.fromNamespaceAndPath(LeetTechMod.MOD_ID, "container/common/rf_off");
    private static final ResourceLocation ENERGY_SPRITE_ON =
            ResourceLocation.fromNamespaceAndPath(LeetTechMod.MOD_ID, "container/common/rf_on");

    private static final int PROGRESS_WIDTH = 24;
    private static final int PROGRESS_HEIGHT = 16;

    private static final int ENERGY_WIDTH = 12;
    private static final int ENERGY_HEIGHT = 54;

    private static final int FLUID_WIDTH = 16;
    private static final int FLUID_HEIGHT = 54;

    public CrystallizerScreen(CrystallizerMenu menu, Inventory playerInventory, Component title)
    {
        super(menu, playerInventory, title);
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
    protected void renderBg(GuiGraphics guiGraphics, float v, int i, int i1)
    {
        // Draw BG
        guiGraphics.blit(RenderType::guiTextured,
                BACKGROUND,
                leftPos, topPos,
                0, 0,
                imageWidth, imageHeight,
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

        // Draw Energy
        guiGraphics.blitSprite(RenderType::guiTextured,
                ENERGY_SPRITE_ON,
                leftPos + 15, topPos + 16,   // Target (x,y)
                ENERGY_WIDTH, ENERGY_HEIGHT);  // Rendered texture size (cx,cy)

        int totalHeight = ENERGY_HEIGHT;
        int requiredHeight = (menu.getMaxEnergy() - menu.getEnergyStored()) * totalHeight / menu.getMaxEnergy();
        guiGraphics.blitSprite(RenderType::guiTextured,
                ENERGY_SPRITE_OFF,
                leftPos + 15, topPos + 16,   // Target (x,y)
                ENERGY_WIDTH, requiredHeight); // Rendered texture size (cx,cy)

        // Render fluid
        FluidStack stack = menu.getFluid();
        Fluid fluid = stack.getFluid();

        if (fluid != Fluids.EMPTY)
        {
            var attributes = IClientFluidTypeExtensions.of(fluid);
            ResourceLocation fluidLoc = attributes.getStillTexture(stack);
            TextureAtlasSprite sprite = getMinecraft().getTextureAtlas(ResourceLocation.fromNamespaceAndPath("minecraft", "textures/atlas/blocks.png")).apply(fluidLoc);
            int tint = attributes.getTintColor();
            //var atlas = (TextureAtlas) Minecraft.getInstance().getTextureManager().getTexture(sprite.atlasLocation());

            totalHeight = FLUID_HEIGHT;
            requiredHeight = stack.getAmount() * totalHeight / menu.getMaxFluid();
            int heightRendered = 0;
            while (heightRendered < requiredHeight)
            {
                //    int x, int y,
                //    float uOffset, float vOffset,
                //    int uWidth, int vHeight,
                //    int width, int height,
                //    int textureWidth, int textureHeight,
                //    int color) {
                int heightRemaining = requiredHeight - heightRendered;
                int heightToRender = Math.min(heightRemaining, 16);
                guiGraphics.blit(RenderType::guiTextured, sprite.atlasLocation(),
                        leftPos + 31, topPos + 16 + FLUID_HEIGHT - heightRendered - heightToRender, // Position to render (x,y)
                        sprite.getX(), sprite.getY() + ((FLUID_WIDTH - heightToRender) * FLUID_WIDTH / sprite.contents().width()),                           // Source position (x,y)
                        FLUID_WIDTH, heightToRender,                            // Size to render (cx,cy)
                        sprite.contents().width(), heightToRender * FLUID_WIDTH / sprite.contents().width(),  // Source size (cx,cy)
                        1024, 1024,                                 // Full texture size
                        tint);

                heightRendered += heightToRender;
            }
        }
    }
}
