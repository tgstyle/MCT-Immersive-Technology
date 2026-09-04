package mctmods.immersivetechnology.common.multiblocks.metal;

import com.immersiveconvergence.api.multiblock.MachineTemplateMultiblock;
import com.immersiveconvergence.api.multiblock.ShapeData;
import mctmods.immersivetechnology.common.multiblocks.ITShapes;
import mctmods.immersivetechnology.core.lib.Reference;
import mctmods.immersivetechnology.core.registration.MultiblockRegistry;

import net.minecraft.core.BlockPos;

public class HeatExchanger extends MachineTemplateMultiblock {
    private static final ShapeData SHAPE = ITShapes.get("heat_exchanger");
    public static final HeatExchanger INSTANCE = new HeatExchanger();

    public HeatExchanger() { super(Reference.rl("multiblocks/heat_exchanger"), SHAPE.masterPos, SHAPE.triggerPos, new BlockPos(SHAPE.width, SHAPE.height, SHAPE.length), SHAPE.manualScale, MultiblockRegistry.HEAT_EXCHANGER); }
}
