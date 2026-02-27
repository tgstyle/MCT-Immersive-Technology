package mctmods.immersivetechnology.core.integration.jei;

import blusunrize.immersiveengineering.api.crafting.CokeOvenRecipe;
import blusunrize.immersiveengineering.api.crafting.IERecipeTypes;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.*;
import mctmods.immersivetechnology.common.multiblocks.stone.recipe.AdvancedCokeOvenRecipe;
import mctmods.immersivetechnology.common.multiblocks.stone.recipe.CoolingTowerRecipe;
import mctmods.immersivetechnology.core.registration.ITRecipeTypes;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.world.item.crafting.Recipe;

public class JEIRecipeTypes {
    public static final RecipeType<CokeOvenRecipe> ADVANCED_COKE_OVEN = create(IERecipeTypes.COKE_OVEN);
    public static final RecipeType<AdvancedCokeOvenRecipe> ADVANCED_COKE_OVEN_CUSTOM = create(ITRecipeTypes.ADVANCED_COKE_OVEN);
    public static final RecipeType<BoilerLiquidRecipe> BOILER_LIQUID = create(ITRecipeTypes.BOILER_LIQUID);
    public static final RecipeType<BoilerSolidRecipe> BOILER_SOLID = create(ITRecipeTypes.BOILER_SOLID);
    public static final RecipeType<BoilerTankRecipe> BOILER_TANK = create(ITRecipeTypes.BOILER_TANK);
    public static final RecipeType<CoolingTowerRecipe> COOLING_TOWER = create(ITRecipeTypes.COOLING_TOWER);
    public static final RecipeType<DistillerRecipe> DISTILLER = create(ITRecipeTypes.DISTILLER);
    public static final RecipeType<GasTurbineRecipe> GAS_TURBINE = create(ITRecipeTypes.GAS_TURBINE);
    public static final RecipeType<HeatExchangerRecipe> HEAT_EXCHANGER = create(ITRecipeTypes.HEAT_EXCHANGER);
    public static final RecipeType<SolarMelterRecipe> SOLAR_MELTER = create(ITRecipeTypes.SOLAR_MELTER);
    public static final RecipeType<SolarTowerRecipe> SOLAR_TOWER = create(ITRecipeTypes.SOLAR_TOWER);
    public static final RecipeType<SteamTurbineRecipe> STEAM_TURBINE = create(ITRecipeTypes.STEAM_TURBINE);

    private static <T extends Recipe<?>> RecipeType<T> create(IERecipeTypes.TypeWithClass<T> type) {
        assert type.type().getId() != null;
        return new RecipeType<>(type.type().getId(), type.recipeClass());
    }
}
