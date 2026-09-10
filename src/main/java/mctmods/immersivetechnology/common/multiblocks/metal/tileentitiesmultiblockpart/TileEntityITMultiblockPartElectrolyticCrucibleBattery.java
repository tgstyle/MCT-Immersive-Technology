package mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart;

import mctmods.immersivetechnology.common.ITContent;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntityElectrolyticCrucibleBatterySlave;
import mctmods.immersivetechnology.common.multiblocks.metal.types.BlockType_MetalMultiblock1;
import com.immersiveconvergence.api.util.ICUtils;
import com.immersiveconvergence.api.multiblock.MachineTemplateMultiblock;
import mctmods.immersivetechnology.common.multiblocks.ITShapes;

public class TileEntityITMultiblockPartElectrolyticCrucibleBattery extends MachineTemplateMultiblock<TileEntityElectrolyticCrucibleBatterySlave> {
    public static TileEntityITMultiblockPartElectrolyticCrucibleBattery instance = new TileEntityITMultiblockPartElectrolyticCrucibleBattery();

    public TileEntityITMultiblockPartElectrolyticCrucibleBattery() { super("IT:electrolyticCrucibleBattery", ITShapes.get("electrolytic_crucible_battery"), ICUtils.stateOf(ITContent.blockMetalMultiblock1, BlockType_MetalMultiblock1.ELECTROLYTIC_CRUCIBLE_BATTERY), ICUtils.stateOf(ITContent.blockMetalMultiblock1, BlockType_MetalMultiblock1.ELECTROLYTIC_CRUCIBLE_BATTERY_SLAVE), 2, 2.5, 2, 7.14); }
}
