package mctmods.immersivetechnology.common.multiblocks.metal.process;

import mctmods.immersivetechnology.common.multiblocks.metal.logic.DistillerLogic;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.DistillerRecipe;
import mctmods.immersivetechnology.core.util.ItemOutputs;
import mctmods.immersivetechnology.core.util.ProcessUtils;

import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockLevel;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcessInMachine;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.ProcessContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import java.util.function.BiFunction;

public class DistillerProcess extends MultiblockProcessInMachine<DistillerRecipe> {
    public DistillerProcess(DistillerRecipe recipe) { super(recipe); }

    public DistillerProcess(BiFunction<Level, ResourceLocation, DistillerRecipe> getRecipe, CompoundTag data) { super(getRecipe, data); }

    @Override public void doProcessTick(ProcessContext.ProcessContextInMachine<DistillerRecipe> context, IMultiblockLevel level) {
        DistillerRecipe recipe = getLevelData(level.getRawLevel()).recipe();
        if (recipe == null || (this.processTick == 0 && ProcessUtils.drainFails(context.getInternalTanks()[0], recipe.input.getAmount(), recipe.input::testIgnoringAmount))) {
            this.clearProcess = true;
            return;
        }
        super.doProcessTick(context, level);
    }

    @Override public boolean canProcess(ProcessContext.ProcessContextInMachine<DistillerRecipe> context, Level level) {
        LevelDependentData<DistillerRecipe> levelData = getLevelData(level);
        if (levelData.recipe() == null) { return true; }
        return context.getEnergy().extractEnergy(levelData.energyPerTick(), true) == levelData.energyPerTick();
    }

    @Override protected void processFinish(ProcessContext.ProcessContextInMachine<DistillerRecipe> context, IMultiblockLevel level) {
        super.processFinish(context, level);
        DistillerRecipe recipe = getRecipe(level.getRawLevel());
        if (recipe == null) { return; }
        ProcessUtils.fillOutput(((DistillerLogic.State) context).getTanks().output(), recipe.fluidOutput);
        if (!recipe.itemOutput.isEmpty() && level.getRawLevel().random.nextFloat() < recipe.chance) { ItemOutputs.merge(context.getInventory(), DistillerLogic.OUTPUT_SLOT, recipe.itemOutput.copy()); }
    }
}
