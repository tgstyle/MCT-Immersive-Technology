package ferro2000.immersivetech.api.crafting;

import ferro2000.immersivetech.api.ITUtils;
import net.minecraftforge.fluids.FluidStack;
import java.util.ArrayList;

public class SteamTurbineRecipe extends GenericRecipe {

    public static RecipeList<SteamTurbineRecipe> recipeList = new RecipeList<>();
    public static String recipeCategoryName = "Steam Turbine";
    public final FluidStack output;
    public final FluidStack input;
    public final int time;

    public SteamTurbineRecipe(FluidStack output, FluidStack input, int time) {
        this.output = output;
        this.input = input;
        this.time = time;
        this.recipeName = input.getLocalizedName();
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof FluidStack) {
            return this.input.isFluidEqual((FluidStack)object);
        } else if (object instanceof SteamTurbineRecipe) {
            return this == object;
        } else return false;
    }

    public static SteamTurbineRecipe addFuel(FluidStack output, FluidStack input, int time) {
        SteamTurbineRecipe recipe = new SteamTurbineRecipe(output, input, time);
        recipeList.add(recipe);
        return recipe;
    }

    public static SteamTurbineRecipe findFuel(FluidStack input) {
        if(input == null) return null;
        return ITUtils.First(recipeList, input);
    }
}