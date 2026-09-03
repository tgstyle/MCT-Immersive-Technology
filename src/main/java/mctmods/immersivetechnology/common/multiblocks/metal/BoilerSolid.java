package mctmods.immersivetechnology.common.multiblocks.metal;

import blusunrize.immersiveengineering.api.multiblocks.BlockMatcher;
import com.google.common.collect.ImmutableList;
import com.immersiveconvergence.api.multiblock.MachineTemplateMultiblock;
import com.immersiveconvergence.api.multiblock.ShapeData;
import mctmods.immersivetechnology.common.multiblocks.ITShapes;
import mctmods.immersivetechnology.core.lib.Reference;
import mctmods.immersivetechnology.core.registration.MultiblockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;

public class BoilerSolid extends MachineTemplateMultiblock {
    private static final ShapeData SHAPE = ITShapes.get("boiler_solid");
    public static final BoilerSolid INSTANCE = new BoilerSolid();

    public BoilerSolid() {
        super(Reference.rl("multiblocks/boiler_solid"), SHAPE.masterPos, SHAPE.triggerPos, new BlockPos(SHAPE.width,SHAPE.height,SHAPE.length), SHAPE.clientOffset, SHAPE.manualScale, ImmutableList.of((expected, found, world, pos) -> {
            if (expected.getBlock() == Blocks.BLAST_FURNACE) { return BlockMatcher.Result.allow(5); }
            return BlockMatcher.Result.DEFAULT;
        }), MultiblockRegistry.BOILER_SOLID);
    }
}
