package mctmods.immersivetechnology.common.util.compat.groovyscript;

import com.immersiveconvergence.common.util.compat.groovyscript.ICFluidRecipeBuilder;
import com.immersiveconvergence.common.util.compat.groovyscript.ICFluidRecipeRegistry;

import com.cleanroommc.groovyscript.api.GroovyLog;
import mctmods.immersivetechnology.api.crafting.HeatExchangerRecipe;
import mctmods.immersivetechnology.common.Config;

@SuppressWarnings("unused")
public class HeatExchanger extends ICFluidRecipeRegistry<HeatExchangerRecipe> {

    public HeatExchanger() {
        super("heatExchanger", () -> HeatExchangerRecipe.recipeList, () -> Config.ITConfig.Multiblocks.enable.enable_heatExchanger,
                HeatExchangerRecipe::addRecipe,
                r -> HeatExchangerRecipe.removeRecipe(fluidInputAt(r, 0), fluidInputAt(r, 1)));
    }

    public RecipeBuilder recipeBuilder() { return new RecipeBuilder(); }

    public class RecipeBuilder extends ICFluidRecipeBuilder<HeatExchangerRecipe> {

        @Override public String getErrorMsg() { return "Error adding Immersive Technology HeatExchanger recipe"; }

        @Override
        public void validate(GroovyLog.Msg msg) {
            requireTime(msg);
            requireEnergy(msg);
            validateFluids(msg, 2, 2, 1, 2);
        }

        @Override
        public HeatExchangerRecipe register() {
            if (!validate()) { return null; }
            HeatExchangerRecipe recipe = HeatExchangerRecipe.addRecipe(fluidOut(0), fluidOut(1), fluidIn(0), fluidIn(1), energy, time);
            addScripted(recipe);
            return recipe;
        }
    }
}
