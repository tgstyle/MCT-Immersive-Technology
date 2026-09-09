package mctmods.immersivetechnology.common.multiblocks.metal.process;

import mctmods.immersivetechnology.api.crafting.RadiatorRecipe;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

import javax.annotation.Nullable;

public class RadiatorProcess {
    private final RadiatorRecipe recipe;
    private int ticksProcessed = 0;
    private double progressAccumulator = 0;
    private boolean inputDrained = false;

    public RadiatorProcess(RadiatorRecipe recipe) { this.recipe = recipe; }

    public void tick(FluidTank input, FluidTank output, double speedMult) {
        if (isComplete()) { return; }
        if (!inputDrained) {
            inputDrained = true;
            FluidStack drained = input.drain(recipe.fluidInput.amount, true);
            if (drained == null || drained.amount < recipe.fluidInput.amount || !drained.isFluidEqual(recipe.fluidInput)) {
                ticksProcessed = recipe.getTotalProcessTime();
                return;
            }
        }
        progressAccumulator += Math.max(speedMult, 0);
        int advance = (int)progressAccumulator;
        progressAccumulator -= advance;
        int total = recipe.getTotalProcessTime();
        for (int i = 0; i < advance && ticksProcessed < total; i++) {
            if (recipe.fluidOutput != null && recipe.fluidOutput.getFluid() != null) {
                int perTick = recipe.fluidOutput.amount / total;
                if (perTick > 0) { output.fillInternal(new FluidStack(recipe.fluidOutput.getFluid(), perTick), true); }
            }
            ticksProcessed++;
            if (ticksProcessed == total && recipe.fluidOutput != null && recipe.fluidOutput.getFluid() != null) {
                int remainder = recipe.fluidOutput.amount % total;
                if (remainder > 0) { output.fillInternal(new FluidStack(recipe.fluidOutput.getFluid(), remainder), true); }
            }
        }
    }

    public boolean isComplete() { return ticksProcessed >= recipe.getTotalProcessTime(); }

    public int getTicksProcessed() { return ticksProcessed; }

    public int getTotalProcessTime() { return recipe.getTotalProcessTime(); }

    public RadiatorRecipe getRecipe() { return recipe; }

    public NBTTagCompound writeToNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setTag("recipe", recipe.writeToNBT(new NBTTagCompound()));
        tag.setInteger("ticksProcessed", ticksProcessed);
        tag.setDouble("progressAccumulator", progressAccumulator);
        tag.setBoolean("inputDrained", inputDrained);
        return tag;
    }

    @Nullable public static RadiatorProcess readFromNBT(NBTTagCompound tag) {
        RadiatorRecipe recipe = RadiatorRecipe.loadFromNBT(tag.getCompoundTag("recipe"));
        if (recipe == null) { return null; }
        RadiatorProcess process = new RadiatorProcess(recipe);
        process.ticksProcessed = tag.getInteger("ticksProcessed");
        process.progressAccumulator = tag.getDouble("progressAccumulator");
        process.inputDrained = tag.getBoolean("inputDrained");
        return process;
    }
}
