package mctmods.immersivetechnology.common.util.compat.groovyscript;

import com.immersiveconvergence.common.util.compat.groovyscript.ICFluidRecipeBuilder;
import com.immersiveconvergence.common.util.compat.groovyscript.ICFluidRecipeRegistry;

import com.cleanroommc.groovyscript.api.GroovyLog;
import mctmods.immersivetechnology.api.crafting.HighPressureSteamTurbineRecipe;
import mctmods.immersivetechnology.common.Config;

@SuppressWarnings("unused")
public class HighPressureSteamTurbine extends ICFluidRecipeRegistry<HighPressureSteamTurbineRecipe> {

    public HighPressureSteamTurbine() {
        super("highPressureSteamTurbine", () -> HighPressureSteamTurbineRecipe.recipeList, () -> Config.ITConfig.Multiblocks.enable.enable_highPressureSteamTurbine,
                HighPressureSteamTurbineRecipe::addFuel,
                r -> HighPressureSteamTurbineRecipe.removeFuel(fluidInputAt(r, 0)));
    }

    public RecipeBuilder recipeBuilder() { return new RecipeBuilder(); }

    public class RecipeBuilder extends ICFluidRecipeBuilder<HighPressureSteamTurbineRecipe> {

        private float torque = -1;

        public RecipeBuilder torque(float torque) { this.torque = torque; return this; }

        @Override public String getErrorMsg() { return "Error adding Immersive Technology HighPressureSteamTurbine recipe"; }

        @Override
        public void validate(GroovyLog.Msg msg) {
            requireTime(msg);
            validateFluids(msg, 1, 1, 1, 1);
        }

        @Override
        public HighPressureSteamTurbineRecipe register() {
            if (!validate()) { return null; }
            float actualTorque = torque > 0 ? torque : HighPressureSteamTurbineRecipe.defaultTorque();
            HighPressureSteamTurbineRecipe recipe = HighPressureSteamTurbineRecipe.addFuel(new HighPressureSteamTurbineRecipe(fluidOut(0), fluidIn(0), time, actualTorque));
            addScripted(recipe);
            return recipe;
        }
    }
}
