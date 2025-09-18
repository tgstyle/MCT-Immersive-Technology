package mctmods.immersivetechnology.common.blocks.multiblocks.recipe.builder;

import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.api.crafting.builders.IEFinishedRecipe;
import mctmods.immersivetechnology.common.blocks.multiblocks.recipe.BoilerSolidRecipe;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;

public class BoilerSolidRecipeBuilder extends IEFinishedRecipe<BoilerSolidRecipeBuilder> {
    public BoilerSolidRecipeBuilder() {
        super(BoilerSolidRecipe.SERIALIZER.get());
        this.maxInputCount = 1;
    }

    public static BoilerSolidRecipeBuilder builder() { return new BoilerSolidRecipeBuilder(); }

    public BoilerSolidRecipeBuilder addInput(TagKey<Item> itemTag, int amount) { return addInput(new IngredientWithSize(Ingredient.of(itemTag), amount)); }

    public BoilerSolidRecipeBuilder setHeatPerTick(double heatPerTick) { return this.addWriter((jsonObject) -> jsonObject.addProperty("heatPerTick", heatPerTick)); }
}
