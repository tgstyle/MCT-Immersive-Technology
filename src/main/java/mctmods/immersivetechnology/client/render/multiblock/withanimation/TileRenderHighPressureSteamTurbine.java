package mctmods.immersivetechnology.client.render.multiblock.withanimation;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.client.ClientUtils;

import mctmods.immersivetechnology.client.render.ITTESRHelper;
import mctmods.immersivetechnology.common.Config.ITConfig;
import mctmods.immersivetechnology.common.ITContent;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntityHighPressureSteamTurbineMaster;

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

public class TileRenderHighPressureSteamTurbine extends TileEntitySpecialRenderer<TileEntityHighPressureSteamTurbineMaster> {
    @Override public boolean isGlobalRenderer(@Nonnull TileEntityHighPressureSteamTurbineMaster te) { return true; }

    @Override public void render(TileEntityHighPressureSteamTurbineMaster te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        if (!ITConfig.client.render.steam_turbine_renderer || !ITTESRHelper.inRenderRange(x, y, z)) { return; }
        if (!te.formed || te.isInvalid() || !te.getWorld().isBlockLoaded(te.getPos(), false)) { return; }
        final BlockRendererDispatcher blockRenderer = Minecraft.getMinecraft().getBlockRendererDispatcher();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        BlockPos masterPos = te.getPos();
        ClientUtils.bindAtlas();
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableCull();
        if (Minecraft.isAmbientOcclusionEnabled()) { GlStateManager.shadeModel(GL11.GL_SMOOTH); }
        else { GlStateManager.shadeModel(GL11.GL_FLAT); }
        GlStateManager.translate(0.5, 0.5, 0.5);
        float rotation = te.animation.getAnimationRotation() + te.animation.getAnimationMomentum() * partialTicks;
        boolean validFacing = te.facing.getAxis() != EnumFacing.Axis.Y;
        EnumFacing rotAxis = validFacing ? te.facing : EnumFacing.NORTH;
        GlStateManager.rotate(rotation, rotAxis.getXOffset(), 0, rotAxis.getZOffset());
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
        buffer.setTranslation(-0.5, -0.5, -0.5);
        IBlockState state = te.getWorld().getBlockState(masterPos);
        if (state.getBlock() == ITContent.blockMetalMultiblock1) {
            if (validFacing) { state = state.getActualState(te.getWorld(), masterPos); }
            state = state.withProperty(IEProperties.DYNAMICRENDER, true);
            IBakedModel model = blockRenderer.getModelForState(state);
            ITTESRHelper.renderQuads(model.getQuads(state, null, 0L), buffer, te.getWorld(), masterPos, false);
        }
        buffer.setTranslation(0, 0, 0);
        tessellator.draw();
        GlStateManager.enableCull();
        RenderHelper.enableStandardItemLighting();
        GlStateManager.popMatrix();
    }
}
