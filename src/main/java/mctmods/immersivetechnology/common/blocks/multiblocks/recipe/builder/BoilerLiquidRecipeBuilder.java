package mctmods.immersivetechnology.common.blocks.multiblocks.recipe.builder;

import blusunrize.immersiveengineering.api.crafting.builders.IEFinishedRecipe;
import mctmods.immersivetechnology.common.blocks.multiblocks.recipe.BoilerLiquidRecipe;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;

public class BoilerLiquidRecipeBuilder extends IEFinishedRecipe<BoilerLiquidRecipeBuilder> {
    public BoilerLiquidRecipeBuilder() {
        super(BoilerLiquidRecipe.SERIALIZER.get());
        this.maxInputCount = 1;
    }

    public static BoilerLiquidRecipeBuilder builder() { return new BoilerLiquidRecipeBuilder(); }

    public BoilerLiquidRecipeBuilder addInput(TagKey<Fluid> fluidTag, int amount) { return addFluidTag("input", fluidTag, amount); }

    public BoilerLiquidRecipeBuilder setTime(int time) { return this.addWriter((jsonObject) -> jsonObject.addProperty("time", time)); }

    public BoilerLiquidRecipeBuilder setHeatPerTick(double heatPerTick) { return this.addWriter((jsonObject) -> jsonObject.addProperty("heatPerTick", heatPerTick)); }
}
