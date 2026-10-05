package mctmods.immersivetechnology.core.integration.computer;

import mctmods.immersivetechnology.common.multiblocks.metal.logic.BoilerLiquidLogic;
import mctmods.immersivetechnology.common.multiblocks.metal.logic.BoilerLiquidLogic.State;

import blusunrize.immersiveengineering.common.util.compat.computers.generic.Callback;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.CallbackEnvironment;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.ComputerCallable;
import com.immersiveconvergence.api.util.CanisterCallbacks;
import com.immersiveconvergence.api.util.TankInfoCallbacks;

import java.util.Map;

@SuppressWarnings("unused") public class BoilerLiquidCallbacks extends Callback<State> {
    public BoilerLiquidCallbacks() {
        addAdditional(new TankInfoCallbacks<>(state -> state.tanks.input1(), "fuel"));
        addAdditional(new CanisterCallbacks<>(state -> state.inventory, Map.of("fuel", BoilerLiquidLogic.INPUT_FUEL_SLOT_FILLED), Map.of("fuel", BoilerLiquidLogic.INPUT_FUEL_SLOT_EMPTY)));
    }

    @ComputerCallable public double getHeat(CallbackEnvironment<State> env) { return env.object().heatLevel; }

    @ComputerCallable public boolean isPilotLit(CallbackEnvironment<State> env) { return env.object().pilotLit; }
}
