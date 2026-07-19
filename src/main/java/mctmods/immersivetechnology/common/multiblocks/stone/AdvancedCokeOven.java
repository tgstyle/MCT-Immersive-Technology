package mctmods.immersivetechnology.common.multiblocks.stone;

import mctmods.immersivetechnology.common.multiblocks.helper.ITTemplateMultiblock;
import mctmods.immersivetechnology.common.multiblocks.stone.shapes.AdvancedCokeOvenShape;
import mctmods.immersivetechnology.core.lib.ITLib;
import mctmods.immersivetechnology.core.registration.ITMultiblockRegistry;

import net.minecraft.core.BlockPos;

public class AdvancedCokeOven extends ITTemplateMultiblock {
    public static final AdvancedCokeOven INSTANCE = new AdvancedCokeOven();

    public AdvancedCokeOven() { super(ITLib.rl("multiblocks/advanced_coke_oven"), AdvancedCokeOvenShape.MASTER_POS, AdvancedCokeOvenShape.TRIGGER_POS, new BlockPos(AdvancedCokeOvenShape.WIDTH,AdvancedCokeOvenShape.HEIGHT,AdvancedCokeOvenShape.LENGTH), AdvancedCokeOvenShape.CLIENT_OFFSET, AdvancedCokeOvenShape.MANUAL_SCALE, ITMultiblockRegistry.ADVANCED_COKE_OVEN); }
}
