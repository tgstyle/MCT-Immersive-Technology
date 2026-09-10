package mctmods.immersivetechnology.common.util.compat.groovyscript;

import com.immersiveconvergence.common.util.compat.groovyscript.ICFluidRecipeBuilder;
import com.immersiveconvergence.common.util.compat.groovyscript.ICFluidRecipeRegistry;

import com.cleanroommc.groovyscript.api.GroovyLog;
import mctmods.immersivetechnology.api.crafting.BoilerTankRecipe;
import mctmods.immersivetechnology.common.Config;

@SuppressWarnings("unused")
public class BoilerTank extends ICFluidRecipeRegistry<BoilerTankRecipe> {

    public BoilerTank() {
        super("boilerTank", () -> BoilerTankRecipe.recipeList, () -> Config.ITConfig.Multiblocks.enable.enable_boiler,
                BoilerTankRecipe::addRecipe,
                r -> BoilerTankRecipe.removeRecipe(fluidInputAt(r, 0)));
    }

    public RecipeBuilder recipeBuilder() { return new RecipeBuilder(); }

    public class RecipeBuilder extends ICFluidRecipeBuilder<BoilerTankRecipe> {

        private double requiredHeat;

        public RecipeBuilder requiredHeat(double requiredHeat) { this.requiredHeat = requiredHeat; return this; }

        @Override public String getErrorMsg() { return "Error adding Immersive Technology BoilerTank recipe"; }

        @Override
        public void validate(GroovyLog.Msg msg) {
            requireTime(msg);
            validateFluids(msg, 1, 1, 1, 1);
            if (requiredHeat <= 0) { msg.add("the recipe needs a required heat above 0, got " + requiredHeat); }
        }

        @Override
        public BoilerTankRecipe register() {
            if (!validate()) { return null; }
            BoilerTankRecipe recipe = BoilerTankRecipe.addRecipe(fluidOut(0), fluidIn(0), time, requiredHeat);
            addScripted(recipe);
            return recipe;
        }
    }
}
