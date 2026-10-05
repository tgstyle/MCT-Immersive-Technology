package mctmods.immersivetechnology.core.integration.computer;

import mctmods.immersivetechnology.common.multiblocks.metal.logic.SolarCollectorLogic;
import mctmods.immersivetechnology.common.multiblocks.metal.logic.SolarTowerLogic.State;

import blusunrize.immersiveengineering.common.util.compat.computers.generic.Callback;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.CallbackEnvironment;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.ComputerCallable;
import com.immersiveconvergence.api.util.CanisterCallbacks;
import com.immersiveconvergence.api.util.TankInfoCallbacks;

import java.util.Map;

@SuppressWarnings("unused") public class SolarTowerCallbacks extends Callback<State> {
    public SolarTowerCallbacks() {
        addAdditional(new TankInfoCallbacks<>(state -> state.tanks.input(), "input"));
        addAdditional(new TankInfoCallbacks<>(state -> state.tanks.output(), "output"));
        addAdditional(new CanisterCallbacks<>(state -> state.inventory, Map.of("input", SolarCollectorLogic.SLOT_INPUT_FILLED, "output", SolarCollectorLogic.SLOT_OUTPUT_FILLED), Map.of("input", SolarCollectorLogic.SLOT_INPUT_EMPTY, "output", SolarCollectorLogic.SLOT_OUTPUT_EMPTY)));
    }

    @ComputerCallable public double getReflectors(CallbackEnvironment<State> env) { return env.object().reflectorStrength; }
}
