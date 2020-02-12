package mctmods.immersivetechnology.common.blocks.stone.tileentities;

import blusunrize.immersiveengineering.api.MultiblockHandler;
import mctmods.immersivetechnology.common.blocks.stone.multiblocks.MultiblockCokeOvenAdvanced;
import mctmods.immersivetechnology.common.util.TemporaryTileEntity;

public class TileEntityCokeOvenAdvanced extends TemporaryTileEntity {

    public MultiblockHandler.IMultiblock getMultiblock() {
        return MultiblockCokeOvenAdvanced.instance;
    }

    public int[] dimensions() {
        return new int[] {4, 3, 3};
    }
}
