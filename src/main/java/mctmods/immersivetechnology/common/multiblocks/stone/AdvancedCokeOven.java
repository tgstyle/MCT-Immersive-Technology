package mctmods.immersivetechnology.common.multiblocks.stone;

import mctmods.immersivetechnology.common.multiblocks.helper.ModTemplateMultiblock;
import mctmods.immersivetechnology.common.multiblocks.stone.shapes.AdvancedCokeOvenShape;
import mctmods.immersivetechnology.core.lib.Reference;
import mctmods.immersivetechnology.core.registration.MultiblockRegistry;
import net.minecraft.core.BlockPos;

public class AdvancedCokeOven extends ModTemplateMultiblock {
    public static final AdvancedCokeOven INSTANCE = new AdvancedCokeOven();

    public AdvancedCokeOven() { super(Reference.rl("multiblocks/advanced_coke_oven"), AdvancedCokeOvenShape.MASTER_POS, AdvancedCokeOvenShape.TRIGGER_POS, new BlockPos(AdvancedCokeOvenShape.WIDTH,AdvancedCokeOvenShape.HEIGHT,AdvancedCokeOvenShape.LENGTH), AdvancedCokeOvenShape.CLIENT_OFFSET, AdvancedCokeOvenShape.MANUAL_SCALE, MultiblockRegistry.ADVANCED_COKE_OVEN); }
}
