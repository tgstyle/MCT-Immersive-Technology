package mctmods.immersivetechnology.common.multiblocks.metal;

import com.immersiveconvergence.api.capability.HeatCapabilities;
import com.immersiveconvergence.api.multiblock.FormationCandidate;
import com.immersiveconvergence.api.multiblock.MachineTemplateMultiblock;
import com.immersiveconvergence.api.multiblock.ShapeData;
import mctmods.immersivetechnology.common.multiblocks.ITShapes;
import mctmods.immersivetechnology.common.multiblocks.metal.logic.BoilerLiquidLogic;
import mctmods.immersivetechnology.core.lib.Reference;
import mctmods.immersivetechnology.core.registration.MultiblockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import java.util.List;

import javax.annotation.Nullable;

public class BoilerLiquid extends MachineTemplateMultiblock {
    private static final ShapeData SHAPE = ITShapes.get("boiler_liquid");
    public static final BoilerLiquid INSTANCE = new BoilerLiquid();

    public BoilerLiquid() { super(Reference.rl("multiblocks/boiler_liquid"), SHAPE.masterPos, SHAPE.triggerPos, new BlockPos(SHAPE.width,SHAPE.height,SHAPE.length), SHAPE.manualScale, MultiblockRegistry.BOILER_LIQUID); }

    @Override @Nullable protected FormationCandidate preferredCandidate(Level world, List<FormationCandidate> candidates, @Nullable Player player) {
        return FormationCandidate.preferFacing(world, candidates, BoilerLiquidLogic.HEAT_OUTPUT_POIS, BoilerLiquidLogic.HEAT_OUTPUT_FACING, HeatCapabilities.HEAT_CONSUMER_CAPABILITY);
    }
}
