package mctmods.immersivetechnology.core.integration.computer;

import mctmods.immersivetechnology.common.multiblocks.metal.logic.DistillerLogic;
import mctmods.immersivetechnology.common.multiblocks.metal.logic.DistillerLogic.State;

import blusunrize.immersiveengineering.common.util.compat.computers.generic.Callback;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.CallbackEnvironment;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.ComputerCallable;
import com.immersiveconvergence.api.util.CanisterCallbacks;
import com.immersiveconvergence.api.util.TankInfoCallbacks;

import java.util.Map;

@SuppressWarnings("unused") public class DistillerCallbacks extends Callback<State> {
    public DistillerCallbacks() {
        addAdditional(new TankInfoCallbacks<>(state -> state.tanks.input(), "input"));
        addAdditional(new TankInfoCallbacks<>(state -> state.tanks.output(), "output"));
        addAdditional(new CanisterCallbacks<>(state -> state.inventory, Map.of("input", DistillerLogic.SLOT_INPUT_FILLED, "output", DistillerLogic.SLOT_OUTPUT_FILLED), Map.of("input", DistillerLogic.SLOT_INPUT_EMPTY, "output", DistillerLogic.SLOT_OUTPUT_EMPTY)));
    }

    @ComputerCallable public int getEnergyStored(CallbackEnvironment<State> env) { return env.object().energy.getEnergyStored(); }

    @ComputerCallable public int getMaxEnergyStored(CallbackEnvironment<State> env) { return env.object().energy.getMaxEnergyStored(); }
}
