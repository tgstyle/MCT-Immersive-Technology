package mctmods.immersivetechnology.common.blocks.multiblocks.tileentitiesmultiblockpart;

import blusunrize.immersiveengineering.api.MultiblockHandler;
import blusunrize.immersiveengineering.client.ClientUtils;
import mctmods.immersivetechnology.common.ITContent;
import mctmods.immersivetechnology.common.blocks.multiblocks.tileentities.TileEntityCoolingTowerSlave;
import mctmods.immersivetechnology.common.blocks.multiblocks.types.BlockType_MetalMultiblock;
import mctmods.immersivetechnology.common.tileentities.TileEntityITMultiblockPart;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityITMultiblockPartCoolingTower extends TileEntityITMultiblockPart<TileEntityCoolingTowerSlave> implements MultiblockHandler.IMultiblock {
    public static TileEntityITMultiblockPartCoolingTower instance = new TileEntityITMultiblockPartCoolingTower();

    @SideOnly(Side.CLIENT)
    static ItemStack renderStack;

    public TileEntityITMultiblockPartCoolingTower() {
        super("multiblocks/cooling_tower.json",
                ITContent.blockMetalMultiblock.getStateFromMeta(BlockType_MetalMultiblock.COOLING_TOWER.getMeta()),
                ITContent.blockMetalMultiblock.getStateFromMeta(BlockType_MetalMultiblock.COOLING_TOWER_SLAVE.getMeta()));
    }

    @Override
    public boolean overwriteBlockRender(ItemStack stack, int iterator) {
        return false;
    }

    @Override
    public float getManualScale() {
        return 6;
    }

    @Override
    public boolean canRenderFormedStructure() {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderFormedStructure() {
        if (renderStack == null) renderStack = new ItemStack(ITContent.blockMetalMultiblock, 1, BlockType_MetalMultiblock.COOLING_TOWER.getMeta());
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
