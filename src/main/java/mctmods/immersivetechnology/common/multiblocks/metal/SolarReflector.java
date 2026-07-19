package mctmods.immersivetechnology.common.multiblocks.metal;

import blusunrize.immersiveengineering.api.multiblocks.blocks.util.MultiblockOrientation;
import mctmods.immersivetechnology.common.multiblocks.helper.ModTemplateMultiblock;
import mctmods.immersivetechnology.common.multiblocks.metal.logic.SolarReflectorLogic;
import mctmods.immersivetechnology.common.multiblocks.metal.shapes.SolarReflectorShape;
import mctmods.immersivetechnology.core.util.solarregistry.SolarRegistry;
import mctmods.immersivetechnology.core.lib.Reference;
import mctmods.immersivetechnology.core.registration.MultiblockRegistry;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

public class SolarReflector extends ModTemplateMultiblock {
    public static final SolarReflector INSTANCE = new SolarReflector();

    public SolarReflector() { super(Reference.rl("multiblocks/solar_reflector"), SolarReflectorShape.MASTER_POS, SolarReflectorShape.TRIGGER_POS, new BlockPos(SolarReflectorShape.WIDTH,SolarReflectorShape.HEIGHT,SolarReflectorShape.LENGTH), SolarReflectorShape.CLIENT_OFFSET, SolarReflectorShape.MANUAL_SCALE, MultiblockRegistry.SOLAR_REFLECTOR); }

    @Override public void disassemble(Level world, BlockPos origin, boolean mirrored, Direction clickDirectionAtCreation) {
        MultiblockOrientation orientation = new MultiblockOrientation(clickDirectionAtCreation, mirrored);
        BlockPos base = origin.offset(orientation.getAbsoluteOffset(SolarReflectorLogic.LINK_POI));
        SolarRegistry.unregisterReflector(world, base);
        super.disassemble(world, origin, mirrored, clickDirectionAtCreation);
    }
}
