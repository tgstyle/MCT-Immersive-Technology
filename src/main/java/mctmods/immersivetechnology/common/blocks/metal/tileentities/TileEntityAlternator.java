package mctmods.immersivetechnology.common.blocks.metal.tileentities;

import blusunrize.immersiveengineering.api.MultiblockHandler;
import mctmods.immersivetechnology.common.blocks.metal.multiblocks.MultiblockAlternator;
import mctmods.immersivetechnology.common.util.TemporaryTileEntity;

public class TileEntityAlternator extends TemporaryTileEntity {

    public MultiblockHandler.IMultiblock getMultiblock() {
        return MultiblockAlternator.instance;
    }

    public int[] dimensions() {
        return new int[] {3, 4, 3};
    }
}