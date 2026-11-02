package mctmods.immersivetechnology.core.integration.groovyscript;

import mctmods.immersivetechnology.common.recipes.BoilerSolidRecipe;
import mctmods.immersivetechnology.core.config.Config;

import com.immersiveconvergence.core.integration.groovyscript.ICFluidRecipeBuilder;
import com.immersiveconvergence.core.integration.groovyscript.ICFluidRecipeRegistry;
import com.cleanroommc.groovyscript.api.GroovyLog;

@SuppressWarnings("unused") public class BoilerSolid extends ICFluidRecipeRegistry<BoilerSolidRecipe> {

    public BoilerSolid() {
        super("boilerSolid", () -> BoilerSolidRecipe.fuelList, () -> Config.ITConfig.Multiblocks.enable.enable_boilerSolid,
                BoilerSolidRecipe::addFuel,
                BoilerSolidRecipe.fuelList::remove);
    }

    public RecipeBuilder recipeBuilder() { return new RecipeBuilder(); }

    public class RecipeBuilder extends ICFluidRecipeBuilder<BoilerSolidRecipe> {

        private double heatPerTick;
        private double targetHeat;

        public RecipeBuilder heatPerTick(double heatPerTick) { this.heatPerTick = heatPerTick; return this; }

        public RecipeBuilder targetHeat(double targetHeat) { this.targetHeat = targetHeat; return this; }

        @Override public String getErrorMsg() { return "Error adding Immersive Technology BoilerSolid fuel"; }

        @Override public void validate(GroovyLog.Msg msg) {
            validateItems(msg, 1, 1, 0, 0);
            if (heatPerTick <= 0) { msg.add("the fuel needs a heat per tick above 0, got " + heatPerTick); }
            if (targetHeat <= 0) { msg.add("the fuel needs a target heat above 0, got " + targetHeat); }
        }

        @Override public BoilerSolidRecipe register() {
            if (!validate()) { return null; }
            BoilerSolidRecipe recipe = BoilerSolidRecipe.addFuel(itemIn(0), heatPerTick, targetHeat);
            addScripted(recipe);
            return recipe;
        }
    }
}
