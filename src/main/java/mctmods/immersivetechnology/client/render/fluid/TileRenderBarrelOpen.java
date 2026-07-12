package mctmods.immersivetechnology.client.render.fluid;

import mctmods.immersivetechnology.common.blocks.metal.tileentities.TileEntityBarrelOpen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

import org.lwjgl.opengl.GL11;

public class TileRenderBarrelOpen extends TileEntitySpecialRenderer<TileEntityBarrelOpen> {
	@Override public void render(TileEntityBarrelOpen te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		final FluidTank tank = te.tank;
		final int amount = tank.getFluidAmount();
		final FluidStack fluidStack = tank.getFluid();
		if (fluidStack == null || amount <= 0) { return; }
		final Fluid fluid = fluidStack.getFluid();
		if (fluid == null) { return; }
		final TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(fluid.getStill().toString());
		final int c = fluid.getColor();
		final int blue = c & 0xFF;
		final int green = (c >> 8) & 0xFF;
		final int red = (c >> 16) & 0xFF;
		final int alphaValue = (c >> 24) & 0xFF;
		final double multiplier = 0.25;
		final double diffU = sprite.getMaxU() - sprite.getMinU();
		final double diffV = sprite.getMaxV() - sprite.getMinV();
		final double minU = sprite.getMinU() + diffU * multiplier;
		final double maxU = sprite.getMaxU() - diffU * multiplier;
		final double minV = sprite.getMinV() + diffV * multiplier;
		final double maxV = sprite.getMaxV() - diffV * multiplier;
		final int i = getWorld().getCombinedLight(te.getPos(), fluid.getLuminosity());
		final int lightx = i >> 0x10 & 0xFFFF;
		final int lighty = i & 0xFFFF;
		final double yFilled = 0.8125 * (amount / (double) tank.getCapacity());
		final double startPos = 0.0625;
		final double endPos = 1 - startPos;
		final double yStartOffset = 0.125;
		GlStateManager.disableCull();
		GlStateManager.disableLighting();
		GlStateManager.enableBlend();
		GlStateManager.enableAlpha();
		GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
		Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		final Tessellator tess = Tessellator.getInstance();
		final BufferBuilder buffer = tess.getBuffer();
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_LMAP_COLOR);
		buffer.pos(x + startPos, y + yStartOffset + yFilled, z + startPos).tex(minU, minV).lightmap(lightx, lighty).color(red, green, blue, alphaValue).endVertex();
		buffer.pos(x + endPos, y + yStartOffset + yFilled, z + startPos).tex(maxU, minV).lightmap(lightx, lighty).color(red, green, blue, alphaValue).endVertex();
		buffer.pos(x + endPos, y + yStartOffset + yFilled, z + endPos).tex(maxU, maxV).lightmap(lightx, lighty).color(red, green, blue, alphaValue).endVertex();
		buffer.pos(x + startPos, y + yStartOffset + yFilled, z + endPos).tex(minU, maxV).lightmap(lightx, lighty).color(red, green, blue, alphaValue).endVertex();
		tess.draw();
		GlStateManager.disableBlend();
		GlStateManager.enableLighting();
		GlStateManager.enableCull();
	}
}
