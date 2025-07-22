package mctmods.immersivetechnology.common.blocks.multiblocks.recipe.builder;

import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.api.crafting.builders.IEFinishedRecipe;
import mctmods.immersivetechnology.common.blocks.multiblocks.recipe.AdvancedCokeOvenRecipe;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class AdvancedCokeOvenRecipeBuilder extends IEFinishedRecipe<AdvancedCokeOvenRecipeBuilder>
{
    private AdvancedCokeOvenRecipeBuilder()
    {
        super(AdvancedCokeOvenRecipe.SERIALIZER.get());
    }

    public static AdvancedCokeOvenRecipeBuilder builder(Item result)
    {
        return new AdvancedCokeOvenRecipeBuilder().addResult(result);
    }

    public static AdvancedCokeOvenRecipeBuilder builder(ItemStack result)
    {
        return new AdvancedCokeOvenRecipeBuilder().addResult(result);
    }

    public static AdvancedCokeOvenRecipeBuilder builder(TagKey<Item> result, int count)
    {
        return new AdvancedCokeOvenRecipeBuilder().addResult(new IngredientWithSize(result, count));
    }

    public AdvancedCokeOvenRecipeBuilder setOil(int amount)
    {
        return addWriter(jsonObject -> jsonObject.addProperty("creosote", amount));
    }
}
