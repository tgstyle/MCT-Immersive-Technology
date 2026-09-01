package mctmods.immersivetechnology.common.multiblocks.metal;

import blusunrize.immersiveengineering.api.multiblocks.BlockMatcher;
import com.google.common.collect.ImmutableList;
import com.immersiveconvergence.api.multiblock.MachineTemplateMultiblock;
import mctmods.immersivetechnology.common.multiblocks.metal.shapes.BoilerSolidShape;
import mctmods.immersivetechnology.core.lib.Reference;
import mctmods.immersivetechnology.core.registration.MultiblockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;

public class BoilerSolid extends MachineTemplateMultiblock {
    public static final BoilerSolid INSTANCE = new BoilerSolid();

    public BoilerSolid() {
        super(Reference.rl("multiblocks/boiler_solid"), BoilerSolidShape.MASTER_POS, BoilerSolidShape.TRIGGER_POS, new BlockPos(BoilerSolidShape.WIDTH,BoilerSolidShape.HEIGHT,BoilerSolidShape.LENGTH), BoilerSolidShape.CLIENT_OFFSET, BoilerSolidShape.MANUAL_SCALE, ImmutableList.of((expected, found, world, pos) -> {
            if (expected.getBlock() == Blocks.BLAST_FURNACE) { return BlockMatcher.Result.allow(5); }
            return BlockMatcher.Result.DEFAULT;
        }), MultiblockRegistry.BOILER_SOLID);
    }
}
