package mctmods.immersivetechnology.common.multiblocks.metal.process;

import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcessInMachine;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.HeatExchangerRecipe;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public class HeatExchangerProcess extends MultiblockProcessInMachine<HeatExchangerRecipe> {
    public HeatExchangerProcess(HeatExchangerRecipe recipe) {
        super(recipe);
        this.setInputTanks(0, 1);
    }

    public HeatExchangerProcess(BiFunction<Level, ResourceLocation, HeatExchangerRecipe> getRecipe, CompoundTag data) {
        super(getRecipe, data);
        this.setInputTanks(0, 1);
    }

    @Override protected List<FluidStack> getRecipeFluidOutputs(Level level) {
        HeatExchangerRecipe recipe = this.getRecipe(level);
        if (recipe == null) { return List.of(); }
        List<FluidStack> list = new ArrayList<>();
        list.add(recipe.output0.copy());
        if (recipe.output1 != null) { list.add(recipe.output1.copy()); }
        return list;
    }
}
