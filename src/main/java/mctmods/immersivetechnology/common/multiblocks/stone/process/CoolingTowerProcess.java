package mctmods.immersivetechnology.common.multiblocks.stone.process;

import mctmods.immersivetechnology.common.multiblocks.stone.logic.CoolingTowerLogic;
import mctmods.immersivetechnology.common.multiblocks.stone.recipe.CoolingTowerRecipe;
import mctmods.immersivetechnology.core.util.ProcessUtils;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;

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
        this.recipeInput0 = tank0.copyWithAmount(recipe.getInput0Amount());
        this.recipeInput1 = tank1.copyWithAmount(recipe.getInput1Amount());
    }

    @Nullable public static CoolingTowerProcess fromNBT(Level level, CompoundTag tag) {
        CompoundTag recipeTag = tag.getCompound("recipe");
        FluidStack input0 = FluidStack.parseOptional(level.registryAccess(), recipeTag.getCompound("input0"));
        FluidStack input1 = FluidStack.parseOptional(level.registryAccess(), recipeTag.getCompound("input1"));
        CoolingTowerRecipe recipe = CoolingTowerRecipe.findOriented(level, input0, input1, CoolingTowerRecipe::findRecipe, CoolingTowerRecipe::findRecipe);
        if (recipe == null) { return null; }
        CoolingTowerProcess process = new CoolingTowerProcess(recipe, input0, input1);
        process.ticksProcessed = tag.getInt("ticksProcessed");
        process.progressAccumulator = tag.getDouble("progressAccumulator");
        process.inputDrained = tag.getBoolean("inputDrained");
        return process;
    }

    public CompoundTag toNBT(HolderLookup.Provider provider) {
        CompoundTag recipeTag = new CompoundTag();
        recipeTag.put("input0", recipeInput0.saveOptional(provider));
        recipeTag.put("input1", recipeInput1.saveOptional(provider));
        CompoundTag tag = new CompoundTag();
        tag.put("recipe", recipeTag);
        tag.putInt("ticksProcessed", ticksProcessed);
        tag.putDouble("progressAccumulator", progressAccumulator);
        tag.putBoolean("inputDrained", inputDrained);
        return tag;
    }

    public void tick(CoolingTowerLogic.State state, double speedMult) {
        int total = recipe.getTotalProcessTime();
        if (ticksProcessed >= total) { return; }
        if (!inputDrained) {
            inputDrained = true;
            if (ProcessUtils.drainFails(state.tanks.input0(), recipe.getInput0Amount(), fluid -> fluid.is(recipe.inputTag0()), state.tanks.input1(), recipe.getInput1Amount(), fluid -> fluid.is(recipe.inputTag1()))) {
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
            ProcessUtils.fillShare(state.tanks.output0(), recipe.fluidOutput0(), total, last);
            ProcessUtils.fillShare(state.tanks.output1(), recipe.fluidOutput1(), total, last);
            ProcessUtils.fillShare(state.tanks.output2(), recipe.fluidOutput2(), total, last);
        }
    }

    public boolean isComplete() { return ticksProcessed >= recipe.getTotalProcessTime(); }

    public int getTicksProcessed() { return ticksProcessed; }

    public CoolingTowerRecipe getRecipe() { return recipe; }
}
