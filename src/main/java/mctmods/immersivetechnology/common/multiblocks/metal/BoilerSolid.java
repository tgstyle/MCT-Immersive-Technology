package mctmods.immersivetechnology.common.multiblocks.metal;

import blusunrize.immersiveengineering.api.multiblocks.BlockMatcher;
import blusunrize.immersiveengineering.api.multiblocks.ClientMultiblocks;
import com.google.common.collect.ImmutableList;
import mctmods.immersivetechnology.common.multiblocks.helper.ITClientMultiblockProperties;
import mctmods.immersivetechnology.common.multiblocks.helper.ITTemplateMultiblock;
import mctmods.immersivetechnology.common.multiblocks.metal.shapes.BoilerSolidShape;
import mctmods.immersivetechnology.core.lib.ITLib;
import mctmods.immersivetechnology.core.registration.ITMultiblockProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import java.util.function.Consumer;

public class BoilerSolid extends ITTemplateMultiblock {
    public static final BoilerSolid INSTANCE = new BoilerSolid();

    public BoilerSolid() {
        super(ITLib.rl("multiblocks/boiler_solid"), BoilerSolidShape.MASTER_POS, BoilerSolidShape.TRIGGER_POS, new BlockPos(BoilerSolidShape.WIDTH,BoilerSolidShape.HEIGHT,BoilerSolidShape.LENGTH), ImmutableList.of((expected, found, world, pos) -> {
            if (expected.getBlock() == Blocks.BLAST_FURNACE) { return BlockMatcher.Result.allow(5); }
            return BlockMatcher.Result.DEFAULT;
        }), ITMultiblockProvider.BOILER_SOLID);
    }

    @Override public void disassemble(Level world, BlockPos origin, boolean mirrored, Direction clickDirectionAtCreation) { super.disassemble(world, origin, mirrored, clickDirectionAtCreation); }

    @Override public float getManualScale() { return BoilerSolidShape.MANUAL_SCALE; }

    @Override public void initializeClient(Consumer<ClientMultiblocks.MultiblockManualData> consumer) { consumer.accept(new ITClientMultiblockProperties(this, BoilerSolidShape.CLIENT_OFFSET.getX(), BoilerSolidShape.CLIENT_OFFSET.getY(), BoilerSolidShape.CLIENT_OFFSET.getZ())); }

    @Override public boolean canBeMirrored() { return false; }
}
