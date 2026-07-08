package mctmods.immersivetechnology.common.multiblocks.metal.process;

import mctmods.immersivetechnology.common.multiblocks.metal.logic.RadiatorHorizontalLogic;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.RadiatorRecipe;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;

public class RadiatorHorizontalProcess {
    private final RadiatorRecipe recipe;
    private int ticksProcessed = 0;

    public RadiatorHorizontalProcess(RadiatorRecipe recipe) {
        this.recipe = recipe;
    }

    public void tick(RadiatorHorizontalLogic.State state) {
        tick(state, 1.0D);
    }

    public void tick(RadiatorHorizontalLogic.State state, double speedMult) {
        if (ticksProcessed >= recipe.getTotalProcessTime()) return;

        int advance = (int) Math.max(1, speedMult);
        for (int i = 0; i < advance && ticksProcessed < recipe.getTotalProcessTime(); i++) {
            FluidStack outFluid = recipe.fluidOutput();
            if (outFluid != null && !outFluid.isEmpty()) {
                int perTickOut = outFluid.getAmount() / recipe.getTotalProcessTime();
                state.tanks.output().fill(new FluidStack(outFluid.getFluid(), perTickOut), FluidAction.EXECUTE);
            }

            ticksProcessed++;

            if (ticksProcessed == recipe.getTotalProcessTime()) {
                if (outFluid != null && !outFluid.isEmpty()) {
                    int remainder = outFluid.getAmount() % recipe.getTotalProcessTime();
                    if (remainder > 0) {
                        state.tanks.output().fill(new FluidStack(outFluid.getFluid(), remainder), FluidAction.EXECUTE);
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
