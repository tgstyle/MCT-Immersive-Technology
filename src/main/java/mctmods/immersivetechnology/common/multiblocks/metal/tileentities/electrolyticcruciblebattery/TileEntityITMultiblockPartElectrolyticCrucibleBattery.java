package mctmods.immersivetechnology.common.multiblocks.metal.tileentities.electrolyticcruciblebattery;

import mctmods.immersivetechnology.common.ITContent;
import mctmods.immersivetechnology.common.multiblocks.ITShapes;
import mctmods.immersivetechnology.common.multiblocks.metal.types.BlockType_MetalMultiblock1;

import com.immersiveconvergence.api.multiblock.MachineTemplateMultiblock;
import com.immersiveconvergence.api.util.ICUtils;

public class TileEntityITMultiblockPartElectrolyticCrucibleBattery extends MachineTemplateMultiblock<TileEntityElectrolyticCrucibleBatterySlave> {
    public static TileEntityITMultiblockPartElectrolyticCrucibleBattery instance = new TileEntityITMultiblockPartElectrolyticCrucibleBattery();

    public TileEntityITMultiblockPartElectrolyticCrucibleBattery() { super("IT:electrolyticCrucibleBattery", ITShapes.get("electrolytic_crucible_battery"), ICUtils.stateOf(ITContent.blockMetalMultiblock1, BlockType_MetalMultiblock1.ELECTROLYTIC_CRUCIBLE_BATTERY), ICUtils.stateOf(ITContent.blockMetalMultiblock1, BlockType_MetalMultiblock1.ELECTROLYTIC_CRUCIBLE_BATTERY_SLAVE), 2, 2.5, 2, 7.14); }
}
