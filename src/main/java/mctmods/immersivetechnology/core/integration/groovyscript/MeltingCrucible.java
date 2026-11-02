package mctmods.immersivetechnology.core.integration.groovyscript;

import mctmods.immersivetechnology.common.recipes.MeltingCrucibleRecipe;
import mctmods.immersivetechnology.core.config.Config;

import com.immersiveconvergence.core.integration.groovyscript.ICFluidRecipeBuilder;
import com.immersiveconvergence.core.integration.groovyscript.ICFluidRecipeRegistry;
import com.cleanroommc.groovyscript.api.GroovyLog;

@SuppressWarnings("unused") public class MeltingCrucible extends ICFluidRecipeRegistry<MeltingCrucibleRecipe> {

    public MeltingCrucible() {
        super("meltingCrucible", () -> MeltingCrucibleRecipe.recipeList, () -> Config.ITConfig.Multiblocks.enable.enable_meltingCrucible,
                MeltingCrucibleRecipe::addRecipe,
                MeltingCrucibleRecipe.recipeList::remove);
    }

    public RecipeBuilder recipeBuilder() { return new RecipeBuilder(); }

    public class RecipeBuilder extends ICFluidRecipeBuilder<MeltingCrucibleRecipe> {

        private double requiredTemp = -1;

        public RecipeBuilder requiredTemp(double requiredTemp) { this.requiredTemp = requiredTemp; return this; }

        @Override public String getErrorMsg() { return "Error adding Immersive Technology MeltingCrucible recipe"; }

        @Override public void validate(GroovyLog.Msg msg) {
            requireTime(msg);
            validateItems(msg, 1, 1, 0, 0);
            validateFluids(msg, 0, 0, 1, 1);
        }

        @Override public MeltingCrucibleRecipe register() {
            if (!validate()) { return null; }
            double actualTemp = requiredTemp >= 0 ? requiredTemp : MeltingCrucibleRecipe.defaultTemperature();
            MeltingCrucibleRecipe recipe = MeltingCrucibleRecipe.addRecipe(fluidOut(0), itemIn(0), time, actualTemp);
            addScripted(recipe);
            return recipe;
        }
    }
}
