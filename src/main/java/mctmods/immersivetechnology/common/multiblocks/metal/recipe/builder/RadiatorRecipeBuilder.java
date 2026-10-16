package mctmods.immersivetechnology.common.multiblocks.metal.recipe.builder;

import com.immersiveconvergence.api.crafting.ICFinishedRecipe;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.RadiatorRecipe;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

public class RadiatorRecipeBuilder extends ICFinishedRecipe<RadiatorRecipeBuilder> {
    public RadiatorRecipeBuilder() { super(RadiatorRecipe.SERIALIZER.get()); }

    public static RadiatorRecipeBuilder builder() { return new RadiatorRecipeBuilder(); }

    public RadiatorRecipeBuilder addInput(TagKey<Fluid> fluidTag, int amount) { return addFluidTag("input", fluidTag, amount); }

    public RadiatorRecipeBuilder addOutput(FluidStack fluidStack) { return addFluid("output", fluidStack); }

    public RadiatorRecipeBuilder addOutput(Fluid fluid, int amount) { return addOutput(new FluidStack(fluid, amount)); }
}
