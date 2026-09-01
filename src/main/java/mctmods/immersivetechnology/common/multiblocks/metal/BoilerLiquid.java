package mctmods.immersivetechnology.common.multiblocks.metal;

import com.immersiveconvergence.api.multiblock.MachineTemplateMultiblock;
import mctmods.immersivetechnology.common.multiblocks.metal.shapes.BoilerLiquidShape;
import mctmods.immersivetechnology.core.lib.Reference;
import mctmods.immersivetechnology.core.registration.MultiblockRegistry;
import net.minecraft.core.BlockPos;

public class BoilerLiquid extends MachineTemplateMultiblock {
    public static final BoilerLiquid INSTANCE = new BoilerLiquid();

    public BoilerLiquid() { super(Reference.rl("multiblocks/boiler_liquid"), BoilerLiquidShape.MASTER_POS, BoilerLiquidShape.TRIGGER_POS, new BlockPos(BoilerLiquidShape.WIDTH,BoilerLiquidShape.HEIGHT,BoilerLiquidShape.LENGTH), BoilerLiquidShape.CLIENT_OFFSET, BoilerLiquidShape.MANUAL_SCALE, MultiblockRegistry.BOILER_LIQUID); }
}
