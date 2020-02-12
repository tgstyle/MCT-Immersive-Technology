package mctmods.immersivetechnology.common.blocks.metal.tileentities;

import blusunrize.immersiveengineering.api.MultiblockHandler;
import mctmods.immersivetechnology.common.blocks.metal.multiblocks.MultiblockSolarTower;
import mctmods.immersivetechnology.common.util.TemporaryTileEntity;

public class TileEntitySolarTower extends TemporaryTileEntity {

    public MultiblockHandler.IMultiblock getMultiblock() {
        return MultiblockSolarTower.instance;
    }

    public int[] dimensions() {
        return new int[] { 7, 3, 3 };
    }
}