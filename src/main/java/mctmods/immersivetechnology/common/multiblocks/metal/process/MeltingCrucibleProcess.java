package mctmods.immersivetechnology.common.multiblocks.metal.process;

import mctmods.immersivetechnology.common.multiblocks.metal.logic.MeltingCrucibleLogic;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.MeltingRecipe;
import mctmods.immersivetechnology.core.util.ProcessUtils;

import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockLevel;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcessInMachine;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.ProcessContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import java.util.function.BiFunction;

public class MeltingCrucibleProcess extends MultiblockProcessInMachine<MeltingRecipe> {
    public MeltingCrucibleProcess(MeltingRecipe recipe) { super(recipe); }

    public MeltingCrucibleProcess(BiFunction<Level, ResourceLocation, MeltingRecipe> getRecipe, CompoundTag data) { super(getRecipe, data); }

    @Override public void doProcessTick(ProcessContext.ProcessContextInMachine<MeltingRecipe> context, IMultiblockLevel level) {
        MeltingRecipe recipe = getLevelData(level.getRawLevel()).recipe();
        if (recipe == null || (this.processTick == 0 && ProcessUtils.drainFails(context.getInternalTanks()[0], recipe.input.getAmount(), recipe.input::testIgnoringAmount))) {
            this.clearProcess = true;
            return;
        }
        super.doProcessTick(context, level);
    }

    @Override public boolean canProcess(ProcessContext.ProcessContextInMachine<MeltingRecipe> context, Level level) {
        MeltingRecipe recipe = getLevelData(level).recipe();
        return recipe != null && ((MeltingCrucibleLogic.State) context).heatLevel >= recipe.requiredTemp;
    }

    @Override protected void processFinish(ProcessContext.ProcessContextInMachine<MeltingRecipe> context, IMultiblockLevel level) {
        super.processFinish(context, level);
        MeltingRecipe recipe = getRecipe(level.getRawLevel());
        if (recipe != null) { ProcessUtils.fillOutput(((MeltingCrucibleLogic.State) context).getTanks().output(), recipe.fluidOutput); }
    }
}
