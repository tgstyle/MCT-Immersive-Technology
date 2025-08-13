package mctmods.immersivetechnology.common.blocks.multiblocks.recipe.builder;

import blusunrize.immersiveengineering.api.crafting.builders.IEFinishedRecipe;
import mctmods.immersivetechnology.common.blocks.multiblocks.recipe.AdvancedCokeOvenFuel;
import net.minecraft.world.item.crafting.Ingredient;

public class AdvancedCokeOvenFuelBuilder extends IEFinishedRecipe<AdvancedCokeOvenFuelBuilder> {
    public AdvancedCokeOvenFuelBuilder() { super(AdvancedCokeOvenFuel.SERIALIZER.get()); }

    public static AdvancedCokeOvenFuelBuilder builder(Ingredient input) { return new AdvancedCokeOvenFuelBuilder().addIngredient("input", input); }

    public AdvancedCokeOvenFuelBuilder setTime(int time) { return this.addWriter(jsonObject -> jsonObject.addProperty("time", time)); }
}
