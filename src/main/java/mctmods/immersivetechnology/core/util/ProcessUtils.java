package mctmods.immersivetechnology.core.util;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import java.util.function.Predicate;

public class ProcessUtils {
    public static boolean drainFails(IFluidTank tank, int amount, Predicate<FluidStack> matches) {
        if (cannotDrain(tank, amount, matches)) { return true; }
        tank.drain(amount, FluidAction.EXECUTE);
        return false;
    }

    public static boolean drainFails(IFluidTank tank0, int amount0, Predicate<FluidStack> matches0, IFluidTank tank1, int amount1, Predicate<FluidStack> matches1) {
        if (cannotDrain(tank0, amount0, matches0) || cannotDrain(tank1, amount1, matches1)) { return true; }
        tank0.drain(amount0, FluidAction.EXECUTE);
        tank1.drain(amount1, FluidAction.EXECUTE);
        return false;
    }

    private static boolean cannotDrain(IFluidTank tank, int amount, Predicate<FluidStack> matches) {
        FluidStack drained = tank.drain(amount, FluidAction.SIMULATE);
        return drained.getAmount() < amount || !matches.test(drained);
    }

    public static void fillShare(IFluidTank tank, FluidStack output, int totalTime, boolean last) {
        if (output == null || output.isEmpty()) { return; }
        tank.fill(new FluidStack(output.getFluid(), output.getAmount() / totalTime), FluidAction.EXECUTE);
        int remainder = output.getAmount() % totalTime;
        if (last && remainder > 0) { tank.fill(new FluidStack(output.getFluid(), remainder), FluidAction.EXECUTE); }
    }

    public static void fillOutput(IFluidTank tank, FluidStack output) {
        if (output != null && !output.isEmpty()) { tank.fill(output.copy(), FluidAction.EXECUTE); }
    }
}
