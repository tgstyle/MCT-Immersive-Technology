package mctmods.immersivetechnology.common.blocks.multiblocks.logic.interfaces;

import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import mctmods.immersivetechnology.common.blocks.multiblocks.helper.ITSlotwiseItemHandler;
import mctmods.immersivetechnology.common.blocks.multiblocks.logic.helper.ITSolarTank;

public interface ITISolarMultiblockState extends IMultiblockState {
    ITSlotwiseItemHandler getInventory();
    ITSolarTank getTanks();
    double getHeatLevel();
    byte[] getDirCounts();
    int getProcessProgress();
    boolean isSunVisible();
}
