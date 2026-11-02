package mctmods.immersivetechnology.core.integration.groovyscript;

import mctmods.immersivetechnology.common.recipes.CoolingTowerRecipe;
import mctmods.immersivetechnology.core.config.Config;

import com.immersiveconvergence.core.integration.groovyscript.ICFluidRecipeBuilder;
import com.immersiveconvergence.core.integration.groovyscript.ICFluidRecipeRegistry;
import com.cleanroommc.groovyscript.api.GroovyLog;

@SuppressWarnings("unused") public class CoolingTower extends ICFluidRecipeRegistry<CoolingTowerRecipe> {

    public CoolingTower() {
        super("coolingTower", () -> CoolingTowerRecipe.recipeList, () -> Config.ITConfig.Multiblocks.enable.enable_coolingTower,
                CoolingTowerRecipe::addRecipe,
                r -> CoolingTowerRecipe.removeRecipe(fluidInputAt(r, 0), fluidInputAt(r, 1)));
    }

    public RecipeBuilder recipeBuilder() { return new RecipeBuilder(); }

    public class RecipeBuilder extends ICFluidRecipeBuilder<CoolingTowerRecipe> {

        @Override public String getErrorMsg() { return "Error adding Immersive Technology CoolingTower recipe"; }

        @Override public void validate(GroovyLog.Msg msg) {
            requireTime(msg);
            validateFluids(msg, 2, 2, 3, 3);
        }

        @Override public CoolingTowerRecipe register() {
            if (!validate()) { return null; }
            CoolingTowerRecipe recipe = CoolingTowerRecipe.addRecipe(fluidOut(0), fluidOut(1), fluidOut(2), fluidIn(0), fluidIn(1), time);
            addScripted(recipe);
            return recipe;
        }
    }
}
