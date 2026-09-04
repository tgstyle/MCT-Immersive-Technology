package mctmods.immersivetechnology.common.multiblocks.metal;

import com.immersiveconvergence.api.multiblock.MachineTemplateMultiblock;
import com.immersiveconvergence.api.multiblock.ShapeData;
import mctmods.immersivetechnology.common.multiblocks.ITShapes;
import mctmods.immersivetechnology.core.lib.Reference;
import mctmods.immersivetechnology.core.registration.MultiblockRegistry;
import net.minecraft.core.BlockPos;

public class RadiatorHorizontal extends MachineTemplateMultiblock {
    private static final ShapeData SHAPE = ITShapes.get("radiator_horizontal");
    public static final RadiatorHorizontal INSTANCE = new RadiatorHorizontal();

    public RadiatorHorizontal() { super(Reference.rl("multiblocks/radiator_horizontal"), SHAPE.masterPos, SHAPE.triggerPos, new BlockPos(SHAPE.width, SHAPE.height, SHAPE.length), SHAPE.manualScale, MultiblockRegistry.RADIATOR_HORIZONTAL); }
}
