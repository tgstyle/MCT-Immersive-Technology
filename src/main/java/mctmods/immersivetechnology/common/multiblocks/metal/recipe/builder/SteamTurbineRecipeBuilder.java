package mctmods.immersivetechnology.common.multiblocks.metal.recipe.builder;

import com.immersiveconvergence.api.crafting.ICFinishedRecipe;
import com.google.gson.JsonObject;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.SteamTurbineRecipe;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Objects;

public class SteamTurbineRecipeBuilder extends ICFinishedRecipe<SteamTurbineRecipeBuilder> {
    public SteamTurbineRecipeBuilder() { super(SteamTurbineRecipe.SERIALIZER.get()); }

    public static SteamTurbineRecipeBuilder builder() { return new SteamTurbineRecipeBuilder(); }

    public SteamTurbineRecipeBuilder addInput(TagKey<Fluid> fluidTag, int amount) {
        return this.addWriter((jsonObject) -> {
            JsonObject obj = new JsonObject();
            obj.addProperty("tag", fluidTag.location().toString());
            obj.addProperty("amount", amount);
            jsonObject.add("input", obj);
        });
    }

    public SteamTurbineRecipeBuilder addOutput(FluidStack fluidStack) {
        return this.addWriter((jsonObject) -> {
            JsonObject obj = new JsonObject();
            obj.addProperty("fluid", Objects.requireNonNull(ForgeRegistries.FLUIDS.getKey(fluidStack.getFluid())).toString());
            obj.addProperty("amount", fluidStack.getAmount());
            jsonObject.add("output", obj);
        });
    }

    public SteamTurbineRecipeBuilder addOutput(Fluid fluid, int amount) { return addOutput(new FluidStack(fluid, amount)); }

    public SteamTurbineRecipeBuilder setTime(int time) { return this.addWriter((jsonObject) -> jsonObject.addProperty("time", time)); }

    public SteamTurbineRecipeBuilder setTorque(float torque) { return this.addWriter((jsonObject) -> jsonObject.addProperty("torque", torque)); }
}
