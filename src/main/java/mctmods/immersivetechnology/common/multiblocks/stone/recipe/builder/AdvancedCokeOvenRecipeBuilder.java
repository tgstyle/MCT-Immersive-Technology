package mctmods.immersivetechnology.common.multiblocks.stone.recipe.builder;

import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.api.crafting.builders.IEFinishedRecipe;
import com.google.gson.JsonObject;
import mctmods.immersivetechnology.common.multiblocks.stone.recipe.AdvancedCokeOvenRecipe;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class AdvancedCokeOvenRecipeBuilder extends IEFinishedRecipe<AdvancedCokeOvenRecipeBuilder> {
    private AdvancedCokeOvenRecipeBuilder() { super(AdvancedCokeOvenRecipe.SERIALIZER.get()); }

    public static AdvancedCokeOvenRecipeBuilder builder(Item input) { return new AdvancedCokeOvenRecipeBuilder().addInput(input); }

    public static AdvancedCokeOvenRecipeBuilder builder(TagKey<Item> input, int count) { return new AdvancedCokeOvenRecipeBuilder().addInput(input, count); }

    public AdvancedCokeOvenRecipeBuilder addInput(Item input) { return addIngredient("input", IngredientWithSize.of(new ItemStack(input))); }

    public AdvancedCokeOvenRecipeBuilder addInput(TagKey<Item> input, int count) { return addIngredient("input", new IngredientWithSize(input, count)); }

    public AdvancedCokeOvenRecipeBuilder addOutput(Item output) { return addItem("result", output); }

    @SuppressWarnings("unused")
    public AdvancedCokeOvenRecipeBuilder addOutput(ItemStack output) { return addItem("result", output); }

    public AdvancedCokeOvenRecipeBuilder addOutput(TagKey<Item> output, int count) {
        return addWriter(jsonObject -> {
            JsonObject obj = new JsonObject();
            obj.addProperty("tag", output.location().toString());
            if (count > 1) obj.addProperty("count", count);
            jsonObject.add("result", obj);
        });
    }

    public AdvancedCokeOvenRecipeBuilder setCreosote(int amount) { return addWriter(jsonObject -> jsonObject.addProperty("creosote", amount)); }
}
