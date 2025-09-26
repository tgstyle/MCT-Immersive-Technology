package mctmods.immersivetechnology.common.integration.jei;

import blusunrize.immersiveengineering.api.crafting.IERecipeTypes;
import mctmods.immersivetechnology.common.blocks.multiblocks.recipe.BoilerTankRecipe;
import mctmods.immersivetechnology.common.blocks.multiblocks.recipe.DistillerRecipe;
import mctmods.immersivetechnology.core.registration.ITRecipeTypes;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.world.item.crafting.Recipe;

public class JEIRecipeTypes {
    public static final RecipeType<BoilerTankRecipe> BOILER_TANK = create(ITRecipeTypes.BOILER_TANK);
    public static final RecipeType<DistillerRecipe> DISTILLER = create(ITRecipeTypes.DISTILLER);

    private static <T extends Recipe<?>> RecipeType<T> create(IERecipeTypes.TypeWithClass<T> type) {
        assert type.type().getId() != null;
        return new RecipeType<>(type.type().getId(), type.recipeClass());
    }
}
