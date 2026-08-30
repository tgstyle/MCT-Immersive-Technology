package mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart;

import blusunrize.immersiveengineering.client.ClientUtils;
import mctmods.immersivetechnology.common.ITContent;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntitySteelSheetmetalTankSlave;
import mctmods.immersivetechnology.common.multiblocks.metal.types.BlockType_MetalMultiblock;
import mctmods.immersivetechnology.common.shared.tileentities.TileEntityITMultiblockPart;
import mctmods.immersivetechnology.common.multiblocks.metal.shapes.SteelSheetmetalTankShape;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityITMultiblockPartSteelSheetmetalTank extends TileEntityITMultiblockPart<TileEntitySteelSheetmetalTankSlave> {

    public static TileEntityITMultiblockPartSteelSheetmetalTank instance = new TileEntityITMultiblockPartSteelSheetmetalTank();

    static ItemStack renderStack = ItemStack.EMPTY;

    public TileEntityITMultiblockPartSteelSheetmetalTank() { super("IT:SteelSheetmetalTank", SteelSheetmetalTankShape.SHAPE, ITContent.blockMetalMultiblock.getStateFromMeta(BlockType_MetalMultiblock.STEEL_TANK.getMeta()), ITContent.blockMetalMultiblock.getStateFromMeta(BlockType_MetalMultiblock.STEEL_TANK_SLAVE.getMeta())); }

    @Override public boolean overwriteBlockRender(ItemStack stack, int iterator) { return false; }

    @Override public boolean canRenderFormedStructure() { return true; }

    @SideOnly(Side.CLIENT)
    @Override
    public void renderFormedStructure() {
        if (renderStack.isEmpty()) renderStack = new ItemStack(ITContent.blockMetalMultiblock, 1, BlockType_MetalMultiblock.STEEL_TANK.getMeta());
        GlStateManager.translate(1.875, 1.75, 1.125);
        GlStateManager.rotate(-45, 0, 1, 0);
        GlStateManager.rotate(-20, 1, 0, 0);
        GlStateManager.scale(5.5, 5.5, 5.5);
        ClientUtils.mc().getRenderItem().renderItem(renderStack, ItemCameraTransforms.TransformType.GUI);
    }
}
