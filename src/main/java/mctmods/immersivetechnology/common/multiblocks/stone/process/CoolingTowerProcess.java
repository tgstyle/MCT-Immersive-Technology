package mctmods.immersivetechnology.common.multiblocks.stone.process;

import mctmods.immersivetechnology.common.multiblocks.stone.logic.CoolingTowerLogic;
import mctmods.immersivetechnology.common.multiblocks.stone.recipe.CoolingTowerRecipe;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;

public class CoolingTowerProcess {
    private final CoolingTowerRecipe recipe;
    private int ticksProcessed = 0;
    private double progressAccumulator = 0.0D;
    private boolean inputsDrained = false;

    public CoolingTowerProcess(CoolingTowerRecipe recipe) { this.recipe = recipe; }

    public void tick(CoolingTowerLogic.State state) { tick(state, 1.0D); }

    public void tick(CoolingTowerLogic.State state, double speedMult) {
        if (ticksProcessed >= recipe.getTotalProcessTime()) { return; }

        if (!inputsDrained) {
            FluidStack drained0 = state.tanks.input0().drain(recipe.getInput0Amount(), FluidAction.EXECUTE);
            FluidStack drained1 = state.tanks.input1().drain(recipe.getInput1Amount(), FluidAction.EXECUTE);
            inputsDrained = true;
            if (drained0.getAmount() < recipe.getInput0Amount() || !drained0.is(recipe.inputTag0()) || drained1.getAmount() < recipe.getInput1Amount() || !drained1.is(recipe.inputTag1())) {
                ticksProcessed = recipe.getTotalProcessTime();
                return;
            }
        }

        progressAccumulator += Math.max(speedMult, 0.0D);
        int advance = (int) progressAccumulator;
        progressAccumulator -= advance;
        for (int i = 0; i < advance && ticksProcessed < recipe.getTotalProcessTime(); i++) {
            FluidStack out0 = recipe.fluidOutput0();
            FluidStack out1 = recipe.fluidOutput1();
            FluidStack out2 = recipe.fluidOutput2();

            int perTickOut0 = out0.getAmount() / recipe.getTotalProcessTime();
            int perTickOut1 = out1.getAmount() / recipe.getTotalProcessTime();
            int perTickOut2 = out2.getAmount() / recipe.getTotalProcessTime();

            if (!out0.isEmpty()) { state.tanks.output0().fill(new FluidStack(out0.getFluid(), perTickOut0), FluidAction.EXECUTE); }
            if (!out1.isEmpty()) { state.tanks.output1().fill(new FluidStack(out1.getFluid(), perTickOut1), FluidAction.EXECUTE); }
            if (!out2.isEmpty()) { state.tanks.output2().fill(new FluidStack(out2.getFluid(), perTickOut2), FluidAction.EXECUTE); }

            ticksProcessed++;

            if (ticksProcessed == recipe.getTotalProcessTime()) {
                int remOut0 = out0.getAmount() % recipe.getTotalProcessTime();
                int remOut1 = out1.getAmount() % recipe.getTotalProcessTime();
                int remOut2 = out2.getAmount() % recipe.getTotalProcessTime();
                if (!out0.isEmpty() && remOut0 > 0) { state.tanks.output0().fill(new FluidStack(out0.getFluid(), remOut0), FluidAction.EXECUTE); }
                if (!out1.isEmpty() && remOut1 > 0) { state.tanks.output1().fill(new FluidStack(out1.getFluid(), remOut1), FluidAction.EXECUTE); }
                if (!out2.isEmpty() && remOut2 > 0) { state.tanks.output2().fill(new FluidStack(out2.getFluid(), remOut2), FluidAction.EXECUTE); }
            }
        }
    }

    public boolean isComplete() { return ticksProcessed >= recipe.getTotalProcessTime(); }

    public int getTicksProcessed() { return ticksProcessed; }

    public CoolingTowerRecipe getRecipe() { return recipe; }
}
