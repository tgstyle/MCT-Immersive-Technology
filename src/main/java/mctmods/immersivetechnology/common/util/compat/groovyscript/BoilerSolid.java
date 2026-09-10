package mctmods.immersivetechnology.common.util.compat.groovyscript;

import com.immersiveconvergence.common.util.compat.groovyscript.ICFluidRecipeBuilder;
import com.immersiveconvergence.common.util.compat.groovyscript.ICFluidRecipeRegistry;

import com.cleanroommc.groovyscript.api.GroovyLog;
import mctmods.immersivetechnology.api.crafting.BoilerSolidRecipe;
import mctmods.immersivetechnology.common.Config;

@SuppressWarnings("unused")
public class BoilerSolid extends ICFluidRecipeRegistry<BoilerSolidRecipe> {

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

        @Override
        public void validate(GroovyLog.Msg msg) {
            validateItems(msg, 1, 1, 0, 0);
            if (heatPerTick <= 0) { msg.add("the fuel needs a heat per tick above 0, got " + heatPerTick); }
            if (targetHeat <= 0) { msg.add("the fuel needs a target heat above 0, got " + targetHeat); }
        }

        @Override
        public BoilerSolidRecipe register() {
            if (!validate()) { return null; }
            BoilerSolidRecipe recipe = BoilerSolidRecipe.addFuel(itemIn(0), heatPerTick, targetHeat);
            addScripted(recipe);
            return recipe;
        }
    }
}
