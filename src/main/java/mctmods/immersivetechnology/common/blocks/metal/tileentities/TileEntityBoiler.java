package mctmods.immersivetechnology.common.blocks.metal.tileentities;

import blusunrize.immersiveengineering.api.MultiblockHandler;
import mctmods.immersivetechnology.common.blocks.metal.multiblocks.MultiblockBoiler;
import mctmods.immersivetechnology.common.util.TemporaryTileEntity;

public class TileEntityBoiler extends TemporaryTileEntity {

    public MultiblockHandler.IMultiblock getMultiblock() {
        return MultiblockBoiler.instance;
    }

    public int[] dimensions() {
        return new int[] { 3, 3, 5 };
    }
}