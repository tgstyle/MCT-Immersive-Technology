package mctmods.immersivetechnology.common.multiblocks.metal;

import com.immersiveconvergence.api.multiblock.MachineTemplateMultiblock;
import com.immersiveconvergence.api.multiblock.ShapeData;
import mctmods.immersivetechnology.common.multiblocks.ITShapes;
import mctmods.immersivetechnology.core.lib.Reference;
import mctmods.immersivetechnology.core.registration.MultiblockRegistry;
import net.minecraft.core.BlockPos;

public class SteelSheetmetalTank extends MachineTemplateMultiblock {
    private static final ShapeData SHAPE = ITShapes.get("steel_sheetmetal_tank");
    public static final SteelSheetmetalTank INSTANCE = new SteelSheetmetalTank();

    public SteelSheetmetalTank() { super(Reference.rl("multiblocks/steel_sheetmetal_tank"), SHAPE.masterPos, SHAPE.triggerPos, new BlockPos(SHAPE.width, SHAPE.height, SHAPE.length), SHAPE.clientOffset, SHAPE.manualScale, MultiblockRegistry.STEEL_SHEETMETAL_TANK); }
}
