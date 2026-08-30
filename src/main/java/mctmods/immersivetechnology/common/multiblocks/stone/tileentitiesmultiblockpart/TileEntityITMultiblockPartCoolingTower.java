package mctmods.immersivetechnology.common.multiblocks.stone.tileentitiesmultiblockpart;

import blusunrize.immersiveengineering.client.ClientUtils;

import mctmods.immersivetechnology.common.ITContent;
import mctmods.immersivetechnology.common.multiblocks.stone.tileentities.TileEntityCoolingTowerSlave;
import mctmods.immersivetechnology.common.multiblocks.stone.types.BlockType_StoneMultiblock;
import mctmods.immersivetechnology.common.shared.tileentities.TileEntityITMultiblockPart;
import mctmods.immersivetechnology.common.multiblocks.stone.shapes.CoolingTowerShape;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityITMultiblockPartCoolingTower extends TileEntityITMultiblockPart<TileEntityCoolingTowerSlave> {
    public static TileEntityITMultiblockPartCoolingTower instance = new TileEntityITMultiblockPartCoolingTower();

    @SideOnly(Side.CLIENT)
    static ItemStack renderStack;

    public TileEntityITMultiblockPartCoolingTower() { super("IT:CoolingTower", CoolingTowerShape.SHAPE, ITContent.blockStoneMultiblock.getStateFromMeta(BlockType_StoneMultiblock.COOLING_TOWER.getMeta()), ITContent.blockStoneMultiblock.getStateFromMeta(BlockType_StoneMultiblock.COOLING_TOWER_SLAVE.getMeta())); }

    @Override public boolean overwriteBlockRender(ItemStack stack, int iterator) { return false; }

    @Override public boolean canRenderFormedStructure() { return true; }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderFormedStructure() {
        if (renderStack == null) renderStack = new ItemStack(ITContent.blockStoneMultiblock, 1, BlockType_StoneMultiblock.COOLING_TOWER.getMeta());
        GlStateManager.translate(.25, .4, .25);
        GlStateManager.translate(1.5, 6, 7);
        GlStateManager.rotate(- 45, 0, 1, 0);
        GlStateManager.rotate(- 20, 1, 0, 0);
        GlStateManager.scale(12.5, 12.5, 12.5);
        GlStateManager.disableCull();
        ClientUtils.mc().getRenderItem().renderItem(renderStack, ItemCameraTransforms.TransformType.GUI);
        GlStateManager.enableCull();
    }
}
