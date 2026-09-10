package mctmods.immersivetechnology.common.util.compat.groovyscript;

import com.immersiveconvergence.common.util.compat.groovyscript.ICFluidRecipeBuilder;
import com.immersiveconvergence.common.util.compat.groovyscript.ICFluidRecipeRegistry;

import com.cleanroommc.groovyscript.api.GroovyLog;
import mctmods.immersivetechnology.api.crafting.RadiatorRecipe;
import mctmods.immersivetechnology.common.Config;

@SuppressWarnings("unused")
public class Radiator extends ICFluidRecipeRegistry<RadiatorRecipe> {

    public Radiator() {
        super("radiator", () -> RadiatorRecipe.recipeList, () -> Config.ITConfig.Multiblocks.enable.enable_radiator,
                RadiatorRecipe::addRecipe,
                r -> RadiatorRecipe.removeRecipe(fluidInputAt(r, 0)));
    }

    public RecipeBuilder recipeBuilder() { return new RecipeBuilder(); }

    public class RecipeBuilder extends ICFluidRecipeBuilder<RadiatorRecipe> {

        @Override public String getErrorMsg() { return "Error adding Immersive Technology Radiator recipe"; }

        @Override
        public void validate(GroovyLog.Msg msg) {
            requireTime(msg);
            validateFluids(msg, 1, 1, 1, 1);
        }

        @Override
        public RadiatorRecipe register() {
            if (!validate()) { return null; }
            RadiatorRecipe recipe = RadiatorRecipe.addRecipe(fluidOut(0), fluidIn(0), time);
            addScripted(recipe);
            return recipe;
        }
    }
}
