package mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart;

import mctmods.immersivetechnology.common.ITContent;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntityHeatExchangerSlave;
import mctmods.immersivetechnology.common.multiblocks.metal.types.BlockType_MetalMultiblock1;
import com.immersiveconvergence.api.util.ICUtils;
import com.immersiveconvergence.api.multiblock.MachineTemplateMultiblock;
import mctmods.immersivetechnology.common.multiblocks.ITShapes;

public class TileEntityITMultiblockPartHeatExchanger extends MachineTemplateMultiblock<TileEntityHeatExchangerSlave> {
    public static TileEntityITMultiblockPartHeatExchanger instance = new TileEntityITMultiblockPartHeatExchanger();

    public TileEntityITMultiblockPartHeatExchanger() { super("IT:HeatExchanger", ITShapes.get("heat_exchanger"), ICUtils.stateOf(ITContent.blockMetalMultiblock1, BlockType_MetalMultiblock1.HEAT_EXCHANGER), ICUtils.stateOf(ITContent.blockMetalMultiblock1, BlockType_MetalMultiblock1.HEAT_EXCHANGER_SLAVE), 0.9, 1.5, 3, 5.88); }
}
