package com.leetftw.tech_mod.client.gui;

import com.leetftw.tech_mod.LeetTechMod;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidInteractionRegistry;
import net.neoforged.neoforge.fluids.FluidStack;

public class RenderUtils
{
    private static final ResourceLocation ENERGY_SPRITE_OFF =
            ResourceLocation.fromNamespaceAndPath(LeetTechMod.MOD_ID, "container/common/rf_off");
    private static final ResourceLocation ENERGY_SPRITE_ON =
            ResourceLocation.fromNamespaceAndPath(LeetTechMod.MOD_ID, "container/common/rf_on");

    private static final int ENERGY_WIDTH = 12;
    private static final int FLUID_WIDTH = 16;

    public static void drawEnergyBar(GuiGraphics guiGraphics, int x, int y, int height, int energyStored, int maxEnergyStored)
    {
        // Render energy
        guiGraphics.blitSprite(RenderType::guiTextured,
                ENERGY_SPRITE_ON,
                x, y,   // Target (x,y)
                ENERGY_WIDTH, height);  // Rendered texture size (cx,cy)

        int requiredHeight = (int) (((long)(maxEnergyStored - energyStored)) * height / maxEnergyStored);
        guiGraphics.blitSprite(RenderType::guiTextured,
                ENERGY_SPRITE_OFF,
                x, y,   // Target (x,y)
                ENERGY_WIDTH, requiredHeight); // Rendered texture size (cx,cy)
    }

    public static void drawFluidBar(GuiGraphics guiGraphics, Minecraft minecraft, int x, int y, int height, FluidStack stack, int maxAmount)
    {
        if (stack.isEmpty() || stack.getFluid() == Fluids.EMPTY || maxAmount <= 0) return;

        var attributes = IClientFluidTypeExtensions.of(stack.getFluid());
        ResourceLocation fluidLoc = attributes.getStillTexture(stack);
        TextureAtlasSprite sprite = minecraft.getTextureAtlas(ResourceLocation.fromNamespaceAndPath("minecraft", "textures/atlas/blocks.png")).apply(fluidLoc);
        int tint = attributes.getTintColor();

        int requiredHeight = (int) (((long)stack.getAmount()) * height / maxAmount);

        float atlasWidth = (float)sprite.contents().width() / (sprite.getU1() - sprite.getU0());
        float atlasHeight = (float)sprite.contents().height() / (sprite.getV1() - sprite.getV0());


        int heightRendered = 0;
        while (heightRendered < requiredHeight)
        {
            int heightRemaining = requiredHeight - heightRendered;
            int heightToRender = Math.min(heightRemaining, FLUID_WIDTH);

            guiGraphics.blit(RenderType::guiTextured, sprite.atlasLocation(),
                    x, y + height - heightRendered - heightToRender, // Position to render (x,y)
                    sprite.getX(), sprite.getY() + ((float) ((FLUID_WIDTH - heightToRender) * sprite.contents().height()) / FLUID_WIDTH), // Source position (x,y)
                    FLUID_WIDTH, heightToRender, // Size to render (cx,cy)
                    sprite.contents().width(), heightToRender * sprite.contents().height() / FLUID_WIDTH ,  // Source size (cx,cy)
                    (int)atlasWidth, (int)atlasHeight, // Full texture size
                    tint);

            heightRendered += heightToRender;
        }
    }
}
