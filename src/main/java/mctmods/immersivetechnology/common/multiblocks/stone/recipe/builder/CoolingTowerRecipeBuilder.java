package mctmods.immersivetechnology.common.multiblocks.stone.recipe.builder;

import com.immersiveconvergence.api.crafting.ICFinishedRecipe;
import mctmods.immersivetechnology.common.multiblocks.stone.recipe.CoolingTowerRecipe;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

public class CoolingTowerRecipeBuilder extends ICFinishedRecipe<CoolingTowerRecipeBuilder> {
    private int outputCount = 0;

    public CoolingTowerRecipeBuilder() { super(CoolingTowerRecipe.SERIALIZER.get()); }

    public static CoolingTowerRecipeBuilder builder() { return new CoolingTowerRecipeBuilder(); }

    public CoolingTowerRecipeBuilder addInput(TagKey<Fluid> fluidTag, int amount) { return addFluidTag("input" + inputCount++, fluidTag, amount); }

    public CoolingTowerRecipeBuilder addOutput(FluidStack fluidStack) { return addFluid("output" + outputCount++, fluidStack); }

    public CoolingTowerRecipeBuilder addOutput(Fluid fluid, int amount) { return addOutput(new FluidStack(fluid, amount)); }
}
