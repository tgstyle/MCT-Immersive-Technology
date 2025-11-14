package mctmods.immersivetechnology.common.multiblocks.stone.recipe.builder;

import blusunrize.immersiveengineering.api.crafting.builders.IEFinishedRecipe;
import com.google.gson.JsonObject;
import mctmods.immersivetechnology.common.multiblocks.stone.recipe.CoolingTowerRecipe;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.fluids.FluidStack;

import java.util.Objects;

public class CoolingTowerRecipeBuilder extends IEFinishedRecipe<CoolingTowerRecipeBuilder> {
    private int inputCount = 0;
    private int outputCount = 0;

    public CoolingTowerRecipeBuilder() { super(CoolingTowerRecipe.SERIALIZER.get()); }

    public static CoolingTowerRecipeBuilder builder() { return new CoolingTowerRecipeBuilder(); }

    public CoolingTowerRecipeBuilder addInput(TagKey<Fluid> fluidTag, int amount) {
        String key = "input" + inputCount;
        inputCount++;
        return this.addWriter((jsonObject) -> {
            JsonObject obj = new JsonObject();
            obj.addProperty("tag", fluidTag.location().toString());
            obj.addProperty("amount", amount);
            jsonObject.add(key, obj);
        });
    }

    public CoolingTowerRecipeBuilder addOutput(FluidStack fluidStack) {
        String key = "output" + outputCount;
        outputCount++;
        return this.addWriter((jsonObject) -> {
            JsonObject obj = new JsonObject();
            obj.addProperty("fluid", Objects.requireNonNull(ForgeRegistries.FLUIDS.getKey(fluidStack.getFluid())).toString());
            obj.addProperty("amount", fluidStack.getAmount());
            jsonObject.add(key, obj);
        });
    }

    public CoolingTowerRecipeBuilder addOutput(Fluid fluid, int amount) { return addOutput(new FluidStack(fluid, amount)); }

    public CoolingTowerRecipeBuilder setTime(int time) { return this.addWriter((jsonObject) -> jsonObject.addProperty("time", time)); }
}
