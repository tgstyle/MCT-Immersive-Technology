package mctmods.immersivetechnology.client.render;

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
	int blue, green, red, alphaValue;
	int lightx, lighty;
	double minU, minV, maxU, maxV, diffU, diffV;

	@Override
	public void render(TileEntityBarrelOpen te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		final FluidTank tank = te.tank;
		final int amount = tank.getFluidAmount();
		final int capacity = tank.getCapacity();
		final FluidStack fluidStack = tank.getFluid();
		final Fluid fluid = (fluidStack != null) ? fluidStack.getFluid() : null;
		if(fluid != null) {
			final int c = fluid.getColor();
			this.blue = c & 0xFF;
			this.green = (c >> 8) & 0xFF;
			this.red = (c >> 16) & 0xFF;
			this.alphaValue = (c >> 24) & 0xFF;
			final TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks().getTextureExtry(fluid.getStill().toString());
			this.diffU = this.maxU - this.minU;
			this.diffV = this.maxV - this.minV;
			if(sprite != null) {
				final double multiplier = 0.25;
				this.minU = sprite.getMinU() + this.diffU * multiplier;
				this.maxU = sprite.getMaxU() - this.diffU * multiplier;
				this.minV = sprite.getMinV() + this.diffV * multiplier;
				this.maxV = sprite.getMaxV() - this.diffV * multiplier;
				final int i = getWorld().getCombinedLight(te.getPos(), fluid.getLuminosity());
				this.lightx = i >> 0x10 & 0xFFFF;
				this.lighty = i & 0xFFFF;
				final double yFilled = 0.8125 * (amount / (double) capacity);
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
				buffer.pos(x + startPos, y + yStartOffset + yFilled, z + startPos).tex(this.minU, this.minV).lightmap(this.lightx, this.lighty).color(this.red, this.green, this.blue, this.alphaValue).endVertex();
				buffer.pos(x + endPos, y + yStartOffset + yFilled, z + startPos).tex(this.maxU, this.minV).lightmap(this.lightx, this.lighty).color(this.red, this.green, this.blue, this.alphaValue).endVertex();
				buffer.pos(x + endPos, y + yStartOffset + yFilled, z + endPos).tex(this.maxU, this.maxV).lightmap(this.lightx, this.lighty).color(this.red, this.green, this.blue, this.alphaValue).endVertex();
				buffer.pos(x + startPos, y + yStartOffset + yFilled, z + endPos).tex(this.minU, this.maxV).lightmap(this.lightx, this.lighty).color(this.red, this.green, this.blue, this.alphaValue).endVertex();
				tess.draw();
				GlStateManager.disableBlend();
				GlStateManager.enableLighting();
			}
		}
	}

}