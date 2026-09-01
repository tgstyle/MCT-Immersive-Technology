package mctmods.immersivetechnology.common.multiblocks.metal.logic;

import com.immersiveconvergence.api.multiblock.IMachineMultiblockState;

public interface ISolarMultiblockState extends IMachineMultiblockState {
    byte[] getDirCounts();
    boolean isSunVisible();
}
