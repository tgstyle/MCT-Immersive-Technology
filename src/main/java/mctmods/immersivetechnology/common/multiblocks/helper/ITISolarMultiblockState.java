package mctmods.immersivetechnology.common.multiblocks.helper;

import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import mctmods.immersivetechnology.common.fluids.helper.ITSolarTank;

public interface ITISolarMultiblockState extends IMultiblockState {
    ITSlotwiseItemHandler getInventory();
    ITSolarTank getTanks();
    double getHeatLevel();
    byte[] getDirCounts();
    int getProcessProgress();
    boolean isSunVisible();
}
