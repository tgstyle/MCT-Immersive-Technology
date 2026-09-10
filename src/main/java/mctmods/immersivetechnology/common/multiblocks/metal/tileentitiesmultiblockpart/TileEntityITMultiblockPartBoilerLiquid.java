package mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart;

import mctmods.immersivetechnology.common.ITContent;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntityBoilerLiquidSlave;
import mctmods.immersivetechnology.common.multiblocks.metal.types.BlockType_MetalMultiblock1;
import com.immersiveconvergence.api.capability.IHeatConsumer;
import com.immersiveconvergence.api.util.ICUtils;
import com.immersiveconvergence.api.multiblock.FormationCandidate;
import com.immersiveconvergence.api.multiblock.MachineTemplateMultiblock;
import mctmods.immersivetechnology.common.multiblocks.ITShapes;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import java.util.List;

import javax.annotation.Nullable;

public class TileEntityITMultiblockPartBoilerLiquid extends MachineTemplateMultiblock<TileEntityBoilerLiquidSlave> {
    public static TileEntityITMultiblockPartBoilerLiquid instance = new TileEntityITMultiblockPartBoilerLiquid();

    public TileEntityITMultiblockPartBoilerLiquid() { super("IT:BoilerLiquid", ITShapes.get("boiler_liquid"), ICUtils.stateOf(ITContent.blockMetalMultiblock1, BlockType_MetalMultiblock1.BOILER_LIQUID), ICUtils.stateOf(ITContent.blockMetalMultiblock1, BlockType_MetalMultiblock1.BOILER_LIQUID_SLAVE), 0.5, 1.5, 2, 5.88); }

    @Override @Nullable protected FormationCandidate preferredCandidate(World world, List<FormationCandidate> candidates, @Nullable EntityPlayer player) {
        return FormationCandidate.preferFacing(world, candidates, pointsOfInterest, "heat_output0", (te, side) -> te instanceof IHeatConsumer && ((IHeatConsumer) te).acceptsHeatFrom(side));
    }
}
