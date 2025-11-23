package mctmods.immersivetechnology.common.multiblocks.metal.process;

import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockLevel;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcessInMachine;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.ProcessContext;
import mctmods.immersivetechnology.common.multiblocks.metal.logic.DistillerLogic;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.DistillerRecipe;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.function.BiFunction;

public class DistillerProcess extends MultiblockProcessInMachine<DistillerRecipe> {
    public DistillerProcess(DistillerRecipe recipe) {
        super(recipe);
        this.setInputTanks(0);
    }
    public DistillerProcess(BiFunction<Level, ResourceLocation, DistillerRecipe> getRecipe, CompoundTag data) {
        super(getRecipe, data);
        this.setInputTanks(0);
    }

    @Override
    public void doProcessTick(ProcessContext.ProcessContextInMachine<DistillerRecipe> context, IMultiblockLevel level) {
        LevelDependentData<DistillerRecipe> levelData = getLevelData(level.getRawLevel());
        if (levelData.recipe() == null) { this.clearProcess = true; return; }
        if (this.processTick == 0) {
            IFluidTank inputTank = context.getInternalTanks()[0];
            int amount = levelData.recipe().input.getAmount();
            FluidStack drained = inputTank.drain(amount, FluidAction.SIMULATE);
            if (drained.getAmount() < amount || !levelData.recipe().input.testIgnoringAmount(drained)) { this.clearProcess = true; return; }
            inputTank.drain(amount, FluidAction.EXECUTE);
        }
        super.doProcessTick(context, level);
    }

    @Override
    public boolean canProcess(ProcessContext.ProcessContextInMachine<DistillerRecipe> context, Level level) {
        LevelDependentData<DistillerRecipe> levelData = getLevelData(level);
        if (levelData.recipe() == null) return true;
        return context.getEnergy().extractEnergy(levelData.energyPerTick(), true) == levelData.energyPerTick();
    }

    @Override
    protected void processFinish(ProcessContext.ProcessContextInMachine<DistillerRecipe> context, IMultiblockLevel level) {
        super.processFinish(context, level);
        DistillerRecipe recipe = getRecipe(level.getRawLevel());
        if (recipe != null) {
            if (recipe.fluidOutput != null && !recipe.fluidOutput.isEmpty()) {
                ((DistillerLogic.State) context).getTanks().output().fill(recipe.fluidOutput.copy(), FluidAction.EXECUTE);
            }
            if (!recipe.itemOutput.isEmpty() && level.getRawLevel().random.nextFloat() < recipe.chance) {
                IItemHandlerModifiable inv = context.getInventory();
                ItemStack salt = recipe.itemOutput.copy();
                ItemStack current = inv.getStackInSlot(DistillerLogic.OUTPUT_SLOT);
                if (current.isEmpty()) { inv.setStackInSlot(DistillerLogic.OUTPUT_SLOT, salt); }
                else if (ItemHandlerHelper.canItemStacksStack(current, salt) && current.getCount() + salt.getCount() <= current.getMaxStackSize()) {
                    current.grow(salt.getCount());
                    inv.setStackInSlot(DistillerLogic.OUTPUT_SLOT, current);
                }
            }
        }
    }
}
