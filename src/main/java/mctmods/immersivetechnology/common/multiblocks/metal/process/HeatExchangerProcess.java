package mctmods.immersivetechnology.common.multiblocks.metal.process;

import mctmods.immersivetechnology.common.multiblocks.metal.logic.HeatExchangerLogic;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.HeatExchangerRecipe;
import mctmods.immersivetechnology.core.util.ProcessUtils;

import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockLevel;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcessInMachine;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.ProcessContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.IFluidTank;
import java.util.function.BiFunction;

public class HeatExchangerProcess extends MultiblockProcessInMachine<HeatExchangerRecipe> {
    public HeatExchangerProcess(HeatExchangerRecipe recipe) { super(recipe); }

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
        if (recipe.input1 == null || recipe.input1.getAmount() <= 0) { return ProcessUtils.drainFails(tanks[0], recipe.input0.getAmount(), recipe.input0::testIgnoringAmount); }
        return ProcessUtils.drainFails(tanks[0], recipe.input0.getAmount(), recipe.input0::testIgnoringAmount, tanks[1], recipe.input1.getAmount(), recipe.input1::testIgnoringAmount);
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
        ProcessUtils.fillOutput(tanks.output0(), recipe.output0);
        ProcessUtils.fillOutput(tanks.output1(), recipe.output1);
    }

    public int getCurrentTick() { return processTick; }
}
