package mctmods.immersivetechnology.common.blocks.multiblocks.recipe.builder;

import blusunrize.immersiveengineering.api.crafting.builders.IEFinishedRecipe;
import com.google.gson.JsonObject;
import mctmods.immersivetechnology.common.blocks.multiblocks.recipe.SteamTurbineRecipe;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

public class SteamTurbineRecipeBuilder extends IEFinishedRecipe<SteamTurbineRecipeBuilder>
{
    public SteamTurbineRecipeBuilder()
    {
        super(SteamTurbineRecipe.SERIALIZER.get());
    }

    public static SteamTurbineRecipeBuilder builder()
    {
        return new SteamTurbineRecipeBuilder();
    }

    public SteamTurbineRecipeBuilder addInput(TagKey<Fluid> fluidTag, int amount)
    {
        return this.addWriter((jsonObject) -> {
            JsonObject obj = new JsonObject();
            obj.addProperty("tag", fluidTag.location().toString());
            obj.addProperty("amount", amount);
            jsonObject.add("input", obj);
        });
    }

    public SteamTurbineRecipeBuilder addOutput(FluidStack fluidStack)
    {
        return this.addWriter((jsonObject) -> {
            JsonObject obj = new JsonObject();
            obj.addProperty("fluid", ForgeRegistries.FLUIDS.getKey(fluidStack.getFluid()).toString());
            obj.addProperty("amount", fluidStack.getAmount());
            jsonObject.add("output", obj);
        });
    }

    public SteamTurbineRecipeBuilder addOutput(Fluid fluid, int amount)
    {
        return addOutput(new FluidStack(fluid, amount));
    }

    public SteamTurbineRecipeBuilder setTime(int time)
    {
        return this.addWriter((jsonObject) -> {
            jsonObject.addProperty("time", time);
        });
    }
}