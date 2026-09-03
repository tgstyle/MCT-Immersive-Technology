package mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart;

import blusunrize.immersiveengineering.client.ClientUtils;

import mctmods.immersivetechnology.common.Config.ITConfig.Multiblocks;
import mctmods.immersivetechnology.common.ITContent;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntityHighPressureSteamTurbineSlave;
import mctmods.immersivetechnology.common.multiblocks.metal.types.BlockType_MetalMultiblock1;
import mctmods.immersivetechnology.common.multiblocks.ITShapes;
import mctmods.immersivetechnology.common.util.ITUtils;
import com.immersiveconvergence.api.multiblock.MachineTemplateMultiblock;

import com.immersiveconvergence.api.multiblock.BlockMatcher;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TileEntityITMultiblockPartHighPressureSteamTurbine extends MachineTemplateMultiblock<TileEntityHighPressureSteamTurbineSlave> {
    public static TileEntityITMultiblockPartHighPressureSteamTurbine instance = new TileEntityITMultiblockPartHighPressureSteamTurbine();
    private Map<IBlockState, IBlockState> turbineMaterial;
    private boolean turbineMaterialResolved;

    public TileEntityITMultiblockPartHighPressureSteamTurbine() { super("IT:HighPressureSteamTurbine", ITShapes.get("high_pressure_steam_turbine"), ITUtils.stateOf(ITContent.blockMetalMultiblock1, BlockType_MetalMultiblock1.HIGH_PRESSURE_STEAM_TURBINE), ITUtils.stateOf(ITContent.blockMetalMultiblock1, BlockType_MetalMultiblock1.HIGH_PRESSURE_STEAM_TURBINE_SLAVE)); }

    @Override protected IBlockState modifyTemplateState(IBlockState state) {
        if (!turbineMaterialResolved) { resolveTurbineMaterial(); }
        if (turbineMaterial == null) { return state; }
        return turbineMaterial.getOrDefault(state, state);
    }

    private void resolveTurbineMaterial() {
        turbineMaterialResolved = true;
        if (template == null || !Multiblocks.highPressureSteamTurbine.highPressureSteamTurbine_turbine_material) { return; }
        Map<IBlockState, IBlockState> substitutions = new HashMap<>();
        addSubstitutions(substitutions, "blockNickel", "blockTungsten");
        addSubstitutions(substitutions, "blockSheetmetalNickel", "blockSheetmetalTungsten");
        if (!substitutions.isEmpty()) { turbineMaterial = substitutions; }
    }

    private void addSubstitutions(Map<IBlockState, IBlockState> substitutions, String fromOreName, String toOreName) {
        List<ItemStack> targets = OreDictionary.getOres(toOreName, false);
        if (targets.isEmpty()) { return; }
        ItemStack target = targets.get(0);
        Block targetBlock = Block.getBlockFromItem(target.getItem());
        if (targetBlock == Blocks.AIR) { return; }
        int meta = target.getMetadata() == OreDictionary.WILDCARD_VALUE ? 0 : target.getMetadata();
        IBlockState targetState = ITUtils.stateOf(targetBlock, meta);
        for (int h = 0; h < template.height; h++) {
            for (int l = 0; l < template.length; l++) {
                for (int w = 0; w < template.width; w++) {
                    IBlockState state = template.getState(w, h, l);
                    if (state == null || substitutions.containsKey(state)) { continue; }
                    ItemStack stack = BlockMatcher.stackFromState(state);
                    if (!stack.isEmpty() && BlockMatcher.hasOreName(stack, fromOreName)) { substitutions.put(state, targetState); }
                }
            }
        }
    }

    @Override public boolean overwriteBlockRender(ItemStack stack, int iterator) { return false; }

    @Override public boolean canRenderFormedStructure() { return true; }

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
