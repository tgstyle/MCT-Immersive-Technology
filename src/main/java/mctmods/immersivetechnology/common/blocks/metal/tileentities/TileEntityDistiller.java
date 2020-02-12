package mctmods.immersivetechnology.common.blocks.metal.tileentities;

import blusunrize.immersiveengineering.api.MultiblockHandler;
import mctmods.immersivetechnology.common.blocks.metal.multiblocks.MultiblockDistiller;
import mctmods.immersivetechnology.common.util.TemporaryTileEntity;

public class TileEntityDistiller extends TemporaryTileEntity {

    public MultiblockHandler.IMultiblock getMultiblock() {
        return MultiblockDistiller.instance;
    }

    public int[] dimensions() {
        return new int[] { 3, 3, 3 };
    }
}