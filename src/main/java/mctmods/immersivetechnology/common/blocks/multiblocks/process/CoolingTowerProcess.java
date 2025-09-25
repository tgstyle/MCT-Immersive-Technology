package mctmods.immersivetechnology.common.blocks.multiblocks.process;

import mctmods.immersivetechnology.common.blocks.multiblocks.logic.CoolingTowerLogic;
import mctmods.immersivetechnology.common.blocks.multiblocks.recipe.CoolingTowerRecipe;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

public class CoolingTowerProcess {
    private final CoolingTowerRecipe recipe;
    private int ticksProcessed = 0;

    public CoolingTowerProcess(CoolingTowerRecipe recipe) { this.recipe = recipe; }

    public void tick(CoolingTowerLogic.State state) {
        if (ticksProcessed >= recipe.totalProcessTime) return;
        int perTickIn0 = recipe.input0.getAmount() / recipe.totalProcessTime;
        int perTickIn1 = recipe.input1.getAmount() / recipe.totalProcessTime;
        int perTickOut0 = recipe.fluidOutput0.getAmount() / recipe.totalProcessTime;
        int perTickOut1 = recipe.fluidOutput1.getAmount() / recipe.totalProcessTime;
        int perTickOut2 = recipe.fluidOutput2.getAmount() / recipe.totalProcessTime;
        state.tanks.input0().drain(perTickIn0, FluidAction.EXECUTE);
        state.tanks.input1().drain(perTickIn1, FluidAction.EXECUTE);
        if (!recipe.fluidOutput0.isEmpty()) { state.tanks.output0().fill(new FluidStack(recipe.fluidOutput0.getFluid(), perTickOut0), FluidAction.EXECUTE); }
        if (!recipe.fluidOutput1.isEmpty()) { state.tanks.output1().fill(new FluidStack(recipe.fluidOutput1.getFluid(), perTickOut1), FluidAction.EXECUTE); }
        if (!recipe.fluidOutput2.isEmpty()) { state.tanks.output2().fill(new FluidStack(recipe.fluidOutput2.getFluid(), perTickOut2), FluidAction.EXECUTE); }
        ticksProcessed++;
        if (ticksProcessed == recipe.totalProcessTime) {
            int remIn0 = recipe.input0.getAmount() % recipe.totalProcessTime;
            int remIn1 = recipe.input1.getAmount() % recipe.totalProcessTime;
            int remOut0 = recipe.fluidOutput0.getAmount() % recipe.totalProcessTime;
            int remOut1 = recipe.fluidOutput1.getAmount() % recipe.totalProcessTime;
            int remOut2 = recipe.fluidOutput2.getAmount() % recipe.totalProcessTime;
            state.tanks.input0().drain(remIn0, FluidAction.EXECUTE);
            state.tanks.input1().drain(remIn1, FluidAction.EXECUTE);
            if (!recipe.fluidOutput0.isEmpty()) { state.tanks.output0().fill(new FluidStack(recipe.fluidOutput0.getFluid(), remOut0), FluidAction.EXECUTE); }
            if (!recipe.fluidOutput1.isEmpty()) { state.tanks.output1().fill(new FluidStack(recipe.fluidOutput1.getFluid(), remOut1), FluidAction.EXECUTE); }
            if (!recipe.fluidOutput2.isEmpty()) { state.tanks.output2().fill(new FluidStack(recipe.fluidOutput2.getFluid(), remOut2), FluidAction.EXECUTE); }
        }
    }

    public boolean isComplete() { return ticksProcessed >= recipe.totalProcessTime; }
}
