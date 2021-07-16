package mctmods.immersivetechnology.common.blocks.metal.multiblocks;

import blusunrize.immersiveengineering.api.MultiblockHandler;
import blusunrize.immersiveengineering.client.ClientUtils;
import mctmods.immersivetechnology.common.ITContent;
import mctmods.immersivetechnology.common.blocks.metal.tileentities.TileEntityElectrolyticCrucibleBatterySlave;
import mctmods.immersivetechnology.common.blocks.metal.types.BlockType_MetalMultiblock1;
import mctmods.immersivetechnology.common.util.multiblock.ITMultiblock;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MultiblockElectrolyticCrucibleBattery extends ITMultiblock<TileEntityElectrolyticCrucibleBatterySlave> implements MultiblockHandler.IMultiblock {

    public static MultiblockElectrolyticCrucibleBattery instance = new MultiblockElectrolyticCrucibleBattery();

    @SideOnly(Side.CLIENT)
    static ItemStack renderStack;

    public MultiblockElectrolyticCrucibleBattery() {
        super("multiblocks/electrolytic_crucible_battery.json",
                ITContent.blockMetalMultiblock1.getStateFromMeta(BlockType_MetalMultiblock1.ELECTROLYTIC_CRUCIBLE_BATTERY.getMeta()),
                ITContent.blockMetalMultiblock1.getStateFromMeta(BlockType_MetalMultiblock1.ELECTROLYTIC_CRUCIBLE_BATTERY_SLAVE.getMeta()));
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
        if(renderStack == null) renderStack = new ItemStack(ITContent.blockMetalMultiblock1, 1, BlockType_MetalMultiblock1.ELECTROLYTIC_CRUCIBLE_BATTERY.getMeta());
        GlStateManager.translate(2, 2.5, 2);
        GlStateManager.rotate(- 45, 0, 1, 0);
        GlStateManager.rotate(- 20, 1, 0, 0);
        GlStateManager.scale(7.14, 7.14, 7.14);
        GlStateManager.disableCull();
        ClientUtils.mc().getRenderItem().renderItem(renderStack, ItemCameraTransforms.TransformType.GUI);
        GlStateManager.enableCull();
    }
}
