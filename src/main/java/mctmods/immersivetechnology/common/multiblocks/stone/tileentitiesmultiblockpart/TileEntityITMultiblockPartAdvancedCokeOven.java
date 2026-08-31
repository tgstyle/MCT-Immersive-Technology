package mctmods.immersivetechnology.common.multiblocks.stone.tileentitiesmultiblockpart;

import blusunrize.immersiveengineering.client.ClientUtils;

import mctmods.immersivetechnology.common.ITContent;
import mctmods.immersivetechnology.common.multiblocks.stone.shapes.AdvancedCokeOvenShape;
import mctmods.immersivetechnology.common.multiblocks.stone.tileentities.TileEntityAdvancedCokeOvenSlave;
import mctmods.immersivetechnology.common.multiblocks.stone.types.BlockType_StoneMultiblock;
import com.immersiveconvergence.api.multiblock.MachineTemplateMultiblock;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityITMultiblockPartAdvancedCokeOven extends MachineTemplateMultiblock<TileEntityAdvancedCokeOvenSlave> {
    public static TileEntityITMultiblockPartAdvancedCokeOven instance = new TileEntityITMultiblockPartAdvancedCokeOven();

    @SideOnly(Side.CLIENT)
    static ItemStack renderStack;

    public TileEntityITMultiblockPartAdvancedCokeOven() { super("IT:AdvancedCokeOven", AdvancedCokeOvenShape.SHAPE, ITContent.blockStoneMultiblock.getStateFromMeta(BlockType_StoneMultiblock.ADVANCED_COKE_OVEN.getMeta()), ITContent.blockStoneMultiblock.getStateFromMeta(BlockType_StoneMultiblock.ADVANCED_COKE_OVEN_SLAVE.getMeta())); }

    @Override public boolean overwriteBlockRender(ItemStack stack, int iterator) { return false; }

    @Override public boolean canRenderFormedStructure() { return true; }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderFormedStructure() {
        if (renderStack == null) renderStack = new ItemStack(ITContent.blockStoneMultiblock, 1, BlockType_StoneMultiblock.ADVANCED_COKE_OVEN.getMeta());
        GlStateManager.translate(.5, 1.5, 1.5);
        GlStateManager.rotate(-45, 0, 1, 0);
        GlStateManager.rotate(-20, 1, 0, 0);
        GlStateManager.scale(4, 4, 4);
        GlStateManager.disableCull();
        ClientUtils.mc().getRenderItem().renderItem(renderStack, ItemCameraTransforms.TransformType.GUI);
        GlStateManager.enableCull();
    }
}
