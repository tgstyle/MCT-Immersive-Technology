package mctmods.immersivetechnology.common.multiblocks.metal.process;

import mctmods.immersivetechnology.common.fluids.helper.MarkableFluidTank;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.RadiatorRecipe;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;

public class RadiatorProcess {
    private final RadiatorRecipe recipe;
    private int ticksProcessed = 0;
    private double progressAccumulator = 0.0D;

    public RadiatorProcess(RadiatorRecipe recipe) {
        this.recipe = recipe;
    }

    public void tick(MarkableFluidTank output) {
        tick(output, 1.0D);
    }

    public void tick(MarkableFluidTank output, double speedMult) {
        if (ticksProcessed >= recipe.getTotalProcessTime()) return;

        progressAccumulator += Math.max(speedMult, 0.0D);
        int advance = (int) progressAccumulator;
        progressAccumulator -= advance;
        for (int i = 0; i < advance && ticksProcessed < recipe.getTotalProcessTime(); i++) {
            FluidStack outFluid = recipe.fluidOutput();
            if (outFluid != null && !outFluid.isEmpty()) {
                int perTickOut = outFluid.getAmount() / recipe.getTotalProcessTime();
                output.fill(new FluidStack(outFluid.getFluid(), perTickOut), FluidAction.EXECUTE);
            }

            ticksProcessed++;

            if (ticksProcessed == recipe.getTotalProcessTime()) {
                if (outFluid != null && !outFluid.isEmpty()) {
                    int remainder = outFluid.getAmount() % recipe.getTotalProcessTime();
                    if (remainder > 0) {
                        output.fill(new FluidStack(outFluid.getFluid(), remainder), FluidAction.EXECUTE);
                    }
                }
            }
        }
    }

    public boolean isComplete() {
        return ticksProcessed >= recipe.getTotalProcessTime();
    }

    public int getTicksProcessed() {
        return ticksProcessed;
    }

    public RadiatorRecipe getRecipe() {
        return recipe;
    }
}
