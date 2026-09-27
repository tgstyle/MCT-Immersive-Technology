package mctmods.immersivetechnology.common.multiblocks.metal.process;

import mctmods.immersivetechnology.common.multiblocks.metal.logic.ElectrolyticCrucibleBatteryLogic;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.ElectrolyticCrucibleBatteryRecipe;
import mctmods.immersivetechnology.core.util.ItemOutputs;
import mctmods.immersivetechnology.core.util.ProcessUtils;

import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockLevel;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcessInMachine;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.ProcessContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import java.util.function.BiFunction;

public class ElectrolyticCrucibleBatteryProcess extends MultiblockProcessInMachine<ElectrolyticCrucibleBatteryRecipe> {
    public ElectrolyticCrucibleBatteryProcess(ElectrolyticCrucibleBatteryRecipe recipe) { super(recipe); }

    public ElectrolyticCrucibleBatteryProcess(BiFunction<Level, ResourceLocation, ElectrolyticCrucibleBatteryRecipe> getRecipe, CompoundTag data) { super(getRecipe, data); }

    @Override public void doProcessTick(ProcessContext.ProcessContextInMachine<ElectrolyticCrucibleBatteryRecipe> context, IMultiblockLevel level) {
        ElectrolyticCrucibleBatteryRecipe recipe = getLevelData(level.getRawLevel()).recipe();
        if (recipe == null || (this.processTick == 0 && ProcessUtils.drainFails(context.getInternalTanks()[0], recipe.fluidInput0.getAmount(), recipe.fluidInput0::testIgnoringAmount))) {
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
