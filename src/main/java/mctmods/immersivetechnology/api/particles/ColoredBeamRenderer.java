package mctmods.immersivetechnology.api.particles;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

public class ColoredBeamRenderer {
    private static final ResourceLocation BEAM_TEXTURE = new ResourceLocation("textures/entity/beacon_beam.png");
    private static final double BEAM_RADIUS = 0.2D;
    private static final double GLOW_RADIUS = 0.25D;

    public static void renderBeam(double localX, double localYBase, double localZ, float height, float partialTicks, float textureScale,
                                  float innerBottomG, float innerTopG, float innerA, float outerBottomG, float outerTopG, float outerA) {
        double totalWorldTime = Minecraft.getMinecraft().world.getTotalWorldTime();
        double d0 = totalWorldTime + partialTicks;
        double d1 = -d0;
        double frac = MathHelper.frac(d1 * 0.2D - (double) MathHelper.floor(d1 * 0.1D));

        Minecraft.getMinecraft().renderEngine.bindTexture(BEAM_TEXTURE);
        GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.disableBlend();
        GlStateManager.depthMask(true);
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

        GlStateManager.pushMatrix();
        GlStateManager.translate(localX, localYBase, localZ);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        double spin = d0 * 0.025D * -1.5D;
        double aX = 0.5D + Math.cos(spin + Math.PI / 4D) * BEAM_RADIUS;
        double aZ = 0.5D + Math.sin(spin + Math.PI / 4D) * BEAM_RADIUS;
        double bX = 0.5D + Math.cos(spin + 3D * Math.PI / 4D) * BEAM_RADIUS;
        double bZ = 0.5D + Math.sin(spin + 3D * Math.PI / 4D) * BEAM_RADIUS;
        double cX = 0.5D + Math.cos(spin + 5D * Math.PI / 4D) * BEAM_RADIUS;
        double cZ = 0.5D + Math.sin(spin + 5D * Math.PI / 4D) * BEAM_RADIUS;
        double dX = 0.5D + Math.cos(spin + 7D * Math.PI / 4D) * BEAM_RADIUS;
        double dZ = 0.5D + Math.sin(spin + 7D * Math.PI / 4D) * BEAM_RADIUS;
        double vBottom = -1.0D + frac;
        double vTop = height * textureScale * (0.5D / BEAM_RADIUS) + vBottom;

        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
        beamSide(buffer, aX, aZ, bX, bZ, height, vBottom, vTop, innerBottomG, innerTopG, innerA);
        beamSide(buffer, bX, bZ, cX, cZ, height, vBottom, vTop, innerBottomG, innerTopG, innerA);
        beamSide(buffer, cX, cZ, dX, dZ, height, vBottom, vTop, innerBottomG, innerTopG, innerA);
        beamSide(buffer, dX, dZ, aX, aZ, height, vBottom, vTop, innerBottomG, innerTopG, innerA);
        tessellator.draw();

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.depthMask(false);

        double gaX = 0.5D - GLOW_RADIUS;
        double gaZ = 0.5D - GLOW_RADIUS;
        double gbX = 0.5D + GLOW_RADIUS;
        double gbZ = 0.5D - GLOW_RADIUS;
        double gcX = 0.5D + GLOW_RADIUS;
        double gcZ = 0.5D + GLOW_RADIUS;
        double gdX = 0.5D - GLOW_RADIUS;
        double gdZ = 0.5D + GLOW_RADIUS;
        double gvBottom = -1.0D + frac;
        double gvTop = height * textureScale + gvBottom;

        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
        beamSide(buffer, gaX, gaZ, gbX, gbZ, height, gvBottom, gvTop, outerBottomG, outerTopG, outerA);
        beamSide(buffer, gbX, gbZ, gcX, gcZ, height, gvBottom, gvTop, outerBottomG, outerTopG, outerA);
        beamSide(buffer, gcX, gcZ, gdX, gdZ, height, gvBottom, gvTop, outerBottomG, outerTopG, outerA);
        beamSide(buffer, gdX, gdZ, gaX, gaZ, height, gvBottom, gvTop, outerBottomG, outerTopG, outerA);
        tessellator.draw();

        GlStateManager.popMatrix();
        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
        GlStateManager.depthMask(true);
        GlStateManager.enableCull();
    }

    private static void beamSide(BufferBuilder buffer, double x0, double z0, double x1, double z1, float height, double vBottom, double vTop, float gBottom, float gTop, float a) {
        buffer.pos(x0, height, z0).tex(1.0D, vTop).color(1F, gTop, 0F, a).endVertex();
        buffer.pos(x0, 0, z0).tex(1.0D, vBottom).color(1F, gBottom, 0F, a).endVertex();
        buffer.pos(x1, 0, z1).tex(0.0D, vBottom).color(1F, gBottom, 0F, a).endVertex();
        buffer.pos(x1, height, z1).tex(0.0D, vTop).color(1F, gTop, 0F, a).endVertex();
    }
}
