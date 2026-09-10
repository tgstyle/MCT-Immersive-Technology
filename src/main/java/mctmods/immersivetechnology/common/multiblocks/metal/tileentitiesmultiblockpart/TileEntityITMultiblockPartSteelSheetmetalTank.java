package mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart;

import mctmods.immersivetechnology.common.ITContent;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntitySteelSheetmetalTankSlave;
import mctmods.immersivetechnology.common.multiblocks.metal.types.BlockType_MetalMultiblock;
import com.immersiveconvergence.api.util.ICUtils;
import com.immersiveconvergence.api.multiblock.MachineTemplateMultiblock;
import mctmods.immersivetechnology.common.multiblocks.ITShapes;

public class TileEntityITMultiblockPartSteelSheetmetalTank extends MachineTemplateMultiblock<TileEntitySteelSheetmetalTankSlave> {

    public static TileEntityITMultiblockPartSteelSheetmetalTank instance = new TileEntityITMultiblockPartSteelSheetmetalTank();

    public TileEntityITMultiblockPartSteelSheetmetalTank() { super("IT:SteelSheetmetalTank", ITShapes.get("steel_sheetmetal_tank"), ICUtils.stateOf(ITContent.blockMetalMultiblock, BlockType_MetalMultiblock.STEEL_TANK), ICUtils.stateOf(ITContent.blockMetalMultiblock, BlockType_MetalMultiblock.STEEL_TANK_SLAVE), 1.875, 1.75, 1.125, 5.5); }
}
