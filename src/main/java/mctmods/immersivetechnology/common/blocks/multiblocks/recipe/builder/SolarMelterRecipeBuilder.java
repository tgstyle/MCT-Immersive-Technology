package mctmods.immersivetechnology.common.blocks.multiblocks.recipe.builder;

import blusunrize.immersiveengineering.api.crafting.builders.IEFinishedRecipe;
import mctmods.immersivetechnology.common.blocks.multiblocks.recipe.SolarMelterRecipe;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

public class SolarMelterRecipeBuilder extends IEFinishedRecipe<SolarMelterRecipeBuilder> {
    public SolarMelterRecipeBuilder() { super(SolarMelterRecipe.SERIALIZER.get()); }

    public static SolarMelterRecipeBuilder builder() { return new SolarMelterRecipeBuilder(); }

    public SolarMelterRecipeBuilder addInput(TagKey<Fluid> fluidTag, int amount) { return addFluidTag("input", fluidTag, amount); }

    public SolarMelterRecipeBuilder addOutput(FluidStack fluidStack) { return addFluid("output", fluidStack); }

    public SolarMelterRecipeBuilder addOutput(Fluid fluid, int amount) { return addOutput(new FluidStack(fluid, amount)); }

    public SolarMelterRecipeBuilder setTime(int time) { return this.addWriter((jsonObject) -> jsonObject.addProperty("time", time)); }
}
