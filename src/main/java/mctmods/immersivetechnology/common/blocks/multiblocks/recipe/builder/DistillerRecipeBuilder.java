package mctmods.immersivetechnology.common.blocks.multiblocks.recipe.builder;

import blusunrize.immersiveengineering.api.crafting.FluidTagInput;
import blusunrize.immersiveengineering.api.crafting.builders.IEFinishedRecipe;
import com.google.gson.JsonObject;
import mctmods.immersivetechnology.common.blocks.multiblocks.recipe.DistillerRecipe;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

public class DistillerRecipeBuilder extends IEFinishedRecipe<DistillerRecipeBuilder>
{
    public static final String FLUID_TAG_KEY = "fluidTag";

    public DistillerRecipeBuilder()
    {
        super(DistillerRecipe.SERIALIZER.get());
        this.maxInputCount = 2;
    }

    private DistillerRecipeBuilder(TagKey<Fluid> fluid)
    {
        super(DistillerRecipe.SERIALIZER.get());
        addWriter(obj -> obj.addProperty(FLUID_TAG_KEY, fluid.location().toString()));
    }

    private DistillerRecipeBuilder(TagKey<Fluid> fluid, int amount)
    {
        super(DistillerRecipe.SERIALIZER.get());
        addWriter(obj -> obj.addProperty(FLUID_TAG_KEY, fluid.location().toString()));
    }

    public static DistillerRecipeBuilder builder(Fluid fluid, int amount)
    {
        return builder(new FluidStack(fluid, amount));
    }

    public static DistillerRecipeBuilder builder()
    {
        return new DistillerRecipeBuilder();
    }

    public static DistillerRecipeBuilder builder(TagKey<Fluid> fluid)
    {
        return new DistillerRecipeBuilder(fluid);
    }

    public static DistillerRecipeBuilder builder(TagKey<Fluid> fluid, int amount)
    {
        return new DistillerRecipeBuilder(fluid, amount);
    }

    public static DistillerRecipeBuilder builder(FluidStack fluidStack)
    {
        return new DistillerRecipeBuilder().addFluid("result", fluidStack);
    }

    public DistillerRecipeBuilder addInput(FluidTagInput fluidTag)
    {
        return addFluidTag(generateSafeInputKey(), fluidTag);
    }
    public DistillerRecipeBuilder addInput(TagKey<Fluid> fluidTag, int amount)
    {
        return this.addWriter((jsonObject) -> {
            JsonObject obj = new JsonObject();
            obj.addProperty("tag", fluidTag.location().toString());
            obj.addProperty("amount", amount);
            jsonObject.add("input", obj);
        });
    }

    public DistillerRecipeBuilder addInput(FluidTagInput fluidTag, String key)
    {
        return addFluidTag(key, fluidTag);
    }
}
