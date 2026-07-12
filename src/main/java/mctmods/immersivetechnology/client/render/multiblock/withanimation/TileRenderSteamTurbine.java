package mctmods.immersivetechnology.client.render.multiblock.withanimation;

import blusunrize.immersiveengineering.api.IEProperties;

import mctmods.immersivetechnology.client.render.multiblock.TileRenderITMultiblockStatic;
import mctmods.immersivetechnology.common.ITContent;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntitySteamTurbineMaster;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartSteamTurbine;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import org.lwjgl.opengl.GL11;

public class TileRenderSteamTurbine extends TileRenderITMultiblockStatic<TileEntitySteamTurbineMaster> {
    @Override protected int getTotalBlocks() { return TileEntityITMultiblockPartSteamTurbine.instance.width * TileEntityITMultiblockPartSteamTurbine.instance.length * TileEntityITMultiblockPartSteamTurbine.instance.height; }

    @Override protected Block getMultiblockBlock() { return ITContent.blockMetalMultiblock; }

    @Override protected void renderDynamic(TileEntitySteamTurbineMaster te, float partialTicks) {
        final BlockRendererDispatcher blockRenderer = Minecraft.getMinecraft().getBlockRendererDispatcher();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        BlockPos masterPos = te.getPos();
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.5, 0.5, 0.5);
        float rotation = te.animation.getAnimationRotation() + te.animation.getAnimationMomentum() * partialTicks;
        boolean validFacing = te.facing.getAxis() != EnumFacing.Axis.Y;
        EnumFacing rotAxis = validFacing ? te.facing : EnumFacing.NORTH;
        GlStateManager.rotate(rotation, rotAxis.getXOffset(), 0, rotAxis.getZOffset());
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
        buffer.setTranslation(-0.5 - masterPos.getX(), -0.5 - masterPos.getY(), -0.5 - masterPos.getZ());
        IBlockState state = te.getWorld().getBlockState(masterPos);
        if (state.getBlock() == ITContent.blockMetalMultiblock) {
            if (validFacing) { state = state.getActualState(te.getWorld(), masterPos); }
            state = state.withProperty(IEProperties.DYNAMICRENDER, true);
            IBakedModel model = blockRenderer.getModelForState(state);
            blockRenderer.getBlockModelRenderer().renderModel(te.getWorld(), model, state, masterPos, buffer, false);
        }
        buffer.setTranslation(0, 0, 0);
        tessellator.draw();
        GlStateManager.popMatrix();
    }
}
