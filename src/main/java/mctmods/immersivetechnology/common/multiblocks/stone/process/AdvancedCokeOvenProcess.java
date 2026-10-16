package mctmods.immersivetechnology.common.multiblocks.stone.process;

import mctmods.immersivetechnology.common.multiblocks.stone.logic.AdvancedCokeOvenLogic;
import mctmods.immersivetechnology.common.multiblocks.stone.recipe.AdvancedCokeOvenRecipe;
import mctmods.immersivetechnology.core.util.ItemOutputs;

import com.immersiveconvergence.api.multiblock.blocks.env.IMultiblockLevel;
import com.immersiveconvergence.api.multiblock.process.MultiblockProcessInMachine;
import com.immersiveconvergence.api.multiblock.process.ProcessContext;
import com.immersiveconvergence.api.crafting.ICCokeOvenRecipe;
import com.immersiveconvergence.api.multiblock.BurnProcessHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import java.util.function.BiFunction;

public class AdvancedCokeOvenProcess extends MultiblockProcessInMachine<AdvancedCokeOvenRecipe> {
    private float tickRemainder;
    private final int maxProcessTime;

    public AdvancedCokeOvenProcess(RecipeHolder<AdvancedCokeOvenRecipe> recipeHolder) {
        super(recipeHolder, 0);
        this.maxProcessTime = recipeHolder.value().getTotalProcessTime();
    }

    public AdvancedCokeOvenProcess(BiFunction<Level, ResourceLocation, AdvancedCokeOvenRecipe> getRecipe, CompoundTag data) {
        super(getRecipe, data);
        if (data.contains("processTick")) {
            float saved = data.getFloat("processTick");
            this.processTick = (int) saved;
            this.tickRemainder = saved - this.processTick;
        }
        else { this.tickRemainder = data.getFloat("tickRemainder"); }
        this.maxProcessTime = data.getInt("maxProcessTime");
    }

    @Override protected void writeExtraDataToNBT(CompoundTag nbt) {
        super.writeExtraDataToNBT(nbt);
        nbt.putFloat("tickRemainder", tickRemainder);
        nbt.putInt("maxProcessTime", maxProcessTime);
    }

    @Override public void doProcessTick(ProcessContext.ProcessContextInMachine<AdvancedCokeOvenRecipe> context, IMultiblockLevel level) {
        if (getRecipe(level.getRawLevel()) == null) {
            this.clearProcess = true;
            return;
        }
        BurnProcessHandler.IFurnaceEnvironment env = (BurnProcessHandler.IFurnaceEnvironment) context;
        float total = this.tickRemainder + (float) env.getProcessSpeed(level);
        int wholeTicks = (int) total;
        this.tickRemainder = total - wholeTicks;
        this.processTick += wholeTicks;
        if (this.processTick >= this.maxProcessTime) {
            processFinish(context, level);
            this.clearProcess = true;
        }
    }

    @Override public boolean canProcess(ProcessContext.ProcessContextInMachine<AdvancedCokeOvenRecipe> context, Level level) { return true; }

    @Override protected void processFinish(ProcessContext.ProcessContextInMachine<AdvancedCokeOvenRecipe> context, IMultiblockLevel level) {
        AdvancedCokeOvenRecipe recipe = getRecipe(level.getRawLevel());
        if (recipe == null) { return; }
        ItemOutputs.merge(context.getInventory(), AdvancedCokeOvenLogic.SLOT_OUTPUT, recipe.itemOutput.get().copy());
        context.getInternalTanks()[0].fill(new FluidStack(ICCokeOvenRecipe.creosote(),recipe.creosoteOutput), FluidAction.EXECUTE);
    }

    public int getCurrentProcessTime() { return this.processTick; }

    public int getMaxProcessTime() { return maxProcessTime; }
}
