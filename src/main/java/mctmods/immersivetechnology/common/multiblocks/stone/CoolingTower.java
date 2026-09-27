package mctmods.immersivetechnology.common.multiblocks.stone;

import com.immersiveconvergence.api.multiblock.MachineTemplateMultiblock;
import mctmods.immersivetechnology.common.multiblocks.ITShapes;
import mctmods.immersivetechnology.core.lib.Reference;
import mctmods.immersivetechnology.core.registration.MultiblockRegistry;

public class CoolingTower extends MachineTemplateMultiblock {
    public static final CoolingTower INSTANCE = new CoolingTower();

    public CoolingTower() { super(Reference.rl("multiblocks/cooling_tower"), () -> ITShapes.get("cooling_tower"), MultiblockRegistry.COOLING_TOWER); }
}
