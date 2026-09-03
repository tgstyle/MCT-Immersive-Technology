package mctmods.immersivetechnology.common.multiblocks.stone;

import com.immersiveconvergence.api.multiblock.MachineTemplateMultiblock;
import com.immersiveconvergence.api.multiblock.ShapeData;
import mctmods.immersivetechnology.common.multiblocks.ITShapes;
import mctmods.immersivetechnology.core.lib.Reference;
import mctmods.immersivetechnology.core.registration.MultiblockRegistry;
import net.minecraft.core.BlockPos;

public class AdvancedCokeOven extends MachineTemplateMultiblock {
    private static final ShapeData SHAPE = ITShapes.get("advanced_coke_oven");
    public static final AdvancedCokeOven INSTANCE = new AdvancedCokeOven();

    public AdvancedCokeOven() { super(Reference.rl("multiblocks/advanced_coke_oven"), SHAPE.masterPos, SHAPE.triggerPos, new BlockPos(SHAPE.width,SHAPE.height,SHAPE.length), SHAPE.clientOffset, SHAPE.manualScale, MultiblockRegistry.ADVANCED_COKE_OVEN); }
}
