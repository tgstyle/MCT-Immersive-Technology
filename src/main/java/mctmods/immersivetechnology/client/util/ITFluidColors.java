package mctmods.immersivetechnology.client.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

public class ITFluidColors {
    public static int color(FluidStack fluid) {
        int tint = fluid.getFluid().getColor(fluid);
        ResourceLocation still = fluid.getFluid().getStill(fluid);
        TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(still.toString());
        int[] pixels = sprite.getFrameTextureData(0)[0];
        int textureColor = textureColor(pixels);
        int r = ((textureColor >> 16 & 0xff) * (tint >> 16 & 0xff)) / 255;
        int g = ((textureColor >> 8 & 0xff) * (tint >> 8 & 0xff)) / 255;
        int b = ((textureColor & 0xff) * (tint & 0xff)) / 255;
        return 0xff000000 | r << 16 | g << 8 | b;
    }

    private static int textureColor(int[] pixels) {
        int avgR = 0, avgG = 0, avgB = 0, count = 0;
        for (int p : pixels) {
            int alpha = p >> 24 & 0xff;
            if (alpha > 0) {
                avgR += (p >> 16 & 0xff) * alpha / 255;
                avgG += (p >> 8 & 0xff) * alpha / 255;
                avgB += (p & 0xff) * alpha / 255;
                count++;
            }
        }
        if (count > 0) {
            avgR /= count;
            avgG /= count;
            avgB /= count;
        }
        return 0xff000000 | avgR << 16 | avgG << 8 | avgB;
    }
}
