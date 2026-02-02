package mctmods.immersivetechnology.client.render.multiblock;

import blusunrize.immersiveengineering.client.ClientUtils;

import mctmods.immersivetechnology.common.ITContent;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntityCoolingTowerMaster;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartCoolingTower;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;

public class TileRenderCoolingTower extends TileEntitySpecialRenderer<TileEntityCoolingTowerMaster> {
    @Override public void render(TileEntityCoolingTowerMaster te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        if (!te.formed) { return; }
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        double distSq = te.getPos().distanceSq(player.posX, player.posY, player.posZ);
        if (distSq > 512 * 512) { return; }
        final BlockRendererDispatcher blockRenderer = Minecraft.getMinecraft().getBlockRendererDispatcher();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        ClientUtils.bindAtlas();
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.blendFunc(770, 771);
        GlStateManager.enableBlend();
        GlStateManager.disableCull();
        if (Minecraft.isAmbientOcclusionEnabled()) { GlStateManager.shadeModel(GL11.GL_SMOOTH); }
        else { GlStateManager.shadeModel(GL11.GL_FLAT); }
        buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
        BlockPos masterPos = te.getPos();
        buffer.setTranslation(-masterPos.getX(), -masterPos.getY(), -masterPos.getZ());
        int totalBlocks = TileEntityITMultiblockPartCoolingTower.instance.width * TileEntityITMultiblockPartCoolingTower.instance.length * TileEntityITMultiblockPartCoolingTower.instance.height;
        for (int i = 0; i < totalBlocks; i++) {
            BlockPos pos = te.getBlockPosForPos(i);
            IBlockState state = te.getWorld().getBlockState(pos);
            if (state.getBlock() != ITContent.blockMetalMultiblock) { continue; }
            state = state.getActualState(te.getWorld(), pos);
            IBakedModel model = blockRenderer.getModelForState(state);
            blockRenderer.getBlockModelRenderer().renderModel(te.getWorld(), model, state, pos, buffer, false, MathHelper.getCoordinateRandom(pos.getX(), pos.getY(), pos.getZ()));
        }
        buffer.setTranslation(0, 0, 0);
        tessellator.draw();
        RenderHelper.enableStandardItemLighting();
        GlStateManager.popMatrix();
    }

    @Override public boolean isGlobalRenderer(@Nonnull TileEntityCoolingTowerMaster te) { return true; }
}
