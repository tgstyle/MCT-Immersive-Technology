package mctmods.immersivetechnology.common.multiblocks.stone.process;

import mctmods.immersivetechnology.common.multiblocks.stone.logic.AdvancedCokeOvenLogic;
import mctmods.immersivetechnology.common.multiblocks.stone.recipe.AdvancedCokeOvenRecipe;
import mctmods.immersivetechnology.core.util.ItemOutputs;

import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockLevel;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcessInMachine;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.ProcessContext;
import blusunrize.immersiveengineering.common.register.IEFluids;
import com.immersiveconvergence.api.multiblock.BurnProcessHandler;
import net.minecraft.core.HolderLookup;
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

    public static AdvancedCokeOvenProcess create(BiFunction<Level, ResourceLocation, AdvancedCokeOvenRecipe> getRecipe, CompoundTag data, Object ignored) { return new AdvancedCokeOvenProcess(getRecipe, data); }

    @Override public void writeExtraDataToNBT(CompoundTag nbt, HolderLookup.Provider provider) {
        super.writeExtraDataToNBT(nbt, provider);
        nbt.putFloat("tickRemainder", tickRemainder);
        nbt.putInt("maxProcessTime", maxProcessTime);
    }

    @Override public void doProcessTick(ProcessContext.ProcessContextInMachine<AdvancedCokeOvenRecipe> context, IMultiblockLevel level) {
        if (getRecipe(level.getRawLevel()) == null) {
            this.clearProcess = true;
            return;
        }
        @SuppressWarnings("unchecked") BurnProcessHandler.IFurnaceEnvironment<AdvancedCokeOvenRecipe> env = (BurnProcessHandler.IFurnaceEnvironment<AdvancedCokeOvenRecipe>) context;
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
        context.getInventory().getStackInSlot(inputSlots[0]).shrink(recipe.input.getCount());
        ItemOutputs.merge(context.getInventory(), AdvancedCokeOvenLogic.SLOT_OUTPUT, recipe.itemOutput.get().copy());
        context.getInternalTanks()[0].fill(new FluidStack(IEFluids.CREOSOTE.getStill(), recipe.creosoteOutput), FluidAction.EXECUTE);
    }

    public int getCurrentProcessTime() { return this.processTick; }

    public int getMaxProcessTime() { return maxProcessTime; }
}
