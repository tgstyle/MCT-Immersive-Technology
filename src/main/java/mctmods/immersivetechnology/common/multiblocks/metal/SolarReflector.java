package mctmods.immersivetechnology.common.multiblocks.metal;

import blusunrize.immersiveengineering.api.multiblocks.blocks.util.MultiblockOrientation;
import com.immersiveconvergence.api.multiblock.MachineTemplateMultiblock;
import com.immersiveconvergence.api.multiblock.ShapeData;
import mctmods.immersivetechnology.common.multiblocks.metal.logic.SolarReflectorLogic;
import mctmods.immersivetechnology.common.multiblocks.ITShapes;
import mctmods.immersivetechnology.core.util.solarregistry.SolarRegistry;
import mctmods.immersivetechnology.core.lib.Reference;
import mctmods.immersivetechnology.core.registration.MultiblockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

public class SolarReflector extends MachineTemplateMultiblock {
    private static final ShapeData SHAPE = ITShapes.get("solar_reflector");
    public static final SolarReflector INSTANCE = new SolarReflector();

    public SolarReflector() { super(Reference.rl("multiblocks/solar_reflector"), SHAPE.masterPos, SHAPE.triggerPos, new BlockPos(SHAPE.width,SHAPE.height,SHAPE.length), SHAPE.manualScale, MultiblockRegistry.SOLAR_REFLECTOR); }

    @Override public void disassemble(Level world, BlockPos origin, boolean mirrored, Direction clickDirectionAtCreation) {
        MultiblockOrientation orientation = new MultiblockOrientation(clickDirectionAtCreation, mirrored);
        BlockPos base = origin.offset(orientation.getAbsoluteOffset(SolarReflectorLogic.LINK_POI));
        SolarRegistry.unregisterReflector(world, base);
        super.disassemble(world, origin, mirrored, clickDirectionAtCreation);
    }
}
