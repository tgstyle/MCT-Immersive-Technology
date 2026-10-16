package mctmods.immersivetechnology.common.multiblocks.metal.recipe.builder;

import com.immersiveconvergence.api.crafting.ICFinishedRecipe;
import com.immersiveconvergence.api.crafting.ICFluidTagInput;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.HeatExchangerRecipe;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;

public class HeatExchangerRecipeBuilder extends ICFinishedRecipe<HeatExchangerRecipeBuilder> {
    public HeatExchangerRecipeBuilder() {
        super(HeatExchangerRecipe.SERIALIZER.get());
    }

    public static HeatExchangerRecipeBuilder builder(ICFluidTagInput input0, @Nullable ICFluidTagInput input1, FluidStack output0, @Nullable FluidStack output1, int energy, int time) { HeatExchangerRecipeBuilder builder = new HeatExchangerRecipeBuilder().addFluidTag("input0", input0).addFluid("output0", output0).setEnergy(energy).setTime(time);
        if (input1 != null) builder.addFluidTag("input1", input1);
        if (output1 != null) builder.addFluid("output1", output1);
        return builder;
    }
}
