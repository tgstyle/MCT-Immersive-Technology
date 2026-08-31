package mctmods.immersivetechnology.client.render.animation;

import mctmods.immersivetechnology.client.render.ITTESRHelper;
import mctmods.immersivetechnology.common.ITContent;
import mctmods.immersivetechnology.common.blocks.metal.tileentities.TileEntityRotorCreative;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.client.ClientUtils;
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

public class TileRenderRotorCreative extends TileEntitySpecialRenderer<TileEntityRotorCreative> {
    @Override public void render(TileEntityRotorCreative te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        if (!te.getWorld().isBlockLoaded(te.getPos(), false)) { return; }
        final BlockRendererDispatcher blockRenderer = Minecraft.getMinecraft().getBlockRendererDispatcher();
        BlockPos blockPos = te.getPos();
        IBlockState state = getWorld().getBlockState(blockPos);
        if (state.getBlock() != ITContent.blockMetalDevice) { return; }
        state = state.getBlock().getActualState(state, getWorld(), blockPos);
        IBlockState dynamicState = state.withProperty(IEProperties.DYNAMICRENDER, true);
        IBakedModel model = blockRenderer.getModelForState(dynamicState);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5, y + 0.5, z + 0.5);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableCull();
        if (Minecraft.isAmbientOcclusionEnabled()) { GlStateManager.shadeModel(GL11.GL_SMOOTH); }
        else { GlStateManager.shadeModel(GL11.GL_FLAT); }
        float rotation = te.getAnimation().getAnimationRotation() + te.getAnimation().getAnimationMomentum() * partialTicks;
        GlStateManager.rotate(rotation, te.facing.getXOffset(), 0, te.facing.getZOffset());
        ClientUtils.bindAtlas();
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
        buffer.setTranslation(-0.5 - blockPos.getX(), -0.5 - blockPos.getY(), -0.5 - blockPos.getZ());
        ITTESRHelper.renderModel(blockRenderer.getBlockModelRenderer(), te.getWorld(), model, dynamicState, blockPos, buffer);
        buffer.setTranslation(0, 0, 0);
        tessellator.draw();
        GlStateManager.enableCull();
        RenderHelper.enableStandardItemLighting();
        GlStateManager.popMatrix();
    }
}
