package mctmods.immersivetechnology.common.multiblocks.metal;

import mctmods.immersivetechnology.common.multiblocks.helper.ModTemplateMultiblock;
import mctmods.immersivetechnology.common.multiblocks.metal.shapes.SteelSheetmetalTankShape;
import mctmods.immersivetechnology.core.lib.Reference;
import mctmods.immersivetechnology.core.registration.MultiblockRegistry;
import net.minecraft.core.BlockPos;

public class SteelSheetmetalTank extends ModTemplateMultiblock {
    public static final SteelSheetmetalTank INSTANCE = new SteelSheetmetalTank();

    public SteelSheetmetalTank() { super(Reference.rl("multiblocks/steel_sheetmetal_tank"), SteelSheetmetalTankShape.MASTER_POS, SteelSheetmetalTankShape.TRIGGER_POS, new BlockPos(SteelSheetmetalTankShape.WIDTH, SteelSheetmetalTankShape.HEIGHT, SteelSheetmetalTankShape.LENGTH), SteelSheetmetalTankShape.CLIENT_OFFSET, SteelSheetmetalTankShape.MANUAL_SCALE, MultiblockRegistry.STEEL_SHEETMETAL_TANK); }
}
