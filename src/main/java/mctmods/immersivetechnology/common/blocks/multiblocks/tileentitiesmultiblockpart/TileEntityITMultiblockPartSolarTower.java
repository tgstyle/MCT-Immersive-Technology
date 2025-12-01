package mctmods.immersivetechnology.common.blocks.multiblocks.tileentitiesmultiblockpart;

import blusunrize.immersiveengineering.api.MultiblockHandler;
import blusunrize.immersiveengineering.client.ClientUtils;
import mctmods.immersivetechnology.common.ITContent;
import mctmods.immersivetechnology.common.blocks.multiblocks.tileentities.TileEntitySolarTowerSlave;
import mctmods.immersivetechnology.common.blocks.multiblocks.types.BlockType_MetalMultiblock;
import mctmods.immersivetechnology.common.tileentities.TileEntityITMultiblockPart;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityITMultiblockPartSolarTower extends TileEntityITMultiblockPart<TileEntitySolarTowerSlave> implements MultiblockHandler.IMultiblock {
    public static TileEntityITMultiblockPartSolarTower instance = new TileEntityITMultiblockPartSolarTower();

    @SideOnly(Side.CLIENT)
    static ItemStack renderStack;

    public TileEntityITMultiblockPartSolarTower() {
        super("multiblocks/solar_tower.json",
                ITContent.blockMetalMultiblock.getStateFromMeta(BlockType_MetalMultiblock.SOLAR_TOWER.getMeta()),
                ITContent.blockMetalMultiblock.getStateFromMeta(BlockType_MetalMultiblock.SOLAR_TOWER_SLAVE.getMeta()));
    }

    @Override
    public boolean overwriteBlockRender(ItemStack stack, int iterator) { return false; }

    @Override
    public float getManualScale() { return 4; }

    @Override
    public boolean canRenderFormedStructure() { return true; }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderFormedStructure() {
        if (renderStack == null) renderStack = new ItemStack(ITContent.blockMetalMultiblock, 1, BlockType_MetalMultiblock.SOLAR_TOWER.getMeta());
        GlStateManager.translate(0.1, 0.25, 0.125);
        GlStateManager.translate(1, 3.5, 2);
        GlStateManager.rotate(-45, 0, 1, 0);
        GlStateManager.rotate(-20, 1, 0, 0);
        GlStateManager.scale(8, 8, 8);
        ClientUtils.mc().getRenderItem().renderItem(renderStack, ItemCameraTransforms.TransformType.GUI);
    }
}
