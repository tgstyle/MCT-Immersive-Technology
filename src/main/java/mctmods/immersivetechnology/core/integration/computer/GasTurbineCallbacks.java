package mctmods.immersivetechnology.core.integration.computer;

import mctmods.immersivetechnology.common.multiblocks.metal.logic.GasTurbineLogic.State;

import blusunrize.immersiveengineering.common.util.compat.computers.generic.Callback;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.CallbackEnvironment;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.ComputerCallable;
import com.immersiveconvergence.api.util.TankInfoCallbacks;

@SuppressWarnings("unused") public class GasTurbineCallbacks extends Callback<State> {
    public GasTurbineCallbacks() {
        addAdditional(new TankInfoCallbacks<>(state -> state.tanks.input(), "input"));
        addAdditional(new TankInfoCallbacks<>(state -> state.tanks.output(), "output"));
    }

    @ComputerCallable public int getSpeed(CallbackEnvironment<State> env) { return env.object().speed; }
}
