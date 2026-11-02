package mctmods.immersivetechnology.core.integration.groovyscript;

import mctmods.immersivetechnology.common.recipes.DistillerRecipe;
import mctmods.immersivetechnology.core.config.Config;

import com.immersiveconvergence.core.integration.groovyscript.ICFluidRecipeBuilder;
import com.immersiveconvergence.core.integration.groovyscript.ICFluidRecipeRegistry;
import com.cleanroommc.groovyscript.api.GroovyLog;

@SuppressWarnings("unused") public class Distiller extends ICFluidRecipeRegistry<DistillerRecipe> {

    public Distiller() {
        super("distiller", () -> DistillerRecipe.recipeList, () -> Config.ITConfig.Multiblocks.enable.enable_distiller,
                DistillerRecipe::addRecipe,
                r -> DistillerRecipe.removeRecipe(fluidInputAt(r, 0)));
    }

    public RecipeBuilder recipeBuilder() { return new RecipeBuilder(); }

    public class RecipeBuilder extends ICFluidRecipeBuilder<DistillerRecipe> {

        private float chance = 1.0F;

        public RecipeBuilder chance(float chance) { this.chance = chance; return this; }

        @Override public String getErrorMsg() { return "Error adding Immersive Technology Distiller recipe"; }

        @Override public void validate(GroovyLog.Msg msg) {
            requireTime(msg);
            requireEnergy(msg);
            validateFluids(msg, 1, 1, 1, 1);
            validateItems(msg, 0, 0, 0, 1);
            if (chance < 0 || chance > 1) { msg.add("the recipe needs an item output chance between 0 and 1, got " + chance); }
        }

        @Override public DistillerRecipe register() {
            if (!validate()) { return null; }
            DistillerRecipe recipe = DistillerRecipe.addRecipe(fluidOut(0), fluidIn(0), itemOut(0), energy, time, chance);
            addScripted(recipe);
            return recipe;
        }
    }
}
