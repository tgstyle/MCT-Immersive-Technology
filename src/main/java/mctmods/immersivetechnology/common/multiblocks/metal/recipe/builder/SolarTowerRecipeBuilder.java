package mctmods.immersivetechnology.common.multiblocks.metal.recipe.builder;

import com.immersiveconvergence.api.crafting.ICFinishedRecipe;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.SolarTowerRecipe;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

public class SolarTowerRecipeBuilder extends ICFinishedRecipe<SolarTowerRecipeBuilder> {
    private SolarTowerRecipeBuilder() { super(SolarTowerRecipe.SERIALIZER.get()); }

    public static SolarTowerRecipeBuilder builder() { return new SolarTowerRecipeBuilder(); }

    public SolarTowerRecipeBuilder addInput(TagKey<Fluid> tag, int amount) { return addFluidTag("input", tag, amount); }

    public SolarTowerRecipeBuilder addOutput(FluidStack output) { return addFluid("output", output); }

    public SolarTowerRecipeBuilder addOutput(Fluid fluid, int amount) { return addOutput(new FluidStack(fluid, amount)); }

    public SolarTowerRecipeBuilder setRequiredTemp(double temp) { return addWriter(json -> json.addProperty("requiredTemp", temp)); }
}
