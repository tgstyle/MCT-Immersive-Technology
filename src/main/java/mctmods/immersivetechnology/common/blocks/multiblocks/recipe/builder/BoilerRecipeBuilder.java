package mctmods.immersivetechnology.common.blocks.multiblocks.recipe.builder;

import blusunrize.immersiveengineering.api.crafting.FluidTagInput;
import blusunrize.immersiveengineering.api.crafting.builders.IEFinishedRecipe;
import mctmods.immersivetechnology.common.blocks.multiblocks.recipe.BoilerRecipe;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

public class BoilerRecipeBuilder extends IEFinishedRecipe<BoilerRecipeBuilder>
{
    public static final String FLUID_TAG_KEY = "fluidTag";
    public static final String AMOUNT_TAG_KEY = "fluidAmount";

    public BoilerRecipeBuilder()
    {
        super(BoilerRecipe.SERIALIZER.get());
        this.maxInputCount = 2;
    }

    private BoilerRecipeBuilder(TagKey<Fluid> fluid)
    {
        super(BoilerRecipe.SERIALIZER.get());
        addWriter(obj -> obj.addProperty(FLUID_TAG_KEY, fluid.location().toString()));
    }

    private BoilerRecipeBuilder(TagKey<Fluid> fluid, int amount)
    {
        super(BoilerRecipe.SERIALIZER.get());
        addWriter(obj -> obj.addProperty(FLUID_TAG_KEY, fluid.location().toString()));
        addWriter(obj -> obj.addProperty(AMOUNT_TAG_KEY, amount));
    }

    public BoilerRecipeBuilder setHeatPerTick(int heatPerTick) {
        return this.addWriter((jsonObject) -> {
            jsonObject.addProperty("heatPerTick", heatPerTick);
        });
    }

    public static BoilerRecipeBuilder builder(Fluid fluid, int amount)
    {
        return builder(new FluidStack(fluid, amount));
    }

    public static BoilerRecipeBuilder builder()
    {
        return new BoilerRecipeBuilder();
    }

    public static BoilerRecipeBuilder builder(TagKey<Fluid> fluid)
    {
        return new BoilerRecipeBuilder(fluid);
    }

    public static BoilerRecipeBuilder builder(TagKey<Fluid> fluid, int amount)
    {
        return new BoilerRecipeBuilder(fluid, amount);
    }

    public static BoilerRecipeBuilder builder(FluidStack fluidStack)
    {
        return new BoilerRecipeBuilder().addFluid("result", fluidStack);
    }

    public BoilerRecipeBuilder addInput(FluidTagInput fluidTag)
    {
        return addFluidTag(generateSafeInputKey(), fluidTag);
    }

    public BoilerRecipeBuilder addInput(TagKey<Fluid> fluidTag, int amount)
    {
        return addFluidTag(generateSafeInputKey(), fluidTag, amount);
    }

    public BoilerRecipeBuilder addInput(FluidTagInput fluidTag, String key)
    {
        return addFluidTag(key, fluidTag);
    }

    public BoilerRecipeBuilder addInput(TagKey<Fluid> fluidTag, int amount, String key)
    {
        return addFluidTag(key, fluidTag, amount);
    }
}
