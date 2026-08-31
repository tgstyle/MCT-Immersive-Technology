package mctmods.immersivetechnology.client.render.multiblock.withanimation;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.client.ClientUtils;

import mctmods.immersivetechnology.common.ITContent;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntityGasTurbineMaster;

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
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;

public class TileRendererGasTurbine extends TileEntitySpecialRenderer<TileEntityGasTurbineMaster> {
    private static final int ROTOR_DISTANCE = 7;

    @Override public boolean isGlobalRenderer(@Nonnull TileEntityGasTurbineMaster te) { return true; }

    @Override public void render(TileEntityGasTurbineMaster te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        if (!te.formed || !te.getWorld().isBlockLoaded(te.getPos(), false)) { return; }
        final BlockRendererDispatcher blockRenderer = Minecraft.getMinecraft().getBlockRendererDispatcher();
        BlockPos blockPos = te.getPos();
        IBlockState state = getWorld().getBlockState(blockPos);
        if (state.getBlock() != ITContent.blockMetalMultiblock1) { return; }
        boolean validFacing = te.facing.getAxis() != EnumFacing.Axis.Y;
        EnumFacing rotAxis = validFacing ? te.facing : EnumFacing.NORTH;
        if (validFacing) { state = state.getActualState(getWorld(), blockPos); }
        state = state.withProperty(IEProperties.DYNAMICRENDER, true);
        IBakedModel model = blockRenderer.getBlockModelShapes().getModelForState(state);
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
        GlStateManager.rotate(te.getAnimation().getAnimationRotation() + (te.getAnimation().getAnimationMomentum() * partialTicks), rotAxis.getXOffset(), 0, rotAxis.getZOffset());
        worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
        worldRenderer.setTranslation(-.5 - blockPos.getX(), -.5 - blockPos.getY(), -.5 - blockPos.getZ());
        blockRenderer.getBlockModelRenderer().renderModel(te.getWorld(), model, state, blockPos.offset(rotAxis, ROTOR_DISTANCE), worldRenderer, false);
        worldRenderer.setTranslation(0.0D, 0.0D, 0.0D);
        tessellator.draw();
        GlStateManager.enableCull();
        RenderHelper.enableStandardItemLighting();
        GlStateManager.popMatrix();
    }
}
