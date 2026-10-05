package mctmods.immersivetechnology.common.multiblocks.stone.process;

import mctmods.immersivetechnology.common.multiblocks.stone.logic.AdvancedCokeOvenLogic;
import mctmods.immersivetechnology.common.multiblocks.stone.recipe.AdvancedCokeOvenRecipe;
import mctmods.immersivetechnology.core.util.ItemOutputs;

import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockLevel;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcessInMachine;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.ProcessContext;
import blusunrize.immersiveengineering.common.register.IEFluids;
import com.immersiveconvergence.api.multiblock.BurnProcessHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import java.util.function.BiFunction;

public class AdvancedCokeOvenProcess extends MultiblockProcessInMachine<AdvancedCokeOvenRecipe> {
    private float tickRemainder;
    private final int maxProcessTime;

    public AdvancedCokeOvenProcess(AdvancedCokeOvenRecipe recipe) {
        super(recipe, 0);
        this.maxProcessTime = recipe.getTotalProcessTime();
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

    @Override public void writeExtraDataToNBT(CompoundTag nbt) {
        super.writeExtraDataToNBT(nbt);
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
        ItemOutputs.merge(context.getInventory(), AdvancedCokeOvenLogic.SLOT_OUTPUT, recipe.itemOutput.get().copy());
        context.getInternalTanks()[0].fill(new FluidStack(IEFluids.CREOSOTE.getStill(), recipe.creosoteOutput), FluidAction.EXECUTE);
    }

    public int getCurrentProcessTime() { return this.processTick; }

    public int getMaxProcessTime() { return maxProcessTime; }
}
