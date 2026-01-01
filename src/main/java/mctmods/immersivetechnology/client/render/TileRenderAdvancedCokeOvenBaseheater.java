package mctmods.immersivetechnology.client.render;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.client.ClientUtils;

import mctmods.immersivetechnology.common.ITContent;
import mctmods.immersivetechnology.common.blocks.metal.tileentities.TileEntityAdvancedCokeOvenBaseheater;

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

public class TileRenderAdvancedCokeOvenBaseheater extends TileEntitySpecialRenderer<TileEntityAdvancedCokeOvenBaseheater> {

    @SuppressWarnings("deprecation")
    @Override public void render(TileEntityAdvancedCokeOvenBaseheater te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        if (te.dummy || !te.getWorld().isBlockLoaded(te.getPos(), false)) { return; }
        final BlockRendererDispatcher blockRenderer = Minecraft.getMinecraft().getBlockRendererDispatcher();
        BlockPos blockPos = te.getPos();
        IBlockState state = getWorld().getBlockState(blockPos);
        if (state.getBlock() != ITContent.blockMetalDevice) { return; }
        state = state.getBlock().getActualState(state, getWorld(), blockPos);

        IBlockState dynamicState = state.withProperty(IEProperties.DYNAMICRENDER, true);
        IBakedModel fanModel = blockRenderer.getModelForState(dynamicState);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder worldRenderer = tessellator.getBuffer();

        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5, y + 0.5, z + 0.5);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.blendFunc(770, 771);
        GlStateManager.enableBlend();
        GlStateManager.disableCull();
        if (Minecraft.isAmbientOcclusionEnabled()) { GlStateManager.shadeModel(7425); } else { GlStateManager.shadeModel(7424); }

        float rot = te.getFanRotation(partialTicks);
        GlStateManager.rotate(rot, te.facing.rotateY().getXOffset(), te.facing.rotateY().getYOffset(), te.facing.rotateY().getZOffset());

        ClientUtils.bindAtlas();
        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
        worldRenderer.setTranslation(-0.5 - blockPos.getX(), -0.5 - blockPos.getY(), -0.5 - blockPos.getZ());
        worldRenderer.color(255, 255, 255, 255);
        blockRenderer.getBlockModelRenderer().renderModel(te.getWorld(), fanModel, dynamicState, blockPos, worldRenderer, false);
        worldRenderer.setTranslation(0.0D, 0.0D, 0.0D);
        tessellator.draw();

        RenderHelper.enableStandardItemLighting();
        GlStateManager.popMatrix();
    }
}
