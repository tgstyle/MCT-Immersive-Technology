package mctmods.immersivetechnology.core.integration.computer;

import mctmods.immersivetechnology.common.multiblocks.metal.logic.SteamTurbineLogic.State;

import blusunrize.immersiveengineering.common.util.compat.computers.generic.Callback;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.CallbackEnvironment;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.ComputerCallable;
import com.immersiveconvergence.api.util.TankInfoCallbacks;

@SuppressWarnings("unused") public class SteamTurbineCallbacks extends Callback<State> {
    public SteamTurbineCallbacks() {
        addAdditional(new TankInfoCallbacks<>(state -> state.tanks.input(), ""));
        addAdditional(new TankInfoCallbacks<>(state -> state.tanks.output(), "output"));
    }

    @ComputerCallable public int getSpeed(CallbackEnvironment<State> env) { return env.object().speed; }
}
