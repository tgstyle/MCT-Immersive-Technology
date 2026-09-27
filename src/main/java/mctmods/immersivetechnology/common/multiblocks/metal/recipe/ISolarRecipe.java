package mctmods.immersivetechnology.common.multiblocks.metal.recipe;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.fluids.FluidStack;

public interface ISolarRecipe {
    ResourceLocation getId();
    int getInputAmount();
    FluidStack fluidOutput();
    double requiredTemp();
    boolean matches(FluidStack stack);
}
