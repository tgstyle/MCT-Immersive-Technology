package mctmods.immersivetechnology.common.blocks.metal.tileentities;

import blusunrize.immersiveengineering.api.MultiblockHandler;
import mctmods.immersivetechnology.common.blocks.metal.multiblocks.MultiblockSolarReflector;
import mctmods.immersivetechnology.common.util.TemporaryTileEntity;

public class TileEntitySolarReflector extends TemporaryTileEntity {

    public MultiblockHandler.IMultiblock getMultiblock() {
        return MultiblockSolarReflector.instance;
    }

    public int[] dimensions() {
        return new int[] {5, 1, 3};
    }
}