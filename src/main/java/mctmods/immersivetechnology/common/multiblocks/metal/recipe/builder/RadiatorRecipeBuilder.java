package mctmods.immersivetechnology.common.multiblocks.metal.recipe.builder;

import blusunrize.immersiveengineering.api.crafting.builders.IEFinishedRecipe;
import com.google.gson.JsonObject;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.RadiatorRecipe;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.fluids.FluidStack;

import java.util.Objects;

public class RadiatorRecipeBuilder extends IEFinishedRecipe<RadiatorRecipeBuilder> {
    public RadiatorRecipeBuilder() { super(RadiatorRecipe.SERIALIZER.get()); }

    public static RadiatorRecipeBuilder builder() { return new RadiatorRecipeBuilder(); }

    public RadiatorRecipeBuilder addInput(TagKey<Fluid> fluidTag, int amount) {
        return this.addWriter((jsonObject) -> {
            JsonObject obj = new JsonObject();
            obj.addProperty("tag", fluidTag.location().toString());
            obj.addProperty("amount", amount);
            jsonObject.add("input", obj);
        });
    }

    public RadiatorRecipeBuilder addOutput(FluidStack fluidStack) {
        return this.addWriter((jsonObject) -> {
            JsonObject obj = new JsonObject();
            obj.addProperty("fluid", Objects.requireNonNull(ForgeRegistries.FLUIDS.getKey(fluidStack.getFluid())).toString());
            obj.addProperty("amount", fluidStack.getAmount());
            jsonObject.add("output", obj);
        });
    }

    public RadiatorRecipeBuilder addOutput(Fluid fluid, int amount) { return addOutput(new FluidStack(fluid, amount)); }

    public RadiatorRecipeBuilder setTime(int time) { return this.addWriter((jsonObject) -> jsonObject.addProperty("time", time)); }
}
