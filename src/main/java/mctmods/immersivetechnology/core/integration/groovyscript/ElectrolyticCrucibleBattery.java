package mctmods.immersivetechnology.core.integration.groovyscript;

import mctmods.immersivetechnology.common.recipes.ElectrolyticCrucibleBatteryRecipe;
import mctmods.immersivetechnology.core.config.Config;

import com.immersiveconvergence.core.integration.groovyscript.ICFluidRecipeBuilder;
import com.immersiveconvergence.core.integration.groovyscript.ICFluidRecipeRegistry;
import com.cleanroommc.groovyscript.api.GroovyLog;

@SuppressWarnings("unused") public class ElectrolyticCrucibleBattery extends ICFluidRecipeRegistry<ElectrolyticCrucibleBatteryRecipe> {

    public ElectrolyticCrucibleBattery() {
        super("electrolyticCrucibleBattery", () -> ElectrolyticCrucibleBatteryRecipe.recipeList, () -> Config.ITConfig.Multiblocks.enable.enable_electrolyticCrucibleBattery,
                ElectrolyticCrucibleBatteryRecipe::addRecipe,
                r -> ElectrolyticCrucibleBatteryRecipe.removeRecipe(fluidInputAt(r, 0)));
    }

    public RecipeBuilder recipeBuilder() { return new RecipeBuilder(); }

    public class RecipeBuilder extends ICFluidRecipeBuilder<ElectrolyticCrucibleBatteryRecipe> {

        @Override public String getErrorMsg() { return "Error adding Immersive Technology ElectrolyticCrucibleBattery recipe"; }

        @Override public void validate(GroovyLog.Msg msg) {
            requireTime(msg);
            requireEnergy(msg);
            validateFluids(msg, 1, 1, 1, 3);
            validateItems(msg, 0, 0, 0, 1);
        }

        @Override public ElectrolyticCrucibleBatteryRecipe register() {
            if (!validate()) { return null; }
            ElectrolyticCrucibleBatteryRecipe recipe = ElectrolyticCrucibleBatteryRecipe.addRecipe(fluidOut(0), fluidOut(1), fluidOut(2), itemOut(0), fluidIn(0), energy, time);
            addScripted(recipe);
            return recipe;
        }
    }
}
