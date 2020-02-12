package mctmods.immersivetechnology.common.blocks.metal.tileentities;

import blusunrize.immersiveengineering.api.MultiblockHandler;
import mctmods.immersivetechnology.common.blocks.metal.multiblocks.MultiblockSteamTurbine;
import mctmods.immersivetechnology.common.util.TemporaryTileEntity;

public class TileEntitySteamTurbine extends TemporaryTileEntity {

    public MultiblockHandler.IMultiblock getMultiblock() {
        return MultiblockSteamTurbine.instance;
    }

    public int[] dimensions() {
        return new int[] { 4, 10, 3 };
    }
}
