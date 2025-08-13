package mctmods.immersivetechnology.common.blocks.multiblocks.recipe.builder;

import blusunrize.immersiveengineering.api.crafting.builders.IEFinishedRecipe;

import mctmods.immersivetechnology.common.blocks.multiblocks.recipe.BoilerRecipe;

import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

public class BoilerRecipeBuilder extends IEFinishedRecipe<BoilerRecipeBuilder> {
    public BoilerRecipeBuilder() {
        super(BoilerRecipe.SERIALIZER.get());
        this.maxInputCount = 1;
    }

    public static BoilerRecipeBuilder builder(TagKey<Fluid> inputTag, int inputAmount) { return new BoilerRecipeBuilder().addFluidTag("input", inputTag, inputAmount); }

    public BoilerRecipeBuilder addOutput(Fluid outputFluid, int outputAmount) { return addFluid("result", new FluidStack(outputFluid, outputAmount)); }

    public BoilerRecipeBuilder setTime(int time) { return this.addWriter((jsonObject) -> jsonObject.addProperty("time", time)); }
}
