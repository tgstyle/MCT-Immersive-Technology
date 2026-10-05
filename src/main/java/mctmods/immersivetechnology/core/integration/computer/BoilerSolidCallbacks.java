package mctmods.immersivetechnology.core.integration.computer;

import mctmods.immersivetechnology.common.multiblocks.metal.logic.BoilerSolidLogic;
import mctmods.immersivetechnology.common.multiblocks.metal.logic.BoilerSolidLogic.State;

import blusunrize.immersiveengineering.common.util.compat.computers.generic.Callback;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.CallbackEnvironment;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.ComputerCallable;
import net.minecraft.world.item.ItemStack;

@SuppressWarnings("unused") public class BoilerSolidCallbacks extends Callback<State> {
    @ComputerCallable public double getHeat(CallbackEnvironment<State> env) { return env.object().heatLevel; }

    @ComputerCallable public boolean isPilotLit(CallbackEnvironment<State> env) { return env.object().pilotLit; }

    @ComputerCallable public int getBurnRemaining(CallbackEnvironment<State> env) { return env.object().burnRemaining; }

    @ComputerCallable public ItemStack getFuelStack(CallbackEnvironment<State> env) { return env.object().inventory.getStackInSlot(BoilerSolidLogic.INPUT_FUEL_SLOT); }
}
