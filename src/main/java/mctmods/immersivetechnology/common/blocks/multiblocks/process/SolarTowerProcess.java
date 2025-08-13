package mctmods.immersivetechnology.common.blocks.multiblocks.process;

import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockLevel;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcessInMachine;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.ProcessContext;
import mctmods.immersivetechnology.common.blocks.multiblocks.logic.SolarTowerLogic;
import mctmods.immersivetechnology.common.blocks.multiblocks.recipe.SolarTowerRecipe;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

import java.util.function.BiFunction;

public class SolarTowerProcess extends MultiblockProcessInMachine<SolarTowerRecipe> {
    public SolarTowerProcess(SolarTowerRecipe recipe) { super(recipe); this.setInputTanks(0); }
    public SolarTowerProcess(BiFunction<Level, ResourceLocation, SolarTowerRecipe> getRecipe, CompoundTag data) { super(getRecipe, data); this.setInputTanks(0); }

    @Override
    public void doProcessTick(ProcessContext.ProcessContextInMachine<SolarTowerRecipe> context, IMultiblockLevel level) {
        SolarTowerLogic.State state = (SolarTowerLogic.State) context;
        if (state.heatLevel >= SolarTowerLogic.WORKING_HEAT_LEVEL) { this.processTick--; }
        else { this.processTick = Math.min(this.processTick + SolarTowerLogic.PROGRESS_LOSS_PER_TICK, getMaxTicks(level.getRawLevel())); }
        if (this.processTick <= 0) { processFinish(context, level); }
    }

    @Override
    public boolean canProcess(ProcessContext.ProcessContextInMachine<SolarTowerRecipe> context, Level level) {
        LevelDependentData<SolarTowerRecipe> levelData = getLevelData(level);
        if (levelData.recipe() == null) return true;
        IFluidTank inputTank = context.getInternalTanks()[0];
        return inputTank.drain(1, FluidAction.SIMULATE).getAmount() > 0;
    }

    @Override
    protected void processFinish(ProcessContext.ProcessContextInMachine<SolarTowerRecipe> context, IMultiblockLevel level) {
        SolarTowerRecipe recipe = getRecipe(level.getRawLevel());
        if (recipe != null) {
            FluidStack outputFluid = recipe.fluidOutput;
            if (outputFluid != null && !outputFluid.isEmpty()) { ((SolarTowerLogic.State) context).getTanks().output().fill(outputFluid.copy(), FluidAction.EXECUTE); }
            ((SolarTowerLogic.State) context).getTanks().input().drain(recipe.input.getAmount(), FluidAction.EXECUTE);
        }
    }
}
