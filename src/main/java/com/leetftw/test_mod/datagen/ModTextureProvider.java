package com.leetftw.test_mod.datagen;

import com.leetftw.test_mod.LeetTechMod;
import com.leetftw.test_mod.datagen.texture.TextureProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.util.List;

public class ModTextureProvider extends TextureProvider
{
    public ModTextureProvider(PackOutput packOutput)
    {
        super(packOutput, LeetTechMod.MOD_ID);
    }

    // https://stackoverflow.com/a/3514297
    static BufferedImage deepCopy(BufferedImage bi)
    {
        ColorModel cm = bi.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = bi.copyData(null);
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
    }

    public BufferedImage generateCellTexture(List<BufferedImage> sourceTextures, Integer state)
    {
        BufferedImage base = sourceTextures.get(0);
        BufferedImage off = sourceTextures.get(1);
        BufferedImage on = sourceTextures.get(2);

        // Draw base
        BufferedImage newTexture = deepCopy(base);
        Graphics g = newTexture.getGraphics();

        // Draw RF off across entire section
        for (int y = 2; y <= 12; y += 2)
            g.drawImage(off, 2, y, null);

        // Draw RF on depending on state
        while (state > 0)
        {
            int srcX = 0;
            int srcY = state % 2;
            int srcW = on.getWidth();
            int srcH = 1;
            int srcDX = srcX + srcW;
            int srcDY = srcY + srcH;
            int dstX = 2;
            int dstY = 14 - state;
            int dstW = on.getWidth();
            int dstH = 1;
            int dstDX = dstX + dstW;
            int dstDY = dstY + dstH;
            g.drawImage(on, dstX, dstY, dstDX, dstDY, srcX, srcY, srcDX, srcDY, null);
            state--;
        }

        return newTexture;
    }

    @Override
    protected void registerTextureGenerators()
    {
        for (int i = 0; i <= 12; i++)
        {
            fromExisting(List.of(ResourceLocation.fromNamespaceAndPath(LeetTechMod.MOD_ID, "block/energy_cell_side"),
                            ResourceLocation.fromNamespaceAndPath(LeetTechMod.MOD_ID, "gui/sprites/container/common/rf_off"),
                            ResourceLocation.fromNamespaceAndPath(LeetTechMod.MOD_ID, "gui/sprites/container/common/rf_on")),
                    ResourceLocation.fromNamespaceAndPath(LeetTechMod.MOD_ID, "block/energy_cell_front_" + i),
                    this::generateCellTexture, i);

            fromExisting(List.of(ResourceLocation.fromNamespaceAndPath(LeetTechMod.MOD_ID, "block/creative_energy_cell_side"),
                            ResourceLocation.fromNamespaceAndPath(LeetTechMod.MOD_ID, "gui/sprites/container/common/rf_off"),
                            ResourceLocation.fromNamespaceAndPath(LeetTechMod.MOD_ID, "gui/sprites/container/common/rf_on")),
                    ResourceLocation.fromNamespaceAndPath(LeetTechMod.MOD_ID, "block/creative_energy_cell_front_" + i),
                    this::generateCellTexture, i);
        }
    }
}
