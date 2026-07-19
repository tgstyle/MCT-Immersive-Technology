package mctmods.immersivetechnology.common.multiblocks.metal;

import mctmods.immersivetechnology.common.multiblocks.helper.ITTemplateMultiblock;
import mctmods.immersivetechnology.common.multiblocks.metal.shapes.SteelSheetmetalTankShape;
import mctmods.immersivetechnology.core.lib.ITLib;
import mctmods.immersivetechnology.core.registration.ITMultiblockRegistry;

import net.minecraft.core.BlockPos;

public class SteelSheetmetalTank extends ITTemplateMultiblock {
    public static final SteelSheetmetalTank INSTANCE = new SteelSheetmetalTank();

    public SteelSheetmetalTank() { super(ITLib.rl("multiblocks/steel_sheetmetal_tank"), SteelSheetmetalTankShape.MASTER_POS, SteelSheetmetalTankShape.TRIGGER_POS, new BlockPos(SteelSheetmetalTankShape.WIDTH, SteelSheetmetalTankShape.HEIGHT, SteelSheetmetalTankShape.LENGTH), SteelSheetmetalTankShape.CLIENT_OFFSET, SteelSheetmetalTankShape.MANUAL_SCALE, ITMultiblockRegistry.STEEL_SHEETMETAL_TANK); }
}
