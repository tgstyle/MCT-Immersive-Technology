package mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart;

import blusunrize.immersiveengineering.client.ClientUtils;
import mctmods.immersivetechnology.common.ITContent;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntityGasTurbineSlave;
import mctmods.immersivetechnology.common.multiblocks.metal.types.BlockType_MetalMultiblock1;
import mctmods.immersivetechnology.common.shared.tileentities.TileEntityITMultiblockPart;
import mctmods.immersivetechnology.common.multiblocks.metal.shapes.GasTurbineShape;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityITMultiblockPartGasTurbine extends TileEntityITMultiblockPart<TileEntityGasTurbineSlave> {

    public static TileEntityITMultiblockPartGasTurbine instance = new TileEntityITMultiblockPartGasTurbine();

    @SideOnly(Side.CLIENT)
    static ItemStack renderStack;

    public TileEntityITMultiblockPartGasTurbine() { super("IT:GasTurbine", GasTurbineShape.SHAPE, ITContent.blockMetalMultiblock1.getStateFromMeta(BlockType_MetalMultiblock1.GAS_TURBINE.getMeta()), ITContent.blockMetalMultiblock1.getStateFromMeta(BlockType_MetalMultiblock1.GAS_TURBINE_SLAVE.getMeta())); }

    @Override public boolean overwriteBlockRender(ItemStack stack, int iterator) { return false; }

    @Override public boolean canRenderFormedStructure() { return true; }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderFormedStructure() {
        if (renderStack == null) renderStack = new ItemStack(ITContent.blockMetalMultiblock1, 1, BlockType_MetalMultiblock1.GAS_TURBINE.getMeta());
        GlStateManager.translate(.3, .1, 0);
        GlStateManager.translate(2, 2, 2.5);
        GlStateManager.rotate(- 45, 0, 1, 0);
        GlStateManager.rotate(- 20, 1, 0, 0);
        GlStateManager.scale(6.25, 6.25, 6.25);

        GlStateManager.disableCull();
        ClientUtils.mc().getRenderItem().renderItem(renderStack, ItemCameraTransforms.TransformType.GUI);
        GlStateManager.enableCull();
    }
}
