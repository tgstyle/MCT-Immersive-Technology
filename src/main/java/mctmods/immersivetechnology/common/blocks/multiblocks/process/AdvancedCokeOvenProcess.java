package mctmods.immersivetechnology.common.blocks.multiblocks.process;

import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockLevel;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcessInMachine;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.ProcessContext;
import mctmods.immersivetechnology.common.blocks.multiblocks.helper.ITFurnaceHandler;
import mctmods.immersivetechnology.common.blocks.multiblocks.recipe.AdvancedCokeOvenRecipe;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.function.BiFunction;

public class AdvancedCokeOvenProcess extends MultiblockProcessInMachine<AdvancedCokeOvenRecipe> {
    private final int maxProcessTime;

    public AdvancedCokeOvenProcess(AdvancedCokeOvenRecipe recipe) { super(recipe, 0); this.maxProcessTime = recipe.getTotalProcessTime(); }

    public AdvancedCokeOvenProcess(BiFunction<Level, ResourceLocation, AdvancedCokeOvenRecipe> getRecipe, CompoundTag data) { super(getRecipe, data); this.maxProcessTime = data.getInt("maxProcessTime"); }

    @Override
    public void writeExtraDataToNBT(CompoundTag nbt) { super.writeExtraDataToNBT(nbt); nbt.putInt("maxProcessTime", maxProcessTime); }

    @Override
    public void doProcessTick(ProcessContext.ProcessContextInMachine<AdvancedCokeOvenRecipe> context, IMultiblockLevel level) {
        if (getRecipe(level.getRawLevel()) == null) { this.clearProcess = true; return; }
        @SuppressWarnings("unchecked")
        ITFurnaceHandler.IFurnaceEnvironment<AdvancedCokeOvenRecipe> env = (ITFurnaceHandler.IFurnaceEnvironment<AdvancedCokeOvenRecipe>) context;
        int speed = env.getProcessSpeed(level);
        this.processTick += speed;
        if (this.processTick >= this.maxProcessTime) { this.clearProcess = true; }
    }

    @Override
    public boolean canProcess(ProcessContext.ProcessContextInMachine<AdvancedCokeOvenRecipe> context, Level level) { return true; }

    @Override
    protected void processFinish(ProcessContext.ProcessContextInMachine<AdvancedCokeOvenRecipe> context, IMultiblockLevel level) {
        super.processFinish(context, level);
    }

    public int getCurrentProcessTime() { return processTick; }

    public int getMaxProcessTime() { return maxProcessTime; }
}
