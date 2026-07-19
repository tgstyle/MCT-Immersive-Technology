package mctmods.immersivetechnology.common.multiblocks.helper;

import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import mctmods.immersivetechnology.common.fluids.helper.SolarTank;

public interface ISolarMultiblockState extends IMultiblockState {
    SlotwiseItemHandler getInventory();
    SolarTank getTanks();
    double getHeatLevel();
    byte[] getDirCounts();
    int getProcessProgress();
    boolean isSunVisible();
}
