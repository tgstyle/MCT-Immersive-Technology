package mctmods.immersivetechnology.common.blocks.multiblocks.recipe.builder;

import blusunrize.immersiveengineering.api.crafting.FluidTagInput;
import blusunrize.immersiveengineering.api.crafting.builders.IEFinishedRecipe;
import mctmods.immersivetechnology.common.blocks.multiblocks.recipe.DistillerRecipe;
import net.minecraftforge.fluids.FluidStack;

public class DistillerRecipeBuilder extends IEFinishedRecipe<DistillerRecipeBuilder>
{

    public DistillerRecipeBuilder()
    {
        super(DistillerRecipe.SERIALIZER.get());
        this.maxInputCount = 2;
    }

    public static DistillerRecipeBuilder builder(FluidTagInput fluidIn, int energy, int time, FluidStack primaryFluidOutput) {
        return new DistillerRecipeBuilder()
                .addFluidTag("input", fluidIn)
                .setEnergy(energy)
                .setTime(time)
                .addFluid("result", primaryFluidOutput);
    }
}
