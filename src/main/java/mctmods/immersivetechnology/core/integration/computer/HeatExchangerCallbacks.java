package mctmods.immersivetechnology.core.integration.computer;

import mctmods.immersivetechnology.common.multiblocks.metal.logic.HeatExchangerLogic.State;

import blusunrize.immersiveengineering.common.util.compat.computers.generic.Callback;
import com.immersiveconvergence.api.util.TankInfoCallbacks;

public class HeatExchangerCallbacks extends Callback<State> {
    public HeatExchangerCallbacks() {
        addAdditional(new TankInfoCallbacks<>(state -> state.tanks.input0(), "first input"));
        addAdditional(new TankInfoCallbacks<>(state -> state.tanks.input1(), "second input"));
        addAdditional(new TankInfoCallbacks<>(state -> state.tanks.output0(), "first output"));
        addAdditional(new TankInfoCallbacks<>(state -> state.tanks.output1(), "second output"));
    }
}
