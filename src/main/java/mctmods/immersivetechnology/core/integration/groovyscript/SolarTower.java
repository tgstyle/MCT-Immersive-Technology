package mctmods.immersivetechnology.core.integration.groovyscript;

import mctmods.immersivetechnology.common.recipes.SolarTowerRecipe;
import mctmods.immersivetechnology.core.config.Config;

import com.immersiveconvergence.core.integration.groovyscript.ICFluidRecipeBuilder;
import com.immersiveconvergence.core.integration.groovyscript.ICFluidRecipeRegistry;
import com.cleanroommc.groovyscript.api.GroovyLog;

@SuppressWarnings("unused") public class SolarTower extends ICFluidRecipeRegistry<SolarTowerRecipe> {

    public SolarTower() {
        super("solarTower", () -> SolarTowerRecipe.recipeList, () -> Config.ITConfig.Multiblocks.enable.enable_solarTower,
                SolarTowerRecipe::addRecipe,
                r -> SolarTowerRecipe.removeRecipe(fluidInputAt(r, 0)));
    }

    public RecipeBuilder recipeBuilder() { return new RecipeBuilder(); }

    public class RecipeBuilder extends ICFluidRecipeBuilder<SolarTowerRecipe> {

        private double requiredTemp = -1;

        public RecipeBuilder requiredTemp(double requiredTemp) { this.requiredTemp = requiredTemp; return this; }

        @Override public String getErrorMsg() { return "Error adding Immersive Technology SolarTower recipe"; }

        @Override public void validate(GroovyLog.Msg msg) {
            requireTime(msg);
            validateFluids(msg, 1, 1, 1, 1);
        }

        @Override public SolarTowerRecipe register() {
            if (!validate()) { return null; }
            double actualTemp = requiredTemp >= 0 ? requiredTemp : SolarTowerRecipe.defaultTemperature();
            SolarTowerRecipe recipe = SolarTowerRecipe.addRecipe(fluidOut(0), fluidIn(0), time, actualTemp);
            addScripted(recipe);
            return recipe;
        }
    }
}
