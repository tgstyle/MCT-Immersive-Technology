package mctmods.immersivetechnology.common.multiblocks.metal.recipe;

import blusunrize.immersiveengineering.api.crafting.FluidTagInput;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

public interface ISolarRecipe {
    ResourceLocation getId();
    FluidTagInput input();
    FluidStack fluidOutput();
    double requiredTemp();
}
