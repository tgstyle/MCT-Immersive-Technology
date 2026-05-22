package mctmods.immersivetechnology.common.multiblocks.metal.process;

import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockLevel;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcessInMachine;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.ProcessContext;
import mctmods.immersivetechnology.common.multiblocks.metal.logic.ElectrolyticCrucibleBatteryLogic;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.ElectrolyticCrucibleBatteryRecipe;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

import java.util.function.BiFunction;

public class ElectrolyticCrucibleBatteryProcess extends MultiblockProcessInMachine<ElectrolyticCrucibleBatteryRecipe> {
    public ElectrolyticCrucibleBatteryProcess(ElectrolyticCrucibleBatteryRecipe recipe) {
        super(recipe);
        this.setInputTanks(0);
    }

    public ElectrolyticCrucibleBatteryProcess(BiFunction<Level, ResourceLocation, ElectrolyticCrucibleBatteryRecipe> getRecipe, CompoundTag data) {
        super(getRecipe, data);
        this.setInputTanks(0);
    }

    @Override public void doProcessTick(ProcessContext.ProcessContextInMachine<ElectrolyticCrucibleBatteryRecipe> context, IMultiblockLevel level) {
        LevelDependentData<ElectrolyticCrucibleBatteryRecipe> levelData = getLevelData(level.getRawLevel());
        if (levelData.recipe() == null) { this.clearProcess = true; return; }
        if (this.processTick == 0) {
            IFluidTank inputTank = context.getInternalTanks()[0];
            int amount = levelData.recipe().fluidInput0.getAmount();
            FluidStack drained = inputTank.drain(amount, FluidAction.SIMULATE);
            if (drained.getAmount() < amount || !levelData.recipe().fluidInput0.testIgnoringAmount(drained)) { this.clearProcess = true; return; }
            inputTank.drain(amount, FluidAction.EXECUTE);
        }
        super.doProcessTick(context, level);
    }

    @Override public boolean canProcess(ProcessContext.ProcessContextInMachine<ElectrolyticCrucibleBatteryRecipe> context, Level level) {
        LevelDependentData<ElectrolyticCrucibleBatteryRecipe> levelData = getLevelData(level);
        if (levelData.recipe() == null) { return true; }
        int energyPerTick = (int) Math.floor((float) levelData.recipe().getTotalProcessEnergy() / levelData.recipe().getTotalProcessTime());
        return context.getEnergy().extractEnergy(energyPerTick, true) == energyPerTick;
    }

    @Override protected void processFinish(ProcessContext.ProcessContextInMachine<ElectrolyticCrucibleBatteryRecipe> context, IMultiblockLevel level) {
        super.processFinish(context, level);
        ElectrolyticCrucibleBatteryRecipe recipe = getRecipe(level.getRawLevel());
        if (recipe == null) { return; }

        ElectrolyticCrucibleBatteryLogic.State state = (ElectrolyticCrucibleBatteryLogic.State) context;

        if (recipe.fluidOutput0 != null && !recipe.fluidOutput0.isEmpty()) { state.getTanks().output0().fill(recipe.fluidOutput0.copy(), FluidAction.EXECUTE); }
        if (recipe.fluidOutput1 != null && !recipe.fluidOutput1.isEmpty()) { state.getTanks().output1().fill(recipe.fluidOutput1.copy(), FluidAction.EXECUTE); }
        if (recipe.fluidOutput2 != null && !recipe.fluidOutput2.isEmpty()) { state.getTanks().output2().fill(recipe.fluidOutput2.copy(), FluidAction.EXECUTE); }
    }
}
