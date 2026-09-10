package mctmods.immersivetechnology.common.multiblocks.metal.tileentitiesmultiblockpart;

import mctmods.immersivetechnology.common.ITContent;
import mctmods.immersivetechnology.common.multiblocks.metal.tileentities.TileEntitySteamTurbineSlave;
import mctmods.immersivetechnology.common.multiblocks.metal.types.BlockType_MetalMultiblock;
import com.immersiveconvergence.api.util.ICUtils;
import com.immersiveconvergence.api.multiblock.MachineTemplateMultiblock;
import mctmods.immersivetechnology.common.multiblocks.ITShapes;

public class TileEntityITMultiblockPartSteamTurbine extends MachineTemplateMultiblock<TileEntitySteamTurbineSlave> {
    public static TileEntityITMultiblockPartSteamTurbine instance = new TileEntityITMultiblockPartSteamTurbine();

    public TileEntityITMultiblockPartSteamTurbine() { super("IT:SteamTurbine", ITShapes.get("steam_turbine"), ICUtils.stateOf(ITContent.blockMetalMultiblock, BlockType_MetalMultiblock.STEAM_TURBINE), ICUtils.stateOf(ITContent.blockMetalMultiblock, BlockType_MetalMultiblock.STEAM_TURBINE_SLAVE), 2.7, 2.1, 3.2, 8.7); }
}
