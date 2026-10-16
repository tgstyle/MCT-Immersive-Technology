package mctmods.immersivetechnology.common.multiblocks.metal.process;

import mctmods.immersivetechnology.common.multiblocks.metal.logic.HeatExchangerLogic;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.HeatExchangerRecipe;
import mctmods.immersivetechnology.core.util.ProcessUtils;

import com.immersiveconvergence.api.multiblock.blocks.env.IMultiblockLevel;
import com.immersiveconvergence.api.multiblock.process.MultiblockProcessInMachine;
import com.immersiveconvergence.api.multiblock.process.ProcessContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.IFluidTank;
import java.util.function.BiFunction;

public class HeatExchangerProcess extends MultiblockProcessInMachine<HeatExchangerRecipe> {
    public HeatExchangerProcess(RecipeHolder<HeatExchangerRecipe> recipeHolder) { super(recipeHolder, 0); }

    public HeatExchangerProcess(BiFunction<Level, ResourceLocation, HeatExchangerRecipe> getRecipe, CompoundTag data) { super(getRecipe, data); }

    @Override public void doProcessTick(ProcessContext.ProcessContextInMachine<HeatExchangerRecipe> context, IMultiblockLevel level) {
        HeatExchangerRecipe recipe = getLevelData(level.getRawLevel()).recipe();
        if (recipe == null || (this.processTick == 0 && drainFails(context, recipe))) {
            this.clearProcess = true;
            return;
        }
        super.doProcessTick(context, level);
    }

    private static boolean drainFails(ProcessContext.ProcessContextInMachine<HeatExchangerRecipe> context, HeatExchangerRecipe recipe) {
        IFluidTank[] tanks = context.getInternalTanks();
        if (recipe.input1Tag() == null || recipe.getInput1Amount() <= 0) { return ProcessUtils.drainFails(tanks[0], recipe.getInput0Amount(), recipe::matchesInput0); }
        return ProcessUtils.drainFails(tanks[0], recipe.getInput0Amount(), recipe::matchesInput0, tanks[1], recipe.getInput1Amount(), recipe::matchesInput1);
    }

    @Override public boolean canProcess(ProcessContext.ProcessContextInMachine<HeatExchangerRecipe> context, Level level) {
        LevelDependentData<HeatExchangerRecipe> levelData = getLevelData(level);
        if (levelData.recipe() == null) { return true; }
        return context.getEnergy().extractEnergy(levelData.energyPerTick(), true) == levelData.energyPerTick();
    }

    @Override protected void processFinish(ProcessContext.ProcessContextInMachine<HeatExchangerRecipe> context, IMultiblockLevel level) {
        super.processFinish(context, level);
        HeatExchangerRecipe recipe = getRecipe(level.getRawLevel());
        if (recipe == null) { return; }
        HeatExchangerLogic.HeatExchangerTanks tanks = ((HeatExchangerLogic.State) context).tanks;
        ProcessUtils.fillOutput(tanks.output0(), recipe.output0());
        ProcessUtils.fillOutput(tanks.output1(), recipe.output1());
    }

    public int getCurrentTick() { return processTick; }
}
