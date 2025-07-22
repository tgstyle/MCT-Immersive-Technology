package mctmods.immersivetechnology.common.blocks.multiblocks.recipe.builder;

import blusunrize.immersiveengineering.api.crafting.builders.IEFinishedRecipe;
import com.google.gson.JsonObject;
import mctmods.immersivetechnology.common.blocks.multiblocks.recipe.GasTurbineRecipe;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

public class GasTurbineRecipeBuilder extends IEFinishedRecipe<GasTurbineRecipeBuilder>
{
    public GasTurbineRecipeBuilder()
    {
        super(GasTurbineRecipe.SERIALIZER.get());
    }

    public static GasTurbineRecipeBuilder builder()
    {
        return new GasTurbineRecipeBuilder();
    }

    public GasTurbineRecipeBuilder addInput(TagKey<Fluid> fluidTag, int amount)
    {
        return this.addWriter((jsonObject) -> {
            JsonObject obj = new JsonObject();
            obj.addProperty("tag", fluidTag.location().toString());
            obj.addProperty("amount", amount);
            jsonObject.add("input", obj);
        });
    }

    public GasTurbineRecipeBuilder addOutput(FluidStack fluidStack)
    {
        return this.addWriter((jsonObject) -> {
            JsonObject obj = new JsonObject();
            obj.addProperty("fluid", ForgeRegistries.FLUIDS.getKey(fluidStack.getFluid()).toString());
            obj.addProperty("amount", fluidStack.getAmount());
            jsonObject.add("output", obj);
        });
    }

    public GasTurbineRecipeBuilder addOutput(Fluid fluid, int amount)
    {
        return addOutput(new FluidStack(fluid, amount));
    }

    public GasTurbineRecipeBuilder setTime(int time)
    {
        return this.addWriter((jsonObject) -> {
            jsonObject.addProperty("time", time);
        });
    }
}