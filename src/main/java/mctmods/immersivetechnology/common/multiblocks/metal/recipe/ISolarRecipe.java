package mctmods.immersivetechnology.common.multiblocks.metal.recipe;

import com.immersiveconvergence.api.crafting.ICFluidTagInput;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

public interface ISolarRecipe {
    ResourceLocation getId();
    ICFluidTagInput input();
    FluidStack fluidOutput();
    double requiredTemp();
}
