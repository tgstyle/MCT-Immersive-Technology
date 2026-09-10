package mctmods.immersivetechnology.common.util.compat.groovyscript;

import com.immersiveconvergence.common.util.compat.groovyscript.ICFluidRecipeBuilder;
import com.immersiveconvergence.common.util.compat.groovyscript.ICFluidRecipeRegistry;

import com.cleanroommc.groovyscript.api.GroovyLog;
import mctmods.immersivetechnology.api.crafting.GasTurbineRecipe;
import mctmods.immersivetechnology.common.Config;

@SuppressWarnings("unused")
public class GasTurbine extends ICFluidRecipeRegistry<GasTurbineRecipe> {

    public GasTurbine() {
        super("gasTurbine", () -> GasTurbineRecipe.recipeList, () -> Config.ITConfig.Multiblocks.enable.enable_gasTurbine,
                GasTurbineRecipe::addFuel,
                r -> GasTurbineRecipe.removeFuel(fluidInputAt(r, 0)));
    }

    public RecipeBuilder recipeBuilder() { return new RecipeBuilder(); }

    public class RecipeBuilder extends ICFluidRecipeBuilder<GasTurbineRecipe> {

        private float torque = -1;

        public RecipeBuilder torque(float torque) { this.torque = torque; return this; }

        @Override public String getErrorMsg() { return "Error adding Immersive Technology GasTurbine recipe"; }

        @Override
        public void validate(GroovyLog.Msg msg) {
            requireTime(msg);
            validateFluids(msg, 1, 1, 1, 1);
        }

        @Override
        public GasTurbineRecipe register() {
            if (!validate()) { return null; }
            float actualTorque = torque > 0 ? torque : GasTurbineRecipe.defaultTorque();
            GasTurbineRecipe recipe = GasTurbineRecipe.addFuel(new GasTurbineRecipe(fluidOut(0), fluidIn(0), time, actualTorque));
            addScripted(recipe);
            return recipe;
        }
    }
}
