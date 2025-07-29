package mctmods.immersivetechnology.common.blocks.multiblocks.recipe.builder;

import blusunrize.immersiveengineering.api.crafting.FluidTagInput;
import blusunrize.immersiveengineering.api.crafting.builders.IEFinishedRecipe;
import mctmods.immersivetechnology.common.blocks.multiblocks.recipe.BoilerFuelRecipe;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;

public class BoilerFuelRecipeBuilder extends IEFinishedRecipe<BoilerFuelRecipeBuilder> {
    public BoilerFuelRecipeBuilder() {
        super(BoilerFuelRecipe.SERIALIZER.get());
        this.maxInputCount = 1;
    }

    public static BoilerFuelRecipeBuilder builder() { return new BoilerFuelRecipeBuilder(); }

    public BoilerFuelRecipeBuilder setTime(int time) { return this.addWriter((jsonObject) -> jsonObject.addProperty("time", time)); }

    public BoilerFuelRecipeBuilder setHeatPerTick(double heatPerTick) { return this.addWriter((jsonObject) -> jsonObject.addProperty("heatPerTick", heatPerTick)); }

    public BoilerFuelRecipeBuilder addInput(FluidTagInput fluidTag) { return addFluidTag("fuel", fluidTag); }

    public BoilerFuelRecipeBuilder addInput(TagKey<Fluid> fluidTag, int amount) { return addFluidTag("fuel", fluidTag, amount); }
}
