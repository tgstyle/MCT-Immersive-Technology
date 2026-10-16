package mctmods.immersivetechnology.common.multiblocks.metal.recipe.builder;

import com.immersiveconvergence.api.crafting.ICFinishedRecipe;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.GasTurbineRecipe;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

public class GasTurbineRecipeBuilder extends ICFinishedRecipe<GasTurbineRecipeBuilder> {
    public GasTurbineRecipeBuilder() { super(GasTurbineRecipe.SERIALIZER.get()); }

    public static GasTurbineRecipeBuilder builder() { return new GasTurbineRecipeBuilder(); }

    public GasTurbineRecipeBuilder addInput(TagKey<Fluid> fluidTag, int amount) { return addFluidTag("input", fluidTag, amount); }

    public GasTurbineRecipeBuilder addOutput(FluidStack fluidStack) { return addFluid("output", fluidStack); }

    public GasTurbineRecipeBuilder addOutput(Fluid fluid, int amount) { return addOutput(new FluidStack(fluid, amount)); }

    public GasTurbineRecipeBuilder setTorque(float torque) { return this.addWriter((jsonObject) -> jsonObject.addProperty("torque", torque)); }
}
