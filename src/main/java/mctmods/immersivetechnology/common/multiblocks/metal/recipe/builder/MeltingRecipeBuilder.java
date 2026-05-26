package mctmods.immersivetechnology.common.multiblocks.metal.recipe.builder;

import blusunrize.immersiveengineering.api.crafting.builders.IEFinishedRecipe;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.MeltingRecipe;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

public class MeltingRecipeBuilder extends IEFinishedRecipe<MeltingRecipeBuilder> {
    private MeltingRecipeBuilder() { super(MeltingRecipe.SERIALIZER.get()); }

    public static MeltingRecipeBuilder builder() { return new MeltingRecipeBuilder(); }

    public MeltingRecipeBuilder addInput(TagKey<Fluid> tag, int amount) { return addFluidTag("input", tag, amount); }

    public MeltingRecipeBuilder addOutput(FluidStack output) { return addFluid("output", output); }

    public MeltingRecipeBuilder addOutput(Fluid fluid, int amount) { return addOutput(new FluidStack(fluid, amount)); }

    public MeltingRecipeBuilder setTime(int time) { return super.setTime(time); }

    public MeltingRecipeBuilder setRequiredTemp(double temp) { return addWriter(json -> json.addProperty("requiredTemp", temp)); }
}
