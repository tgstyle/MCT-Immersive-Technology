package mctmods.immersivetechnology.common.multiblocks.metal.recipe.builder;

import blusunrize.immersiveengineering.api.crafting.builders.IEFinishedRecipe;

import mctmods.immersivetechnology.common.multiblocks.metal.recipe.BoilerTankRecipe;

import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

public class BoilerTankRecipeBuilder extends IEFinishedRecipe<BoilerTankRecipeBuilder> {
    public BoilerTankRecipeBuilder() {
        super(BoilerTankRecipe.SERIALIZER.get());
        this.maxInputCount = 1;
    }

    public static BoilerTankRecipeBuilder builder(TagKey<Fluid> inputTag, int inputAmount) { return new BoilerTankRecipeBuilder().addFluidTag("input", inputTag, inputAmount); }

    public BoilerTankRecipeBuilder addOutput(Fluid outputFluid, int outputAmount) { return addFluid("result", new FluidStack(outputFluid, outputAmount)); }

    public BoilerTankRecipeBuilder setTime(int time) { return this.addWriter((jsonObject) -> jsonObject.addProperty("time", time)); }
}
