package mctmods.immersivetechnology.common.multiblocks.metal;

import blusunrize.immersiveengineering.api.multiblocks.BlockMatcher;
import com.google.common.collect.ImmutableList;
import com.immersiveconvergence.api.capability.HeatCapabilities;
import com.immersiveconvergence.api.multiblock.FormationCandidate;
import com.immersiveconvergence.api.multiblock.MachineTemplateMultiblock;
import com.immersiveconvergence.api.multiblock.ShapeData;
import mctmods.immersivetechnology.common.multiblocks.ITShapes;
import mctmods.immersivetechnology.common.multiblocks.metal.logic.BoilerSolidLogic;
import mctmods.immersivetechnology.core.lib.Reference;
import mctmods.immersivetechnology.core.registration.MultiblockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import java.util.List;

import javax.annotation.Nullable;

public class BoilerSolid extends MachineTemplateMultiblock {
    private static final ShapeData SHAPE = ITShapes.get("boiler_solid");
    public static final BoilerSolid INSTANCE = new BoilerSolid();

    public BoilerSolid() {
        super(Reference.rl("multiblocks/boiler_solid"), SHAPE.masterPos, SHAPE.triggerPos, new BlockPos(SHAPE.width,SHAPE.height,SHAPE.length), SHAPE.manualScale, ImmutableList.of((expected, found, world, pos) -> {
            if (expected.getBlock() == Blocks.BLAST_FURNACE) { return BlockMatcher.Result.allow(5); }
            return BlockMatcher.Result.DEFAULT;
        }), MultiblockRegistry.BOILER_SOLID);
    }

    @Override @Nullable protected FormationCandidate preferredCandidate(Level world, List<FormationCandidate> candidates, @Nullable Player player) {
        return FormationCandidate.preferFacing(world, candidates, BoilerSolidLogic.HEAT_OUTPUT_POIS, BoilerSolidLogic.HEAT_OUTPUT_FACING, HeatCapabilities.HEAT_CONSUMER_CAPABILITY);
    }
}
