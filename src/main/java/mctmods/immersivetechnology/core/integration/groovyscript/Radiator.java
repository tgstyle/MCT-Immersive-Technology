package mctmods.immersivetechnology.core.integration.groovyscript;

import mctmods.immersivetechnology.common.recipes.RadiatorRecipe;
import mctmods.immersivetechnology.core.config.Config;

import com.immersiveconvergence.core.integration.groovyscript.ICFluidRecipeBuilder;
import com.immersiveconvergence.core.integration.groovyscript.ICFluidRecipeRegistry;
import com.cleanroommc.groovyscript.api.GroovyLog;

@SuppressWarnings("unused") public class Radiator extends ICFluidRecipeRegistry<RadiatorRecipe> {

    public Radiator() {
        super("radiator", () -> RadiatorRecipe.recipeList, () -> Config.ITConfig.Multiblocks.enable.enable_radiator,
                RadiatorRecipe::addRecipe,
                r -> RadiatorRecipe.removeRecipe(fluidInputAt(r, 0)));
    }

    public RecipeBuilder recipeBuilder() { return new RecipeBuilder(); }

    public class RecipeBuilder extends ICFluidRecipeBuilder<RadiatorRecipe> {

        @Override public String getErrorMsg() { return "Error adding Immersive Technology Radiator recipe"; }

        @Override public void validate(GroovyLog.Msg msg) {
            requireTime(msg);
            validateFluids(msg, 1, 1, 1, 1);
        }

        @Override public RadiatorRecipe register() {
            if (!validate()) { return null; }
            RadiatorRecipe recipe = RadiatorRecipe.addRecipe(fluidOut(0), fluidIn(0), time);
            addScripted(recipe);
            return recipe;
        }
    }
}
