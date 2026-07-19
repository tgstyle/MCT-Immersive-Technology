package mctmods.immersivetechnology.common.multiblocks.metal;

import mctmods.immersivetechnology.common.multiblocks.helper.ITTemplateMultiblock;
import mctmods.immersivetechnology.common.multiblocks.metal.shapes.BoilerTankShape;
import mctmods.immersivetechnology.core.lib.ITLib;
import mctmods.immersivetechnology.core.registration.ITMultiblockRegistry;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Rotation;
import java.util.ArrayList;
import java.util.List;

public class BoilerTank extends ITTemplateMultiblock {
    public static final BoilerTank INSTANCE = new BoilerTank();

    public BoilerTank() { super(ITLib.rl("multiblocks/boiler_tank"), BoilerTankShape.MASTER_POS, BoilerTankShape.TRIGGER_POS, new BlockPos(BoilerTankShape.WIDTH, BoilerTankShape.HEIGHT, BoilerTankShape.LENGTH), BoilerTankShape.CLIENT_OFFSET, BoilerTankShape.MANUAL_SCALE, ITMultiblockRegistry.BOILER_TANK); }

    @Override protected List<TriggerPoint> getTriggerPoints() {
        List<TriggerPoint> points = new ArrayList<>();
        points.add(new TriggerPoint(getTriggerOffset(), Rotation.NONE));
        for (BlockPos symPos : BoilerTankShape.SYMMETRIC_TRIGGER_OFFSETS) { points.add(new TriggerPoint(symPos, Rotation.CLOCKWISE_180)); }
        return points;
    }
}
