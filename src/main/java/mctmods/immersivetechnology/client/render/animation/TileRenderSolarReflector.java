package mctmods.immersivetechnology.client.render.animation;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.client.ClientUtils;

import mctmods.immersivetechnology.client.render.ITTESRHelper;
import mctmods.immersivetechnology.common.Config.ITConfig;
import mctmods.immersivetechnology.common.ITContent;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntitySolarReflectorMaster;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;

import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;

public class TileRenderSolarReflector extends TileEntitySpecialRenderer<TileEntitySolarReflectorMaster> {
	@SuppressWarnings("deprecation")
	@Override public void render(@Nonnull TileEntitySolarReflectorMaster te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		if (!ITConfig.Client.render.solar_reflector_renderer) { return; }
		if (!te.formed || te.isDummy() || !te.getWorld().isBlockLoaded(te.getPos(), false)) { return; }
		final BlockRendererDispatcher blockRenderer = Minecraft.getMinecraft().getBlockRendererDispatcher();
		BlockPos blockPos = te.getPos();
		IBlockState state = getWorld().getBlockState(blockPos);
		if (state.getBlock() != ITContent.blockMetalMultiblock) { return; }
		state = state.getBlock().getActualState(state, getWorld(), blockPos);
		state = state.withProperty(IEProperties.DYNAMICRENDER, true);
		IBlockState state1 = state.withProperty(IEProperties.DYNAMICRENDER, false).withProperty(IEProperties.BOOLEANS[0], true);
		IBakedModel supportModel = blockRenderer.getBlockModelShapes().getModelForState(state);
		IBakedModel mirrorModel = blockRenderer.getBlockModelShapes().getModelForState(state1);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder worldRenderer = tessellator.getBuffer();
		ClientUtils.bindAtlas();
		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);
		GlStateManager.translate(.5, .5, .5);
		RenderHelper.disableStandardItemLighting();
		GlStateManager.disableCull();
		if (Minecraft.isAmbientOcclusionEnabled()) { GlStateManager.shadeModel(GL11.GL_SMOOTH); }
		else { GlStateManager.shadeModel(GL11.GL_FLAT); }

		GlStateManager.rotate(te.getAnimationRotations()[0], 0, 1, 0);

		worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		worldRenderer.setTranslation(-.5, -.5, -.5);
		ITTESRHelper.renderQuads(supportModel.getQuads(state, null, 0L), worldRenderer, te.getWorld(), blockPos, false);
		worldRenderer.setTranslation(0.0D, 0.0D, 0.0D);
		tessellator.draw();

		GlStateManager.rotate(te.getAnimationRotations()[1], te.getFacing().getZOffset(), 0, te.getFacing().getXOffset());

		worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		worldRenderer.setTranslation(-.5, -.5, -.5);
		ITTESRHelper.renderQuads(mirrorModel.getQuads(state1, null, 0L), worldRenderer, te.getWorld(), blockPos, true);
		worldRenderer.setTranslation(0.0D, 0.0D, 0.0D);
		tessellator.draw();

		GlStateManager.enableCull();
		RenderHelper.enableStandardItemLighting();
		GlStateManager.popMatrix();
	}
}
