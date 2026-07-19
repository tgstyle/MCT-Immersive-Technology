package mctmods.immersivetechnology.client.gui.helper;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mctmods.immersivetechnology.client.renderer.helper.RenderTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;
import org.joml.Matrix4f;

public class GuiHelper {
    public static void drawRepeatedFluidSpriteGui(MultiBufferSource buffer, PoseStack transform, FluidStack fluid, float x, float y, float w, float h) {
        RenderType renderType = RenderTypes.getGuiTranslucent(InventoryMenu.BLOCK_ATLAS);
        VertexConsumer builder = buffer.getBuffer(renderType);
        drawRepeatedFluidSprite(builder, transform, fluid, x, y, w, h);
    }

    public static void drawRepeatedFluidSprite(VertexConsumer builder, PoseStack transform, FluidStack fluid, float x, float y, float w, float h) {
        IClientFluidTypeExtensions props = IClientFluidTypeExtensions.of(fluid.getFluid());
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(props.getStillTexture(fluid));
        int col = props.getTintColor(fluid);
        float r = ((col >> 16) & 255) / 255f;
        float g = ((col >> 8) & 255) / 255f;
        float b = (col & 255) / 255f;
        float a = 1f;
        int iW = sprite.contents().width();
        int iH = sprite.contents().height();
        if (iW > 0 && iH > 0) { drawRepeatedSprite(builder, transform, x, y, w, h, iW, iH, sprite.getU0(), sprite.getU1(), sprite.getV0(), sprite.getV1(), r, g, b, a); }
    }

    public static void drawRepeatedSprite(VertexConsumer builder, PoseStack transform, float x, float y, float w, float h, int iconWidth, int iconHeight, float uMin, float uMax, float vMin, float vMax, float r, float g, float b, float alpha) {
        int iterMaxW = (int)(w / iconWidth);
        int iterMaxH = (int)(h / iconHeight);
        float leftoverW = w % iconWidth;
        float leftoverH = h % iconHeight;
        float leftoverWf = leftoverW / (float)iconWidth;
        float leftoverHf = leftoverH / (float)iconHeight;
        float iconUDif = uMax - uMin;
        float iconVDif = vMax - vMin;
        for (int ww = 0; ww < iterMaxW; ++ww) {
            for (int hh = 0; hh < iterMaxH; ++hh) { drawTexturedColoredRect(builder, transform, x + ww * iconWidth, y + hh * iconHeight, iconWidth, iconHeight, r, g, b, alpha, uMin, uMax, vMin, vMax); }
            drawTexturedColoredRect(builder, transform, x + ww * iconWidth, y + iterMaxH * iconHeight, iconWidth, leftoverH, r, g, b, alpha, uMin, uMax, vMin, vMin + iconVDif * leftoverHf);
        }
        if (leftoverW > 0.0f) {
            for (int hh = 0; hh < iterMaxH; ++hh) { drawTexturedColoredRect(builder, transform, x + iterMaxW * iconWidth, y + hh * iconHeight, leftoverW, iconHeight, r, g, b, alpha, uMin, uMin + iconUDif * leftoverWf, vMin, vMax); }
            drawTexturedColoredRect(builder, transform, x + iterMaxW * iconWidth, y + iterMaxH * iconHeight, leftoverW, leftoverH, r, g, b, alpha, uMin, uMin + iconUDif * leftoverWf, vMin, vMin + iconVDif * leftoverHf);
        }
    }

    public static void drawTexturedRect(VertexConsumer builder, PoseStack transform, float x, float y, float w, float h, float picSize, float u0, float u1, float v0, float v1) { drawTexturedColoredRect(builder, transform, x, y, w, h, 1f, 1f, 1f, 1f, u0 / picSize, u1 / picSize, v0 / picSize, v1 / picSize); }

    public static void drawTexturedColoredRect(VertexConsumer builder, PoseStack transform, float x, float y, float w, float h, float r, float g, float b, float alpha, float u0, float u1, float v0, float v1) {
        Matrix4f mat = transform.last().pose();
        int ir = (int)(255 * r);
        int ig = (int)(255 * g);
        int ib = (int)(255 * b);
        int ia = (int)(255 * alpha);
        int light = LightTexture.pack(15, 15);
        byte nx = 0;
        byte ny = 0;
        byte nz = 127;
        builder.vertex(mat, x, y + h, 0).color(ir, ig, ib, ia).uv(u0, v1).uv2(light).normal(nx, ny, nz).endVertex();
        builder.vertex(mat, x + w, y + h, 0).color(ir, ig, ib, ia).uv(u1, v1).uv2(light).normal(nx, ny, nz).endVertex();
        builder.vertex(mat, x + w, y, 0).color(ir, ig, ib, ia).uv(u1, v0).uv2(light).normal(nx, ny, nz).endVertex();
        builder.vertex(mat, x, y, 0).color(ir, ig, ib, ia).uv(u0, v0).uv2(light).normal(nx, ny, nz).endVertex();
    }
}
