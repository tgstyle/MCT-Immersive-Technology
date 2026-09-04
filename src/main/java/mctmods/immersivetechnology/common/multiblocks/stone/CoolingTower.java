package mctmods.immersivetechnology.common.multiblocks.stone;

import com.immersiveconvergence.api.multiblock.MachineTemplateMultiblock;
import com.immersiveconvergence.api.multiblock.ShapeData;
import mctmods.immersivetechnology.common.multiblocks.ITShapes;
import mctmods.immersivetechnology.core.lib.Reference;
import mctmods.immersivetechnology.core.registration.MultiblockRegistry;
import net.minecraft.core.BlockPos;

public class CoolingTower extends MachineTemplateMultiblock {
    private static final ShapeData SHAPE = ITShapes.get("cooling_tower");
    public static final CoolingTower INSTANCE = new CoolingTower();

    public CoolingTower() { super(Reference.rl("multiblocks/cooling_tower"), SHAPE.masterPos, SHAPE.triggerPos, new BlockPos(SHAPE.width,SHAPE.height,SHAPE.length), SHAPE.manualScale, MultiblockRegistry.COOLING_TOWER); }
}
