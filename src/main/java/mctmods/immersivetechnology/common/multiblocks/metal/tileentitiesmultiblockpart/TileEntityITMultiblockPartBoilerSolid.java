package mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart;

import blusunrize.immersiveengineering.client.ClientUtils;

import mctmods.immersivetechnology.common.ITContent;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntityBoilerSolidSlave;
import mctmods.immersivetechnology.common.multiblocks.metal.types.BlockType_MetalMultiblock2;
import com.immersiveconvergence.api.capability.IHeatConsumer;
import com.immersiveconvergence.api.multiblock.FormationCandidate;
import com.immersiveconvergence.api.multiblock.MachineTemplateMultiblock;
import mctmods.immersivetechnology.common.multiblocks.ITShapes;
import mctmods.immersivetechnology.common.util.ITUtils;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import java.util.List;

import javax.annotation.Nullable;

public class TileEntityITMultiblockPartBoilerSolid extends MachineTemplateMultiblock<TileEntityBoilerSolidSlave> {
    public static TileEntityITMultiblockPartBoilerSolid instance = new TileEntityITMultiblockPartBoilerSolid();

    @SideOnly(Side.CLIENT)
    static ItemStack renderStack;

    public TileEntityITMultiblockPartBoilerSolid() { super("IT:BoilerSolid", ITShapes.get("boiler_solid"), ITUtils.stateOf(ITContent.blockMetalMultiblock2, BlockType_MetalMultiblock2.BOILER_SOLID), ITUtils.stateOf(ITContent.blockMetalMultiblock2, BlockType_MetalMultiblock2.BOILER_SOLID_SLAVE)); }

    @Override public boolean overwriteBlockRender(ItemStack stack, int iterator) { return false; }

    @Override public boolean canRenderFormedStructure() { return true; }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderFormedStructure() {
        if (renderStack == null) renderStack = new ItemStack(ITContent.blockMetalMultiblock2, 1, BlockType_MetalMultiblock2.BOILER_SOLID.getMeta());
        GlStateManager.translate(.1, 0, 0);
        GlStateManager.translate(0.4, 1.5, 2);
        GlStateManager.rotate(-45, 0, 1, 0);
        GlStateManager.rotate(-20, 1, 0, 0);
        GlStateManager.scale(5.88, 5.88, 5.88);
        GlStateManager.disableCull();
        ClientUtils.mc().getRenderItem().renderItem(renderStack, ItemCameraTransforms.TransformType.GUI);
        GlStateManager.enableCull();
    }

    @Override @Nullable protected FormationCandidate preferredCandidate(World world, List<FormationCandidate> candidates, @Nullable EntityPlayer player) {
        return FormationCandidate.preferFacing(world, candidates, pointsOfInterest, "heat_output0", (te, side) -> te instanceof IHeatConsumer && ((IHeatConsumer) te).acceptsHeatFrom(side));
    }
}
