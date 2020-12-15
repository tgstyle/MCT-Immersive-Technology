package mctmods.immersivetechnology.common.blocks.metal.multiblocks;

import blusunrize.immersiveengineering.api.MultiblockHandler;
import blusunrize.immersiveengineering.client.ClientUtils;
import mctmods.immersivetechnology.common.ITContent;
import mctmods.immersivetechnology.common.blocks.metal.tileentities.TileEntityHeatExchangerSlave;
import mctmods.immersivetechnology.common.blocks.metal.types.BlockType_MetalMultiblock1;
import mctmods.immersivetechnology.common.util.multiblock.ITMultiblock;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MultiblockHeatExchanger extends ITMultiblock<TileEntityHeatExchangerSlave> implements MultiblockHandler.IMultiblock {

    public static MultiblockHeatExchanger instance = new MultiblockHeatExchanger();

    @SideOnly(Side.CLIENT)
    static ItemStack renderStack;

    public MultiblockHeatExchanger() {
        super("multiblocks/heat_exchanger.json",
                ITContent.blockMetalMultiblock1.getStateFromMeta(BlockType_MetalMultiblock1.HEAT_EXCHANGER.getMeta()),
                ITContent.blockMetalMultiblock1.getStateFromMeta(BlockType_MetalMultiblock1.HEAT_EXCHANGER_SLAVE.getMeta()));
    }

    @Override
    public boolean overwriteBlockRender(ItemStack stack, int iterator) {
        return false;
    }

    @Override
    public float getManualScale() {
        return 12;
    }

    @Override
    public boolean canRenderFormedStructure() {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderFormedStructure() {
        if(renderStack == null) renderStack = new ItemStack(ITContent.blockMetalMultiblock1, 1, BlockType_MetalMultiblock1.HEAT_EXCHANGER.getMeta());
        GlStateManager.translate(.1, 0, 0);
        GlStateManager.translate(0.8, 1.5, 3);
        GlStateManager.rotate(- 45, 0, 1, 0);
        GlStateManager.rotate(- 20, 1, 0, 0);
        GlStateManager.scale(5.88, 5.88, 5.88);
        GlStateManager.disableCull();
        ClientUtils.mc().getRenderItem().renderItem(renderStack, ItemCameraTransforms.TransformType.GUI);
        GlStateManager.enableCull();
    }
}
