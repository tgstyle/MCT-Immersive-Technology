package mctmods.immersivetechnology.common.multiblocks.metal;

import com.immersiveconvergence.api.capability.HeatCapabilities;
import com.immersiveconvergence.api.multiblock.FormationCandidate;
import com.immersiveconvergence.api.multiblock.MachineTemplateMultiblock;
import com.immersiveconvergence.api.multiblock.ShapeData;
import mctmods.immersivetechnology.common.multiblocks.ITShapes;
import mctmods.immersivetechnology.common.multiblocks.metal.logic.BoilerTankLogic;
import mctmods.immersivetechnology.core.lib.Reference;
import mctmods.immersivetechnology.core.registration.MultiblockRegistry;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public class BoilerTank extends MachineTemplateMultiblock {
    private static final ShapeData SHAPE = ITShapes.get("boiler_tank");
    public static final BoilerTank INSTANCE = new BoilerTank();

    public BoilerTank() { super(Reference.rl("multiblocks/boiler_tank"), SHAPE.masterPos, SHAPE.triggerPos, new BlockPos(SHAPE.width, SHAPE.height, SHAPE.length), SHAPE.clientOffset, SHAPE.manualScale, MultiblockRegistry.BOILER_TANK); }

    @Override protected List<TriggerPoint> getTriggerPoints() {
        List<TriggerPoint> points = new ArrayList<>();
        points.add(new TriggerPoint(getTriggerOffset(), Rotation.NONE));
        for (BlockPos symPos : SHAPE.symmetricTriggerOffsets) { points.add(new TriggerPoint(symPos, Rotation.CLOCKWISE_180)); }
        return points;
    }

    @Override @Nullable protected FormationCandidate preferredCandidate(Level world, List<FormationCandidate> candidates, @Nullable Player player) {
        return FormationCandidate.preferFacing(world, candidates, BoilerTankLogic.HEAT_INPUT_POIS, BoilerTankLogic.HEAT_INPUT_FACING, HeatCapabilities.HEAT_PROVIDER_CAPABILITY);
    }
}
