package mctmods.immersivetechnology.common.multiblocks.stone.tileentitiesmultiblockpart;

import mctmods.immersivetechnology.common.ITContent;
import mctmods.immersivetechnology.common.multiblocks.ITShapes;
import mctmods.immersivetechnology.common.multiblocks.stone.tileentities.TileEntityAdvancedCokeOvenSlave;
import mctmods.immersivetechnology.common.multiblocks.stone.types.BlockType_StoneMultiblock;
import com.immersiveconvergence.api.util.ICUtils;
import com.immersiveconvergence.api.multiblock.MachineTemplateMultiblock;

public class TileEntityITMultiblockPartAdvancedCokeOven extends MachineTemplateMultiblock<TileEntityAdvancedCokeOvenSlave> {
    public static TileEntityITMultiblockPartAdvancedCokeOven instance = new TileEntityITMultiblockPartAdvancedCokeOven();

    public TileEntityITMultiblockPartAdvancedCokeOven() { super("IT:AdvancedCokeOven", ITShapes.get("advanced_coke_oven"), ICUtils.stateOf(ITContent.blockStoneMultiblock, BlockType_StoneMultiblock.ADVANCED_COKE_OVEN), ICUtils.stateOf(ITContent.blockStoneMultiblock, BlockType_StoneMultiblock.ADVANCED_COKE_OVEN_SLAVE), 0.5, 1.5, 1.5, 4); }
}
