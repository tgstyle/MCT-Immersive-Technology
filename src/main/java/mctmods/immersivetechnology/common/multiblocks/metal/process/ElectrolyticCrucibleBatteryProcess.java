package mctmods.immersivetechnology.common.multiblocks.metal.process;

import mctmods.immersivetechnology.common.multiblocks.metal.logic.ElectrolyticCrucibleBatteryLogic;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.ElectrolyticCrucibleBatteryRecipe;
import mctmods.immersivetechnology.core.util.ItemOutputs;
import mctmods.immersivetechnology.core.util.ProcessUtils;

import com.immersiveconvergence.api.crafting.ICFluidTagInput;
import com.immersiveconvergence.api.multiblock.blocks.env.IMultiblockLevel;
import com.immersiveconvergence.api.multiblock.process.MultiblockProcessInMachine;
import com.immersiveconvergence.api.multiblock.process.ProcessContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import java.util.List;
import java.util.function.BiFunction;

public class ElectrolyticCrucibleBatteryProcess extends MultiblockProcessInMachine<ElectrolyticCrucibleBatteryRecipe> {
    public ElectrolyticCrucibleBatteryProcess(ElectrolyticCrucibleBatteryRecipe recipe) {
        super(recipe);
        setInputTanks(0);
    }

    public ElectrolyticCrucibleBatteryProcess(BiFunction<Level, ResourceLocation, ElectrolyticCrucibleBatteryRecipe> getRecipe, CompoundTag data) { super(getRecipe, data); }

    @Override protected List<ICFluidTagInput> getRecipeFluidInputs(Level level) {
        ElectrolyticCrucibleBatteryRecipe recipe = getLevelData(level).recipe();
        return recipe == null ? List.of() : List.of(recipe.fluidInput0);
    }

    @Override public void doProcessTick(ProcessContext.ProcessContextInMachine<ElectrolyticCrucibleBatteryRecipe> context, IMultiblockLevel level) {
        if (getLevelData(level.getRawLevel()).recipe() == null) {
            this.clearProcess = true;
            return;
        }
        super.doProcessTick(context, level);
    }

    @Override public boolean canProcess(ProcessContext.ProcessContextInMachine<ElectrolyticCrucibleBatteryRecipe> context, Level level) {
        ElectrolyticCrucibleBatteryRecipe recipe = getLevelData(level).recipe();
        if (recipe == null) { return true; }
        int energyPerTick = (int) Math.floor((float) recipe.getTotalProcessEnergy() / recipe.getTotalProcessTime());
        return context.getEnergy().extractEnergy(energyPerTick, true) == energyPerTick;
    }

    @Override protected void processFinish(ProcessContext.ProcessContextInMachine<ElectrolyticCrucibleBatteryRecipe> context, IMultiblockLevel level) {
        super.processFinish(context, level);
        ElectrolyticCrucibleBatteryRecipe recipe = getRecipe(level.getRawLevel());
        if (recipe == null) { return; }
        ElectrolyticCrucibleBatteryLogic.ElectrolyticCrucibleBatteryTanks tanks = ((ElectrolyticCrucibleBatteryLogic.State) context).getTanks();
        ProcessUtils.fillOutput(tanks.output0(), recipe.fluidOutput0);
        ProcessUtils.fillOutput(tanks.output1(), recipe.fluidOutput1);
        ProcessUtils.fillOutput(tanks.output2(), recipe.fluidOutput2);
        if (!recipe.itemOutput.isEmpty()) { ItemOutputs.merge(context.getInventory(), 0, recipe.itemOutput.copy()); }
    }
}
