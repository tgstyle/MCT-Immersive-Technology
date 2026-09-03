package mctmods.immersivetechnology.common.multiblocks.metal.recipe.builder;

import blusunrize.immersiveengineering.api.crafting.FluidTagInput;
import blusunrize.immersiveengineering.api.crafting.builders.IEFinishedRecipe;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.HeatExchangerRecipe;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;

public class HeatExchangerRecipeBuilder extends IEFinishedRecipe<HeatExchangerRecipeBuilder> {
    public HeatExchangerRecipeBuilder() {
        super(HeatExchangerRecipe.SERIALIZER.get());
    }

    public static HeatExchangerRecipeBuilder builder(FluidTagInput input0, @Nullable FluidTagInput input1, FluidStack output0, @Nullable FluidStack output1, int energy, int time) { HeatExchangerRecipeBuilder builder = new HeatExchangerRecipeBuilder().addFluidTag("input0", input0).addFluid("output0", output0).setEnergy(energy).setTime(time);
        if (input1 != null) builder.addFluidTag("input1", input1);
        if (output1 != null) builder.addFluid("output1", output1);
        return builder;
    }

    public HeatExchangerRecipeBuilder setEnergy(int energy) { return this.addWriter(jsonObject -> jsonObject.addProperty("energy", energy)); }

    public HeatExchangerRecipeBuilder setTime(int time) { return this.addWriter(jsonObject -> jsonObject.addProperty("time", time)); }
}
