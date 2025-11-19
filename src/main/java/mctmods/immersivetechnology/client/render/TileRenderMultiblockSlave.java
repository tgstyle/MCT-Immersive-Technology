package mctmods.immersivetechnology.client.render;

import mctmods.immersivetechnology.common.util.ITLogger;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

public class TileRenderMultiblockSlave extends TileEntitySpecialRenderer<TileEntity> {
    static final Set<BlockPos> renderedThisFrame = new HashSet<>();

    public static void clearRenderedThisFrame() { renderedThisFrame.clear(); }

    @Override
    public void render(@Nonnull TileEntity te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        try {
            Method masterMethod = te.getClass().getMethod("master");
            Object masterObj = masterMethod.invoke(te);
            if (!(masterObj instanceof TileEntity)) return;
            TileEntity master = (TileEntity) masterObj;
            BlockPos key = master.getPos();
            if (renderedThisFrame.contains(key)) return;
            renderedThisFrame.add(key);
            double dx = key.getX() - te.getPos().getX();
            double dy = key.getY() - te.getPos().getY();
            double dz = key.getZ() - te.getPos().getZ();
            GlStateManager.pushMatrix();
            GlStateManager.translate(x + dx, y + dy, z + dz);
            RenderHelper.disableStandardItemLighting();
            GlStateManager.blendFunc(770, 771);
            GlStateManager.enableBlend();
            GlStateManager.disableCull();
            if (Minecraft.isAmbientOcclusionEnabled()) GlStateManager.shadeModel(GL11.GL_SMOOTH);
            else GlStateManager.shadeModel(GL11.GL_FLAT);
            TileEntitySpecialRenderer<TileEntity> masterRenderer = TileEntityRendererDispatcher.instance.getRenderer(master);
            if (masterRenderer != null && !(masterRenderer instanceof TileRenderMultiblockSlave)) { masterRenderer.render(master, 0, 0, 0, partialTicks, destroyStage, alpha); }
            RenderHelper.enableStandardItemLighting();
            GlStateManager.popMatrix();
        } catch (Exception e) { ITLogger.error("Error in rendering multiblock slave", e); }
    }

    @Override
    public boolean isGlobalRenderer(@Nonnull TileEntity te) { return true; }
}
