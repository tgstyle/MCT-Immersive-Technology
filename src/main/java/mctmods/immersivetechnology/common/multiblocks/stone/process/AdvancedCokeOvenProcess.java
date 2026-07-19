package mctmods.immersivetechnology.common.multiblocks.stone.process;

import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockLevel;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcessInMachine;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.ProcessContext;
import blusunrize.immersiveengineering.common.register.IEFluids;
import mctmods.immersivetechnology.common.multiblocks.helper.FurnaceHandler;
import mctmods.immersivetechnology.common.multiblocks.stone.logic.AdvancedCokeOvenLogic;
import mctmods.immersivetechnology.common.multiblocks.stone.recipe.AdvancedCokeOvenRecipe;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
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
        this.tickRemainder = data.getFloat("tickRemainder");
        this.maxProcessTime = data.getInt("maxProcessTime");
    }

    public static AdvancedCokeOvenProcess create(BiFunction<Level, ResourceLocation, AdvancedCokeOvenRecipe> getRecipe, CompoundTag data, Object ignored) {
        return new AdvancedCokeOvenProcess(getRecipe, data);
    }

    @Override public void writeExtraDataToNBT(CompoundTag nbt, HolderLookup.Provider provider) {
        super.writeExtraDataToNBT(nbt, provider);
        nbt.putFloat("tickRemainder", tickRemainder);
        nbt.putInt("maxProcessTime", maxProcessTime);
    }

    @Override public void doProcessTick(ProcessContext.ProcessContextInMachine<AdvancedCokeOvenRecipe> context, IMultiblockLevel level) {
        if (getRecipe(level.getRawLevel()) == null) { this.clearProcess = true; return; }
        @SuppressWarnings("unchecked")
        FurnaceHandler.IFurnaceEnvironment<AdvancedCokeOvenRecipe> env = (FurnaceHandler.IFurnaceEnvironment<AdvancedCokeOvenRecipe>) context;
        double speed = env.getProcessSpeed(level);
        float total = this.tickRemainder + (float) speed;
        int wholeTicks = (int) total;
        this.tickRemainder = total - wholeTicks;
        this.processTick += wholeTicks;
        if (this.processTick >= this.maxProcessTime) { processFinish(context, level); this.clearProcess = true; }
    }

    @Override public boolean canProcess(ProcessContext.ProcessContextInMachine<AdvancedCokeOvenRecipe> context, Level level) { return true; }

    @Override protected void processFinish(ProcessContext.ProcessContextInMachine<AdvancedCokeOvenRecipe> context, IMultiblockLevel level) {
        AdvancedCokeOvenRecipe recipe = getRecipe(level.getRawLevel());
        if (recipe != null) {
            ItemStack input = context.getInventory().getStackInSlot(inputSlots[0]);
            int processedCount = Math.min(input.getCount(), recipe.input.getCount());
            input.shrink(processedCount);
            ItemStack outUnit = recipe.itemOutput.get();
            ItemStack out = outUnit.copyWithCount(outUnit.getCount() * processedCount);
            ItemStack current = context.getInventory().getStackInSlot(AdvancedCokeOvenLogic.SLOT_OUTPUT);
            if (current.isEmpty()) { context.getInventory().setStackInSlot(AdvancedCokeOvenLogic.SLOT_OUTPUT, out); }
            else if (ItemStack.isSameItemSameComponents(current, out) && current.getCount() + out.getCount() <= current.getMaxStackSize()) { current.grow(out.getCount()); }
            FluidStack fluidOut = new FluidStack(IEFluids.CREOSOTE.getStill(), recipe.creosoteOutput * processedCount);
            context.getInternalTanks()[0].fill(fluidOut.copy(), FluidAction.EXECUTE);
        }
    }

    public int getCurrentProcessTime() { return this.processTick; }

    public int getMaxProcessTime() { return maxProcessTime; }
}
