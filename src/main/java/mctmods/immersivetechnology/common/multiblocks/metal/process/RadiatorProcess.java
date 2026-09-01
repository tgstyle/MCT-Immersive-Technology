package mctmods.immersivetechnology.common.multiblocks.metal.process;

import com.immersiveconvergence.api.util.MarkableFluidTank;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.RadiatorRecipe;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

public class RadiatorProcess {
    private final RadiatorRecipe recipe;
    private int ticksProcessed = 0;
    private double progressAccumulator = 0.0D;

    public RadiatorProcess(RadiatorRecipe recipe) {
        this.recipe = recipe;
    }

    public void tick(MarkableFluidTank input, MarkableFluidTank output) {
        tick(input, output, 1.0D);
    }

    public void tick(MarkableFluidTank input, MarkableFluidTank output, double speedMult) {
        if (ticksProcessed >= recipe.totalProcessTime) return;

        if (ticksProcessed == 0) {
            FluidStack drained = input.drain(recipe.input.getAmount(), FluidAction.EXECUTE);
            if (drained.getAmount() < recipe.input.getAmount() || !recipe.input.testIgnoringAmount(drained)) {
                ticksProcessed = recipe.totalProcessTime;
                return;
            }
        }

        progressAccumulator += Math.max(speedMult, 0.0D);
        int advance = (int) progressAccumulator;
        progressAccumulator -= advance;
        for (int i = 0; i < advance && ticksProcessed < recipe.totalProcessTime; i++) {
            int perTickOut = recipe.fluidOutput.getAmount() / recipe.totalProcessTime;
            if (!recipe.fluidOutput.isEmpty()) {
                output.fill(new FluidStack(recipe.fluidOutput.getFluid(), perTickOut), FluidAction.EXECUTE);
            }

            ticksProcessed++;

            if (ticksProcessed == recipe.totalProcessTime) {
                int remainder = recipe.fluidOutput.getAmount() % recipe.totalProcessTime;
                if (!recipe.fluidOutput.isEmpty() && remainder > 0) {
                    output.fill(new FluidStack(recipe.fluidOutput.getFluid(), remainder), FluidAction.EXECUTE);
                }
            }
        }
    }

    public boolean isComplete() {
        return ticksProcessed >= recipe.totalProcessTime;
    }

    public int getTicksProcessed() {
        return ticksProcessed;
    }

    public RadiatorRecipe getRecipe() {
        return recipe;
    }
}
