package mctmods.immersivetechnology.client.render.multiblock;

import com.immersiveconvergence.api.particles.ColoredBeamRenderer;

import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntitySolarMelterMaster;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.math.BlockPos;
import javax.annotation.Nonnull;

public class TileRenderSolarMelter extends TileEntitySpecialRenderer<TileEntitySolarMelterMaster> {
    @Override public void render(TileEntitySolarMelterMaster te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        if (!te.formed || te.isInvalid()) { return; }
        if (te.particlePos0 == null) { return; }
        if (te.heatLevel < TileEntitySolarMelterMaster.getWorkingHeatLevel() || te.reflectorStrength <= 0) { return; }
        BlockPos offset = te.particlePos0.subtract(te.getPos());
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        ColoredBeamRenderer.renderBeam(offset.getX(), offset.getY(), offset.getZ(), 16F, partialTicks, 1F, 1F, 0F, 1F, 1F, 0F, 0.2F);
        GlStateManager.popMatrix();
    }

    @Override public boolean isGlobalRenderer(@Nonnull TileEntitySolarMelterMaster te) { return true; }
}
