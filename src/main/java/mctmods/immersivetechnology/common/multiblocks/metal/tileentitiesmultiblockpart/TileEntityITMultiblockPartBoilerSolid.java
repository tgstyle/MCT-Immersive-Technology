package mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart;

import mctmods.immersivetechnology.common.ITContent;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntityBoilerSolidSlave;
import mctmods.immersivetechnology.common.multiblocks.metal.types.BlockType_MetalMultiblock2;
import com.immersiveconvergence.api.capability.IHeatConsumer;
import com.immersiveconvergence.api.util.ICUtils;
import com.immersiveconvergence.api.multiblock.FormationCandidate;
import com.immersiveconvergence.api.multiblock.MachineTemplateMultiblock;
import mctmods.immersivetechnology.common.multiblocks.ITShapes;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import java.util.List;

import javax.annotation.Nullable;

public class TileEntityITMultiblockPartBoilerSolid extends MachineTemplateMultiblock<TileEntityBoilerSolidSlave> {
    public static TileEntityITMultiblockPartBoilerSolid instance = new TileEntityITMultiblockPartBoilerSolid();

    public TileEntityITMultiblockPartBoilerSolid() { super("IT:BoilerSolid", ITShapes.get("boiler_solid"), ICUtils.stateOf(ITContent.blockMetalMultiblock2, BlockType_MetalMultiblock2.BOILER_SOLID), ICUtils.stateOf(ITContent.blockMetalMultiblock2, BlockType_MetalMultiblock2.BOILER_SOLID_SLAVE), 0.5, 1.5, 2, 5.88); }

    @Override @Nullable protected FormationCandidate preferredCandidate(World world, List<FormationCandidate> candidates, @Nullable EntityPlayer player) {
        return FormationCandidate.preferFacing(world, candidates, pointsOfInterest, "heat_output0", (te, side) -> te instanceof IHeatConsumer && ((IHeatConsumer) te).acceptsHeatFrom(side));
    }
}
