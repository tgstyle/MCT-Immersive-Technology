package mctmods.immersivetechnology.common.util.compat.groovyscript;

import com.immersiveconvergence.common.util.compat.groovyscript.ICFluidRecipeBuilder;
import com.immersiveconvergence.common.util.compat.groovyscript.ICFluidRecipeRegistry;

import com.cleanroommc.groovyscript.api.GroovyLog;
import mctmods.immersivetechnology.api.crafting.BoilerLiquidRecipe;
import mctmods.immersivetechnology.common.Config;

@SuppressWarnings("unused")
public class BoilerLiquid extends ICFluidRecipeRegistry<BoilerLiquidRecipe> {

    public BoilerLiquid() {
        super("boilerLiquid", () -> BoilerLiquidRecipe.fuelList, () -> Config.ITConfig.Multiblocks.enable.enable_boiler,
                BoilerLiquidRecipe::addFuel,
                r -> BoilerLiquidRecipe.removeFuel(fluidInputAt(r, 0)));
    }

    public RecipeBuilder recipeBuilder() { return new RecipeBuilder(); }

    public class RecipeBuilder extends ICFluidRecipeBuilder<BoilerLiquidRecipe> {

        private double heatPerTick;
        private double targetHeat;

        public RecipeBuilder heatPerTick(double heatPerTick) { this.heatPerTick = heatPerTick; return this; }

        public RecipeBuilder targetHeat(double targetHeat) { this.targetHeat = targetHeat; return this; }

        @Override public String getErrorMsg() { return "Error adding Immersive Technology BoilerLiquid recipe"; }

        @Override
        public void validate(GroovyLog.Msg msg) {
            requireTime(msg);
            validateFluids(msg, 1, 1, 0, 0);
            if (heatPerTick <= 0) { msg.add("the fuel needs a heat per tick above 0, got " + heatPerTick); }
            if (targetHeat <= 0) { msg.add("the fuel needs a target heat above 0, got " + targetHeat); }
        }

        @Override
        public BoilerLiquidRecipe register() {
            if (!validate()) { return null; }
            BoilerLiquidRecipe recipe = BoilerLiquidRecipe.addFuel(fluidIn(0), time, heatPerTick, targetHeat);
            addScripted(recipe);
            return recipe;
        }
    }
}
