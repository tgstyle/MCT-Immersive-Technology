package mctmods.immersivetechnology.client.render.multiblock;

import mctmods.immersivetechnology.api.particles.ColoredBeamRenderer;
import mctmods.immersivetechnology.common.ITContent;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntitySolarMelterMaster;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart.TileEntityITMultiblockPartSolarMelter;

import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;

public class TileRenderSolarMelter extends TileRenderITMultiblockStatic<TileEntitySolarMelterMaster> {
    @Override protected int getTotalBlocks() { return TileEntityITMultiblockPartSolarMelter.instance.width * TileEntityITMultiblockPartSolarMelter.instance.length * TileEntityITMultiblockPartSolarMelter.instance.height; }

    @Override protected Block getMultiblockBlock() { return ITContent.blockMetalMultiblock1; }

    @Override protected void renderDynamic(TileEntitySolarMelterMaster te, float partialTicks) {
        if (te.particlePos0 == null) return;
        if (te.heatLevel < TileEntitySolarMelterMaster.getWorkingHeatLevel() || te.reflectorStrength <= 0) return;
        BlockPos offset = te.particlePos0.subtract(te.getPos());
        ColoredBeamRenderer.renderBeam(offset.getX(), offset.getY(), offset.getZ(), 16F, partialTicks, 1F,
                1F, 0F, 1F, 1F, 0F, 0.2F);
    }
}
