package mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart;

import mctmods.immersivetechnology.common.ITContent;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntityBoilerTankSlave;
import mctmods.immersivetechnology.common.multiblocks.metal.types.BlockType_MetalMultiblock;
import com.immersiveconvergence.api.capability.IHeatProvider;
import com.immersiveconvergence.api.util.ICUtils;
import com.immersiveconvergence.api.multiblock.FormationCandidate;
import com.immersiveconvergence.api.multiblock.MachineTemplateMultiblock;
import mctmods.immersivetechnology.common.multiblocks.ITShapes;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import java.util.List;

import javax.annotation.Nullable;

public class TileEntityITMultiblockPartBoilerTank extends MachineTemplateMultiblock<TileEntityBoilerTankSlave> {
    public static TileEntityITMultiblockPartBoilerTank instance = new TileEntityITMultiblockPartBoilerTank();

    public TileEntityITMultiblockPartBoilerTank() { super("IT:BoilerTank", ITShapes.get("boiler_tank"), ICUtils.stateOf(ITContent.blockMetalMultiblock, BlockType_MetalMultiblock.BOILER_TANK), ICUtils.stateOf(ITContent.blockMetalMultiblock, BlockType_MetalMultiblock.BOILER_TANK_SLAVE), 0.9, 1.5, 3, 5.88); }

    @Override @Nullable protected FormationCandidate preferredCandidate(World world, List<FormationCandidate> candidates, @Nullable EntityPlayer player) {
        return FormationCandidate.preferFacing(world, candidates, pointsOfInterest, "heat_input0", (te, side) -> te instanceof IHeatProvider && ((IHeatProvider) te).providesHeatTo(side));
    }
}
