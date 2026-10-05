package mctmods.immersivetechnology.core.integration.computer;

import mctmods.immersivetechnology.common.multiblocks.metal.logic.BoilerTankLogic;
import mctmods.immersivetechnology.common.multiblocks.metal.logic.BoilerTankLogic.State;

import blusunrize.immersiveengineering.common.util.compat.computers.generic.Callback;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.CallbackEnvironment;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.ComputerCallable;
import com.immersiveconvergence.api.util.CanisterCallbacks;
import com.immersiveconvergence.api.util.TankInfoCallbacks;

import java.util.Map;

@SuppressWarnings("unused") public class BoilerTankCallbacks extends Callback<State> {
    public BoilerTankCallbacks() {
        addAdditional(new TankInfoCallbacks<>(state -> state.tanks.input(), "input"));
        addAdditional(new TankInfoCallbacks<>(state -> state.tanks.output(), "output"));
        addAdditional(new CanisterCallbacks<>(state -> state.inventory, Map.of("input", BoilerTankLogic.INPUT_SLOT_FILLED, "output", BoilerTankLogic.OUTPUT_SLOT_FILLED), Map.of("input", BoilerTankLogic.INPUT_SLOT_EMPTY, "output", BoilerTankLogic.OUTPUT_SLOT_EMPTY)));
    }

    @ComputerCallable public double getHeat(CallbackEnvironment<State> env) { return env.object().heatLevel; }
}
