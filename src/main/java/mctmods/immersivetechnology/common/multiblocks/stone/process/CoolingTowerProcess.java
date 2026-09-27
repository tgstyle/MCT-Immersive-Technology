package mctmods.immersivetechnology.common.multiblocks.stone.process;

import mctmods.immersivetechnology.common.multiblocks.stone.logic.CoolingTowerLogic;
import mctmods.immersivetechnology.common.multiblocks.stone.recipe.CoolingTowerRecipe;
import mctmods.immersivetechnology.core.util.ProcessUtils;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;

public class CoolingTowerProcess {
    private final CoolingTowerRecipe recipe;
    private final FluidStack recipeInput0;
    private final FluidStack recipeInput1;
    private int ticksProcessed = 0;
    private double progressAccumulator = 0.0D;
    private boolean inputDrained = false;

    public CoolingTowerProcess(CoolingTowerRecipe recipe, FluidStack tank0, FluidStack tank1) {
        this.recipe = recipe;
        this.recipeInput0 = new FluidStack(tank0, recipe.input0.getAmount());
        this.recipeInput1 = new FluidStack(tank1, recipe.input1.getAmount());
    }

    @Nullable public static CoolingTowerProcess fromNBT(Level level, CompoundTag tag) {
        CompoundTag recipeTag = tag.getCompound("recipe");
        FluidStack input0 = FluidStack.loadFluidStackFromNBT(recipeTag.getCompound("input0"));
        FluidStack input1 = FluidStack.loadFluidStackFromNBT(recipeTag.getCompound("input1"));
        CoolingTowerRecipe recipe = CoolingTowerRecipe.findOriented(level, input0, input1, CoolingTowerRecipe::findRecipe, CoolingTowerRecipe::findRecipe);
        if (recipe == null) { return null; }
        CoolingTowerProcess process = new CoolingTowerProcess(recipe, input0, input1);
        process.ticksProcessed = tag.getInt("ticksProcessed");
        process.progressAccumulator = tag.getDouble("progressAccumulator");
        process.inputDrained = tag.getBoolean("inputDrained");
        return process;
    }

    public CompoundTag toNBT() {
        CompoundTag recipeTag = new CompoundTag();
        recipeTag.put("input0", recipeInput0.writeToNBT(new CompoundTag()));
        recipeTag.put("input1", recipeInput1.writeToNBT(new CompoundTag()));
        CompoundTag tag = new CompoundTag();
        tag.put("recipe", recipeTag);
        tag.putInt("ticksProcessed", ticksProcessed);
        tag.putDouble("progressAccumulator", progressAccumulator);
        tag.putBoolean("inputDrained", inputDrained);
        return tag;
    }

    public void tick(CoolingTowerLogic.State state, double speedMult) {
        int total = recipe.totalProcessTime;
        if (ticksProcessed >= total) { return; }
        if (!inputDrained) {
            inputDrained = true;
            if (ProcessUtils.drainFails(state.tanks.input0(), recipe.input0.getAmount(), recipe.input0::testIgnoringAmount, state.tanks.input1(), recipe.input1.getAmount(), recipe.input1::testIgnoringAmount)) {
                ticksProcessed = total;
                return;
            }
        }
        progressAccumulator += Math.max(speedMult, 0.0D);
        int advance = (int) progressAccumulator;
        progressAccumulator -= advance;
        for (int i = 0; i < advance && ticksProcessed < total; i++) {
            ticksProcessed++;
            boolean last = ticksProcessed == total;
            ProcessUtils.fillShare(state.tanks.output0(), recipe.fluidOutput0, total, last);
            ProcessUtils.fillShare(state.tanks.output1(), recipe.fluidOutput1, total, last);
            ProcessUtils.fillShare(state.tanks.output2(), recipe.fluidOutput2, total, last);
        }
    }

    public boolean isComplete() { return ticksProcessed >= recipe.totalProcessTime; }

    public int getTicksProcessed() { return ticksProcessed; }

    public CoolingTowerRecipe getRecipe() { return recipe; }
}
