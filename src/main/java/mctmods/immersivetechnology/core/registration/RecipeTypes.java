package mctmods.immersivetechnology.core.registration;

import com.immersiveconvergence.api.crafting.ICRecipeType;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.*;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.serializer.*;
import mctmods.immersivetechnology.common.multiblocks.stone.recipe.AdvancedCokeOvenRecipe;
import mctmods.immersivetechnology.common.multiblocks.stone.recipe.CoolingTowerRecipe;
import mctmods.immersivetechnology.common.multiblocks.stone.recipe.serializer.AdvancedCokeOvenRecipeSerializer;
import mctmods.immersivetechnology.common.multiblocks.stone.recipe.serializer.CoolingTowerRecipeSerializer;
import mctmods.immersivetechnology.core.lib.Reference;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class RecipeTypes {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, Reference.MODID);
    private static final DeferredRegister<RecipeType<?>> REGISTER = DeferredRegister.create(Registries.RECIPE_TYPE, Reference.MODID);

    public static final ICRecipeType<AdvancedCokeOvenRecipe> ADVANCED_COKE_OVEN = register("advanced_coke_oven", AdvancedCokeOvenRecipe.class);
    public static final ICRecipeType<BoilerLiquidRecipe> BOILER_LIQUID = register("boiler_liquid", BoilerLiquidRecipe.class);
    public static final ICRecipeType<BoilerSolidRecipe> BOILER_SOLID = register("boiler_solid", BoilerSolidRecipe.class);
    public static final ICRecipeType<BoilerTankRecipe> BOILER_TANK = register("boiler_tank", BoilerTankRecipe.class);
    public static final ICRecipeType<CoolingTowerRecipe> COOLING_TOWER = register("cooling_tower", CoolingTowerRecipe.class);
    public static final ICRecipeType<DistillerRecipe> DISTILLER = register("distiller", DistillerRecipe.class);
    public static final ICRecipeType<ElectrolyticCrucibleBatteryRecipe> ELECTROLYTIC_CRUCIBLE_BATTERY = register("electrolytic_crucible_battery", ElectrolyticCrucibleBatteryRecipe.class);
    public static final ICRecipeType<GasTurbineRecipe> GAS_TURBINE = register("gas_turbine", GasTurbineRecipe.class);
    public static final ICRecipeType<HeatExchangerRecipe> HEAT_EXCHANGER = register("heat_exchanger", HeatExchangerRecipe.class);
    public static final ICRecipeType<MeltingRecipe> MELTING = register("melting", MeltingRecipe.class);
    public static final ICRecipeType<RadiatorRecipe> RADIATOR = register("radiator", RadiatorRecipe.class);
    public static final ICRecipeType<SolarTowerRecipe> SOLAR_TOWER = register("solar_tower", SolarTowerRecipe.class);
    public static final ICRecipeType<SteamTurbineRecipe> STEAM_TURBINE = register("steam_turbine", SteamTurbineRecipe.class);

    static {
        AdvancedCokeOvenRecipe.SERIALIZER = RECIPE_SERIALIZERS.register("advanced_coke_oven", AdvancedCokeOvenRecipeSerializer::new);
        BoilerLiquidRecipe.SERIALIZER = RECIPE_SERIALIZERS.register("boiler_liquid", BoilerLiquidRecipeSerializer::new);
        BoilerSolidRecipe.SERIALIZER = RECIPE_SERIALIZERS.register("boiler_solid", BoilerSolidRecipeSerializer::new);
        BoilerTankRecipe.SERIALIZER = RECIPE_SERIALIZERS.register("boiler_tank", BoilerTankRecipeSerializer::new);
        CoolingTowerRecipe.SERIALIZER = RECIPE_SERIALIZERS.register("cooling_tower", CoolingTowerRecipeSerializer::new);
        DistillerRecipe.SERIALIZER = RECIPE_SERIALIZERS.register("distiller", DistillerRecipeSerializer::new);
        ElectrolyticCrucibleBatteryRecipe.SERIALIZER = RECIPE_SERIALIZERS.register("electrolytic_crucible_battery", ElectrolyticCrucibleBatteryRecipeSerializer::new);
        GasTurbineRecipe.SERIALIZER = RECIPE_SERIALIZERS.register("gas_turbine", GasTurbineRecipeSerializer::new);
        HeatExchangerRecipe.SERIALIZER = RECIPE_SERIALIZERS.register("heat_exchanger", HeatExchangerRecipeSerializer::new);
        MeltingRecipe.SERIALIZER = RECIPE_SERIALIZERS.register("melting", MeltingRecipeSerializer::new);
        RadiatorRecipe.SERIALIZER = RECIPE_SERIALIZERS.register("radiator", RadiatorRecipeSerializer::new);
        SolarTowerRecipe.SERIALIZER = RECIPE_SERIALIZERS.register("solar_tower", SolarTowerRecipeSerializer::new);
        SteamTurbineRecipe.SERIALIZER = RECIPE_SERIALIZERS.register("steam_turbine", SteamTurbineRecipeSerializer::new);
    }

    private static <T extends Recipe<?>> ICRecipeType<T> register(String name, Class<T> type) { return ICRecipeType.register(REGISTER, name, type); }

    public static void init(IEventBus modEventBus) {
        REGISTER.register(modEventBus);
        RECIPE_SERIALIZERS.register(modEventBus);
    }
}
