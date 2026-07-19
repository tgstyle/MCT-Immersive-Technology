package mctmods.immersivetechnology.core.integration.jei;

import blusunrize.immersiveengineering.api.crafting.CokeOvenRecipe;
import blusunrize.immersiveengineering.api.crafting.IERecipeTypes;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.*;
import mctmods.immersivetechnology.common.multiblocks.stone.recipe.AdvancedCokeOvenRecipe;
import mctmods.immersivetechnology.common.multiblocks.stone.recipe.CoolingTowerRecipe;
import mctmods.immersivetechnology.core.registration.RecipeTypes;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.world.item.crafting.Recipe;

public class JEIRecipeTypes {
    public static final RecipeType<CokeOvenRecipe> ADVANCED_COKE_OVEN = create(IERecipeTypes.COKE_OVEN);
    public static final RecipeType<AdvancedCokeOvenRecipe> ADVANCED_COKE_OVEN_CUSTOM = create(RecipeTypes.ADVANCED_COKE_OVEN);
    public static final RecipeType<BoilerLiquidRecipe> BOILER_LIQUID = create(RecipeTypes.BOILER_LIQUID);
    public static final RecipeType<BoilerSolidRecipe> BOILER_SOLID = create(RecipeTypes.BOILER_SOLID);
    public static final RecipeType<BoilerTankRecipe> BOILER_TANK = create(RecipeTypes.BOILER_TANK);
    public static final RecipeType<CoolingTowerRecipe> COOLING_TOWER = create(RecipeTypes.COOLING_TOWER);
    public static final RecipeType<DistillerRecipe> DISTILLER = create(RecipeTypes.DISTILLER);
    public static final RecipeType<ElectrolyticCrucibleBatteryRecipe> ELECTROLYTIC_CRUCIBLE_BATTERY = create(RecipeTypes.ELECTROLYTIC_CRUCIBLE_BATTERY);
    public static final RecipeType<GasTurbineRecipe> GAS_TURBINE = create(RecipeTypes.GAS_TURBINE);
    public static final RecipeType<HeatExchangerRecipe> HEAT_EXCHANGER = create(RecipeTypes.HEAT_EXCHANGER);
    public static final RecipeType<RadiatorRecipe> RADIATOR = create(RecipeTypes.RADIATOR);
    public static final RecipeType<MeltingRecipe> MELTING = create(RecipeTypes.MELTING);
    public static final RecipeType<SolarTowerRecipe> SOLAR_TOWER = create(RecipeTypes.SOLAR_TOWER);
    public static final RecipeType<SteamTurbineRecipe> STEAM_TURBINE = create(RecipeTypes.STEAM_TURBINE);

    private static <T extends Recipe<?>> RecipeType<T> create(IERecipeTypes.TypeWithClass<T> type) {
        assert type.type().getId() != null;
        return new RecipeType<>(type.type().getId(), type.recipeClass());
    }
}
