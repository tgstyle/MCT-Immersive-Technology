package mctmods.immersivetechnology.common.multiblocks.stone.process;

import mctmods.immersivetechnology.api.crafting.CoolingTowerRecipe;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

import javax.annotation.Nullable;

public class CoolingTowerProcess {
    private final CoolingTowerRecipe recipe;
    private final boolean swapped;
    private int ticksProcessed = 0;
    private double progressAccumulator = 0;
    private boolean inputsDrained = false;

    public CoolingTowerProcess(CoolingTowerRecipe recipe, boolean swapped) {
        this.recipe = recipe;
        this.swapped = swapped;
    }

    public void tick(FluidTank[] tanks, double speedMult) {
        if (isComplete()) { return; }
        FluidTank inputA = tanks[swapped ? 1 : 0];
        FluidTank inputB = tanks[swapped ? 0 : 1];
        int total = recipe.getTotalProcessTime();
        if (!inputsDrained) {
            inputsDrained = true;
            FluidStack drainedA = inputA.drain(recipe.fluidInput0.amount, true);
            FluidStack drainedB = inputB.drain(recipe.fluidInput1.amount, true);
            if (missing(drainedA, recipe.fluidInput0) || missing(drainedB, recipe.fluidInput1)) {
                ticksProcessed = total;
                return;
            }
        }
        progressAccumulator += Math.max(speedMult, 0);
        int advance = (int)progressAccumulator;
        progressAccumulator -= advance;
        for (int i = 0; i < advance && ticksProcessed < total; i++) {
            fill(tanks[2], recipe.fluidOutput0, recipe.fluidOutput0 == null ? 0 : recipe.fluidOutput0.amount / total);
            fill(tanks[3], recipe.fluidOutput1, recipe.fluidOutput1 == null ? 0 : recipe.fluidOutput1.amount / total);
            fill(tanks[4], recipe.fluidOutput2, recipe.fluidOutput2 == null ? 0 : recipe.fluidOutput2.amount / total);
            ticksProcessed++;
            if (ticksProcessed == total) {
                fill(tanks[2], recipe.fluidOutput0, recipe.fluidOutput0 == null ? 0 : recipe.fluidOutput0.amount % total);
                fill(tanks[3], recipe.fluidOutput1, recipe.fluidOutput1 == null ? 0 : recipe.fluidOutput1.amount % total);
                fill(tanks[4], recipe.fluidOutput2, recipe.fluidOutput2 == null ? 0 : recipe.fluidOutput2.amount % total);
            }
        }
    }

    private static boolean missing(@Nullable FluidStack drained, FluidStack wanted) {
        return drained == null || drained.amount < wanted.amount || !drained.isFluidEqual(wanted);
    }

    private static void fill(FluidTank tank, @Nullable FluidStack output, int amount) {
        if (output == null || output.getFluid() == null || amount <= 0) { return; }
        tank.fillInternal(new FluidStack(output.getFluid(), amount), true);
    }

    public boolean isComplete() { return ticksProcessed >= recipe.getTotalProcessTime(); }

    public int getTicksProcessed() { return ticksProcessed; }

    public int getTotalProcessTime() { return recipe.getTotalProcessTime(); }

    public CoolingTowerRecipe getRecipe() { return recipe; }

    public NBTTagCompound writeToNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setTag("recipe", recipe.writeToNBT(new NBTTagCompound()));
        tag.setBoolean("swapped", swapped);
        tag.setInteger("ticksProcessed", ticksProcessed);
        tag.setDouble("progressAccumulator", progressAccumulator);
        tag.setBoolean("inputsDrained", inputsDrained);
        return tag;
    }

    @Nullable public static CoolingTowerProcess readFromNBT(NBTTagCompound tag) {
        CoolingTowerRecipe recipe = CoolingTowerRecipe.loadFromNBT(tag.getCompoundTag("recipe"));
        if (recipe == null) { return null; }
        CoolingTowerProcess process = new CoolingTowerProcess(recipe, tag.getBoolean("swapped"));
        process.ticksProcessed = tag.getInteger("ticksProcessed");
        process.progressAccumulator = tag.getDouble("progressAccumulator");
        process.inputsDrained = tag.getBoolean("inputsDrained");
        return process;
    }
}
