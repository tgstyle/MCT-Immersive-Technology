package mctmods.immersivetechnology.common.multiblocks.metal.process;

import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockLevel;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcessInMachine;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.ProcessContext;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.HeatExchangerRecipe;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.IFluidTank;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public class HeatExchangerProcess extends MultiblockProcessInMachine<HeatExchangerRecipe> {
    public HeatExchangerProcess(RecipeHolder<HeatExchangerRecipe> recipeHolder) {
        super(recipeHolder, 0);
        this.setInputTanks(0, 1);
    }

    public HeatExchangerProcess(BiFunction<Level, ResourceLocation, HeatExchangerRecipe> getRecipe, CompoundTag data) {
        super(getRecipe, data);
        this.setInputTanks(0, 1);
    }

    public HeatExchangerProcess(BiFunction<Level, ResourceLocation, HeatExchangerRecipe> getRecipe, CompoundTag data, HolderLookup.Provider ignoredProvider) {
        this(getRecipe, data);
    }

    @Override public void doProcessTick(ProcessContext.ProcessContextInMachine<HeatExchangerRecipe> context, IMultiblockLevel level) {
        LevelDependentData<HeatExchangerRecipe> levelData = getLevelData(level.getRawLevel());
        if (levelData.recipe() == null) { this.clearProcess = true; return; }
        if (this.processTick == 0) {
            IFluidTank inputTank0 = context.getInternalTanks()[0];
            int amount0 = levelData.recipe().getInput0Amount();
            FluidStack drained0 = inputTank0.drain(amount0, FluidAction.SIMULATE);
            if (drained0.getAmount() < amount0 || !levelData.recipe().matchesInput0(drained0)) { this.clearProcess = true; return; }
            inputTank0.drain(amount0, FluidAction.EXECUTE);

            if (levelData.recipe().input1Tag() != null && levelData.recipe().getInput1Amount() > 0) {
                IFluidTank inputTank1 = context.getInternalTanks()[1];
                int amount1 = levelData.recipe().getInput1Amount();
                FluidStack drained1 = inputTank1.drain(amount1, FluidAction.SIMULATE);
                if (drained1.getAmount() < amount1 || !levelData.recipe().matchesInput1(drained1)) { this.clearProcess = true; return; }
                inputTank1.drain(amount1, FluidAction.EXECUTE);
            }
        }
        super.doProcessTick(context, level);
    }

    @Override public boolean canProcess(ProcessContext.ProcessContextInMachine<HeatExchangerRecipe> context, Level level) {
        LevelDependentData<HeatExchangerRecipe> levelData = getLevelData(level);
        if (levelData.recipe() == null) return true;
        return context.getEnergy().extractEnergy(levelData.energyPerTick(), true) == levelData.energyPerTick();
    }

    @Override protected List<FluidStack> getRecipeFluidOutputs(Level level) {
        HeatExchangerRecipe recipe = getRecipe(level);
        if (recipe == null) { return List.of(); }
        List<FluidStack> list = new ArrayList<>();
        if (recipe.output0() != null && !recipe.output0().isEmpty()) list.add(recipe.output0().copy());
        FluidStack out1 = recipe.output1();
        if (out1 != null && !out1.isEmpty()) list.add(out1.copy());
        return list;
    }

    public int getCurrentTick() { return processTick; }
}
