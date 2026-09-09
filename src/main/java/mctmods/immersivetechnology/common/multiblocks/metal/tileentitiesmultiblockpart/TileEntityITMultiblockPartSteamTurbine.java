package mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart;


import mctmods.immersivetechnology.common.ITContent;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntitySteamTurbineSlave;
import mctmods.immersivetechnology.common.multiblocks.metal.types.BlockType_MetalMultiblock;
import com.immersiveconvergence.api.client.ICClientUtils;
import com.immersiveconvergence.api.util.ICUtils;
import com.immersiveconvergence.api.multiblock.MachineTemplateMultiblock;
import mctmods.immersivetechnology.common.multiblocks.ITShapes;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityITMultiblockPartSteamTurbine extends MachineTemplateMultiblock<TileEntitySteamTurbineSlave> {
    public static TileEntityITMultiblockPartSteamTurbine instance = new TileEntityITMultiblockPartSteamTurbine();

    @SideOnly(Side.CLIENT)
    static ItemStack renderStack;

    public TileEntityITMultiblockPartSteamTurbine() { super("IT:SteamTurbine", ITShapes.get("steam_turbine"), ICUtils.stateOf(ITContent.blockMetalMultiblock, BlockType_MetalMultiblock.STEAM_TURBINE), ICUtils.stateOf(ITContent.blockMetalMultiblock, BlockType_MetalMultiblock.STEAM_TURBINE_SLAVE)); }

    @Override public boolean overwriteBlockRender(ItemStack stack, int iterator) { return false; }

    @Override public boolean canRenderFormedStructure() { return true; }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderFormedStructure() {
        if (renderStack == null) renderStack = new ItemStack(ITContent.blockMetalMultiblock, 1, BlockType_MetalMultiblock.STEAM_TURBINE.getMeta());
        GlStateManager.translate(0.3, 0.1, 0);
        GlStateManager.translate(2.4, 2, 3.2);
        GlStateManager.rotate(-45, 0, 1, 0);
        GlStateManager.rotate(-20, 1, 0, 0);
        GlStateManager.scale(8.7, 8.7, 8.7);
        GlStateManager.disableCull();
        ICClientUtils.mc().getRenderItem().renderItem(renderStack, ItemCameraTransforms.TransformType.GUI);
        GlStateManager.enableCull();
    }
}
