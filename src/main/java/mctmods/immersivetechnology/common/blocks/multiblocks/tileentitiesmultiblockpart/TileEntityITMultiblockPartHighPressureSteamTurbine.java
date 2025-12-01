package mctmods.immersivetechnology.common.blocks.multiblocks.tileentitiesmultiblockpart;

import blusunrize.immersiveengineering.api.MultiblockHandler;
import blusunrize.immersiveengineering.client.ClientUtils;
import mctmods.immersivetechnology.common.Config.ITConfig.Multiblocks;
import mctmods.immersivetechnology.common.ITContent;
import mctmods.immersivetechnology.common.blocks.multiblocks.tileentities.TileEntityHighPressureSteamTurbineSlave;
import mctmods.immersivetechnology.common.blocks.multiblocks.types.BlockType_MetalMultiblock1;
import mctmods.immersivetechnology.common.tileentities.TileEntityITMultiblockPart;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

public class TileEntityITMultiblockPartHighPressureSteamTurbine extends TileEntityITMultiblockPart<TileEntityHighPressureSteamTurbineSlave> implements MultiblockHandler.IMultiblock {
    public static TileEntityITMultiblockPartHighPressureSteamTurbine instance = new TileEntityITMultiblockPartHighPressureSteamTurbine();

    static String blockOreName = (Multiblocks.highPressureSteamTurbine.highPressureSteamTurbine_turbine_material) ? (OreDictionary.doesOreNameExist("blockTungsten") ? "blockTungsten" : "blockNickel") : "blockNickel";
    static String sheetmetalOreName = (Multiblocks.highPressureSteamTurbine.highPressureSteamTurbine_turbine_material) ? (OreDictionary.doesOreNameExist("blockSheetmetalTungsten") ? "blockSheetmetalTungsten" : "blockSheetmetalNickel") : "blockSheetmetalNickel";

    public TileEntityITMultiblockPartHighPressureSteamTurbine() {
        super("multiblocks/high_pressure_steam_turbine.json",
                ITContent.blockMetalMultiblock1.getStateFromMeta(BlockType_MetalMultiblock1.HIGH_PRESSURE_STEAM_TURBINE.getMeta()),
                ITContent.blockMetalMultiblock1.getStateFromMeta(BlockType_MetalMultiblock1.HIGH_PRESSURE_STEAM_TURBINE_SLAVE.getMeta()));
    }

    @Override
    public boolean overwriteBlockRender(ItemStack stack, int iterator) { return false; }

    @Override
    public float getManualScale() { return 8; }

    @Override
    public boolean canRenderFormedStructure() { return true; }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderFormedStructure() {
        if (renderStack == null) renderStack = new ItemStack(ITContent.blockMetalMultiblock1, 1, BlockType_MetalMultiblock1.HIGH_PRESSURE_STEAM_TURBINE.getMeta());
        GlStateManager.translate(0.3, 0.1, 0);
        GlStateManager.translate(2.4, 2, 3.2);
        GlStateManager.rotate(-45, 0, 1, 0);
        GlStateManager.rotate(-20, 1, 0, 0);
        GlStateManager.scale(8.7, 8.7, 8.7);
        GlStateManager.disableCull();
        ClientUtils.mc().getRenderItem().renderItem(renderStack, ItemCameraTransforms.TransformType.GUI);
        GlStateManager.enableCull();
    }

    @SideOnly(Side.CLIENT)
    static ItemStack renderStack;
}
