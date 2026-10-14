package mctmods.immersivetechnology.core.integration.jei;

import com.immersiveconvergence.api.crafting.ICCokeOvenRecipe;
import com.immersiveconvergence.api.crafting.ICRecipeType;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.*;
import mctmods.immersivetechnology.common.multiblocks.stone.recipe.AdvancedCokeOvenRecipe;
import mctmods.immersivetechnology.common.multiblocks.stone.recipe.CoolingTowerRecipe;
import mctmods.immersivetechnology.core.registration.RecipeTypes;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.world.item.crafting.Recipe;

import java.util.List;

public class JEIRecipeTypes {
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
    public static final RecipeType<StyleNOXRecipe> STYLENOX = create(RecipeTypes.STYLENOX);

    public static List<RecipeType<?>> advancedCokeOvenTypes() {
        ICRecipeType<?> cokeOven = ICCokeOvenRecipe.recipeType();
        if (cokeOven == null) { return List.of(ADVANCED_COKE_OVEN_CUSTOM); }
        return List.of(RecipeType.createFromVanilla(cokeOven.get()), ADVANCED_COKE_OVEN_CUSTOM);
    }

    private static <T extends Recipe<?>> RecipeType<T> create(ICRecipeType<T> type) {
        return new RecipeType<>(type.type().getId(), type.recipeClass());
    }
}
