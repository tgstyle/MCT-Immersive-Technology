package mctmods.immersivetechnology.common.multiblocks.metal.process;

import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockLevel;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcessInMachine;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.ProcessContext;
import mctmods.immersivetechnology.common.multiblocks.metal.logic.MeltingCrucibleLogic;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.MeltingRecipe;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;

import java.util.function.BiFunction;

public class MeltingCrucibleProcess extends MultiblockProcessInMachine<MeltingRecipe> {

    public MeltingCrucibleProcess(RecipeHolder<MeltingRecipe> recipeHolder) {
        super(recipeHolder, 0);
        this.setInputTanks(0);
    }

    public MeltingCrucibleProcess(BiFunction<Level, ResourceLocation, MeltingRecipe> getRecipe, CompoundTag data) {
        super(getRecipe, data);
        this.setInputTanks(0);
    }

    public MeltingCrucibleProcess(BiFunction<Level, ResourceLocation, MeltingRecipe> getRecipe, CompoundTag data, HolderLookup.Provider ignoredProvider) {
        this(getRecipe, data);
    }

    @Override public void doProcessTick(ProcessContext.ProcessContextInMachine<MeltingRecipe> context, IMultiblockLevel level) {
        LevelDependentData<MeltingRecipe> levelData = getLevelData(level.getRawLevel());
        if (levelData.recipe() == null) { this.clearProcess = true; return; }
        if (this.processTick == 0) {
            IFluidTank inputTank = context.getInternalTanks()[0];
            int amount = levelData.recipe().getInputAmount();
            FluidStack drained = inputTank.drain(amount, FluidAction.SIMULATE);
            if (drained.getAmount() < amount || !levelData.recipe().matches(drained)) { this.clearProcess = true; return; }
            inputTank.drain(amount, FluidAction.EXECUTE);
        }
        super.doProcessTick(context, level);
    }

    @Override public boolean canProcess(ProcessContext.ProcessContextInMachine<MeltingRecipe> context, Level level) {
        LevelDependentData<MeltingRecipe> levelData = getLevelData(level);
        if (levelData.recipe() == null) { return false; }
        MeltingCrucibleLogic.State state = (MeltingCrucibleLogic.State) context;
        return state.heatLevel >= levelData.recipe().requiredTemp;
    }

    @Override protected void processFinish(ProcessContext.ProcessContextInMachine<MeltingRecipe> context, IMultiblockLevel level) {
        super.processFinish(context, level);
        MeltingRecipe recipe = getRecipe(level.getRawLevel());
        if (recipe != null && recipe.fluidOutput != null && !recipe.fluidOutput.isEmpty()) {
            ((MeltingCrucibleLogic.State) context).getTanks().output().fill(recipe.fluidOutput.copy(), FluidAction.EXECUTE);
        }
    }
}
