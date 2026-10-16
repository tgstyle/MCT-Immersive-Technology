package mctmods.immersivetechnology.common.multiblocks.metal.recipe.builder;

import com.immersiveconvergence.api.crafting.ICFinishedRecipe;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.SteamTurbineRecipe;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

public class SteamTurbineRecipeBuilder extends ICFinishedRecipe<SteamTurbineRecipeBuilder> {
    public SteamTurbineRecipeBuilder() { super(SteamTurbineRecipe.SERIALIZER.get()); }

    public static SteamTurbineRecipeBuilder builder() { return new SteamTurbineRecipeBuilder(); }

    public SteamTurbineRecipeBuilder addInput(TagKey<Fluid> fluidTag, int amount) { return addFluidTag("input", fluidTag, amount); }

    public SteamTurbineRecipeBuilder addOutput(FluidStack fluidStack) { return addFluid("output", fluidStack); }

    public SteamTurbineRecipeBuilder addOutput(Fluid fluid, int amount) { return addOutput(new FluidStack(fluid, amount)); }

    public SteamTurbineRecipeBuilder setTorque(float torque) { return this.addWriter((jsonObject) -> jsonObject.addProperty("torque", torque)); }
}
