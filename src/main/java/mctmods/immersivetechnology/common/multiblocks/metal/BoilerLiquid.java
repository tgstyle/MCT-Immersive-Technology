package mctmods.immersivetechnology.common.multiblocks.metal;

import com.immersiveconvergence.api.capability.HeatCapabilities;
import com.immersiveconvergence.api.multiblock.FormationCandidate;
import com.immersiveconvergence.api.multiblock.MachineTemplateMultiblock;
import mctmods.immersivetechnology.common.multiblocks.ITShapes;
import mctmods.immersivetechnology.common.multiblocks.metal.logic.BoilerLiquidLogic;
import mctmods.immersivetechnology.core.lib.Reference;
import mctmods.immersivetechnology.core.registration.MultiblockRegistry;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import java.util.List;

import javax.annotation.Nullable;

public class BoilerLiquid extends MachineTemplateMultiblock {
    public static final BoilerLiquid INSTANCE = new BoilerLiquid();

    public BoilerLiquid() { super(Reference.rl("multiblocks/boiler_liquid"), () -> ITShapes.get("boiler_liquid"), MultiblockRegistry.BOILER_LIQUID); }

    @Override @Nullable protected FormationCandidate preferredCandidate(Level world, List<FormationCandidate> candidates, @Nullable Player player) {
        return FormationCandidate.preferFacing(world, candidates, BoilerLiquidLogic.HEAT_OUTPUT_POIS, BoilerLiquidLogic.HEAT_OUTPUT_FACING, HeatCapabilities.HEAT_CONSUMER_CAPABILITY);
    }
}
