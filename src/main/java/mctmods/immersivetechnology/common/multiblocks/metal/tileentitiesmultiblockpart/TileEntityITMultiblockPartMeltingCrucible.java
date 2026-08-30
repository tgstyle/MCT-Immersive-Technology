package mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart;

import blusunrize.immersiveengineering.client.ClientUtils;

import mctmods.immersivetechnology.common.ITContent;
import mctmods.immersivetechnology.common.multiblocks.metal.shapes.MeltingCrucibleShape;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntityMeltingCrucibleSlave;
import mctmods.immersivetechnology.common.multiblocks.metal.types.BlockType_MetalMultiblock1;
import mctmods.immersivetechnology.common.shared.tileentities.TileEntityITMultiblockPart;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityITMultiblockPartMeltingCrucible extends TileEntityITMultiblockPart<TileEntityMeltingCrucibleSlave> {
    public static TileEntityITMultiblockPartMeltingCrucible instance = new TileEntityITMultiblockPartMeltingCrucible();

    @SideOnly(Side.CLIENT)
    static ItemStack renderStack;

    public TileEntityITMultiblockPartMeltingCrucible() { super("IT:meltingCrucible", MeltingCrucibleShape.SHAPE, ITContent.blockMetalMultiblock1.getStateFromMeta(BlockType_MetalMultiblock1.MELTING_CRUCIBLE.getMeta()), ITContent.blockMetalMultiblock1.getStateFromMeta(BlockType_MetalMultiblock1.MELTING_CRUCIBLE_SLAVE.getMeta())); }

    @Override public boolean overwriteBlockRender(ItemStack stack, int iterator) { return false; }

    @Override public boolean canRenderFormedStructure() { return true; }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderFormedStructure() {
        if (renderStack == null) renderStack = new ItemStack(ITContent.blockMetalMultiblock1, 1, BlockType_MetalMultiblock1.MELTING_CRUCIBLE.getMeta());
        GlStateManager.translate(1.5, 1.5, 1.5);
        GlStateManager.rotate(-45, 0, 1, 0);
        GlStateManager.rotate(-20, 1, 0, 0);
        GlStateManager.scale(3.5, 3.5, 3.5);
        GlStateManager.disableCull();
        ClientUtils.mc().getRenderItem().renderItem(renderStack, ItemCameraTransforms.TransformType.GUI);
        GlStateManager.enableCull();
    }
}
