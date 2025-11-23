package mctmods.immersivetechnology.common.multiblocks.stone.process;

import mctmods.immersivetechnology.common.multiblocks.stone.logic.CoolingTowerLogic;
import mctmods.immersivetechnology.common.multiblocks.stone.recipe.CoolingTowerRecipe;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

public class CoolingTowerProcess {
    private final CoolingTowerRecipe recipe;
    private int ticksProcessed = 0;

    public CoolingTowerProcess(CoolingTowerRecipe recipe) { this.recipe = recipe; }

    public void tick(CoolingTowerLogic.State state) {
        if (ticksProcessed >= recipe.totalProcessTime) return;
        if (ticksProcessed == 0) {
            FluidStack drained0 = state.tanks.input0().drain(recipe.input0.getAmount(), FluidAction.EXECUTE);
            FluidStack drained1 = state.tanks.input1().drain(recipe.input1.getAmount(), FluidAction.EXECUTE);
            if (drained0.getAmount() < recipe.input0.getAmount() || !recipe.input0.testIgnoringAmount(drained0) || drained1.getAmount() < recipe.input1.getAmount() || !recipe.input1.testIgnoringAmount(drained1)) { ticksProcessed = recipe.totalProcessTime; return; }
        }
        int perTickOut0 = recipe.fluidOutput0.getAmount() / recipe.totalProcessTime;
        int perTickOut1 = recipe.fluidOutput1.getAmount() / recipe.totalProcessTime;
        int perTickOut2 = recipe.fluidOutput2.getAmount() / recipe.totalProcessTime;
        if (!recipe.fluidOutput0.isEmpty()) { state.tanks.output0().fill(new FluidStack(recipe.fluidOutput0.getFluid(), perTickOut0), FluidAction.EXECUTE); }
        if (!recipe.fluidOutput1.isEmpty()) { state.tanks.output1().fill(new FluidStack(recipe.fluidOutput1.getFluid(), perTickOut1), FluidAction.EXECUTE); }
        if (!recipe.fluidOutput2.isEmpty()) { state.tanks.output2().fill(new FluidStack(recipe.fluidOutput2.getFluid(), perTickOut2), FluidAction.EXECUTE); }
        ticksProcessed++;
        if (ticksProcessed == recipe.totalProcessTime) {
            int remOut0 = recipe.fluidOutput0.getAmount() % recipe.totalProcessTime;
            int remOut1 = recipe.fluidOutput1.getAmount() % recipe.totalProcessTime;
            int remOut2 = recipe.fluidOutput2.getAmount() % recipe.totalProcessTime;
            if (!recipe.fluidOutput0.isEmpty()) { state.tanks.output0().fill(new FluidStack(recipe.fluidOutput0.getFluid(), remOut0), FluidAction.EXECUTE); }
            if (!recipe.fluidOutput1.isEmpty()) { state.tanks.output1().fill(new FluidStack(recipe.fluidOutput1.getFluid(), remOut1), FluidAction.EXECUTE); }
            if (!recipe.fluidOutput2.isEmpty()) { state.tanks.output2().fill(new FluidStack(recipe.fluidOutput2.getFluid(), remOut2), FluidAction.EXECUTE); }
        }
    }

    public boolean isComplete() { return ticksProcessed >= recipe.totalProcessTime; }
}
