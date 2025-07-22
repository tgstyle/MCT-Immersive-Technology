package mctmods.immersivetechnology.common.integration.jei;

import blusunrize.immersiveengineering.api.crafting.IERecipeTypes;
import mctmods.immersivetechnology.common.blocks.multiblocks.recipe.AdvancedCokeOvenRecipe;
import mctmods.immersivetechnology.common.blocks.multiblocks.recipe.BoilerRecipe;
import mctmods.immersivetechnology.core.registration.ITRecipeTypes;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.world.item.crafting.Recipe;

public class JEIRecipeTypes
{
    public static final RecipeType<AdvancedCokeOvenRecipe> ADV_COKE_OVEN = create(ITRecipeTypes.ADVANCED_COKE_OVEN);
    public static final RecipeType<BoilerRecipe> BOILER = create(ITRecipeTypes.BOILER);

    private static <T extends Recipe<?>>
    RecipeType<T> create(IERecipeTypes.TypeWithClass<T> type)
    {
        return new RecipeType<>(type.type().getId(), type.recipeClass());
    }
}
