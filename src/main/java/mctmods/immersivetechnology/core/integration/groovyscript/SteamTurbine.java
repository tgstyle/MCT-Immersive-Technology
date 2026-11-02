package mctmods.immersivetechnology.core.integration.groovyscript;

import mctmods.immersivetechnology.common.recipes.SteamTurbineRecipe;
import mctmods.immersivetechnology.core.config.Config;

import com.immersiveconvergence.core.integration.groovyscript.ICFluidRecipeBuilder;
import com.immersiveconvergence.core.integration.groovyscript.ICFluidRecipeRegistry;
import com.cleanroommc.groovyscript.api.GroovyLog;

@SuppressWarnings("unused") public class SteamTurbine extends ICFluidRecipeRegistry<SteamTurbineRecipe> {

    public SteamTurbine() {
        super("steamTurbine", () -> SteamTurbineRecipe.recipeList, () -> Config.ITConfig.Multiblocks.enable.enable_steamTurbine,
                SteamTurbineRecipe::addFuel,
                r -> SteamTurbineRecipe.removeFuel(fluidInputAt(r, 0)));
    }

    public RecipeBuilder recipeBuilder() { return new RecipeBuilder(); }

    public class RecipeBuilder extends ICFluidRecipeBuilder<SteamTurbineRecipe> {

        private float torque = -1;

        public RecipeBuilder torque(float torque) { this.torque = torque; return this; }

        @Override public String getErrorMsg() { return "Error adding Immersive Technology SteamTurbine recipe"; }

        @Override public void validate(GroovyLog.Msg msg) {
            requireTime(msg);
            validateFluids(msg, 1, 1, 1, 1);
        }

        @Override public SteamTurbineRecipe register() {
            if (!validate()) { return null; }
            float actualTorque = torque > 0 ? torque : SteamTurbineRecipe.defaultTorque();
            SteamTurbineRecipe recipe = SteamTurbineRecipe.addFuel(new SteamTurbineRecipe(fluidOut(0), fluidIn(0), time, actualTorque));
            addScripted(recipe);
            return recipe;
        }
    }
}
