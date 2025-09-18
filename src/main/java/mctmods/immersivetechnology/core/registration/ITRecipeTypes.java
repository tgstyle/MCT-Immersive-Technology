package mctmods.immersivetechnology.core.registration;

import blusunrize.immersiveengineering.api.crafting.IERecipeTypes.TypeWithClass;
import mctmods.immersivetechnology.common.blocks.multiblocks.recipe.*;
import mctmods.immersivetechnology.common.blocks.multiblocks.recipe.serializer.*;
import mctmods.immersivetechnology.core.lib.ITLib;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ITRecipeTypes {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, ITLib.MODID);
    private static final DeferredRegister<RecipeType<?>> REGISTER = DeferredRegister.create(Registries.RECIPE_TYPE, ITLib.MODID);

    public static final TypeWithClass<AdvancedCokeOvenRecipe> ADVANCED_COKE_OVEN = register("advanced_coke_oven", AdvancedCokeOvenRecipe.class);
    public static final TypeWithClass<AdvancedCokeOvenFuel> ADVANCED_COKE_OVEN_FUEL = register("advanced_coke_oven_fuel", AdvancedCokeOvenFuel.class);
    public static final TypeWithClass<BoilerLiquidRecipe> BOILER_LIQUID = register("boiler_liquid", BoilerLiquidRecipe.class);
    public static final TypeWithClass<BoilerSolidRecipe> BOILER_SOLID = register("boiler_solid", BoilerSolidRecipe.class);
    public static final TypeWithClass<BoilerTankRecipe> BOILER_TANK = register("boiler_tank", BoilerTankRecipe.class);
    public static final TypeWithClass<DistillerRecipe> DISTILLER = register("distiller", DistillerRecipe.class);
    public static final TypeWithClass<GasTurbineRecipe> GAS_TURBINE = register("gas_turbine", GasTurbineRecipe.class);
    public static final TypeWithClass<SolarMelterRecipe> SOLAR_MELTER = register("solar_melter", SolarMelterRecipe.class);
    public static final TypeWithClass<SolarTowerRecipe> SOLAR_TOWER = register("solar_tower", SolarTowerRecipe.class);
    public static final TypeWithClass<SteamTurbineRecipe> STEAM_TURBINE = register("steam_turbine", SteamTurbineRecipe.class);

    static {
        AdvancedCokeOvenRecipe.SERIALIZER = RECIPE_SERIALIZERS.register("advanced_coke_oven", AdvancedCokeOvenRecipeSerializer::new);
        AdvancedCokeOvenFuel.SERIALIZER = RECIPE_SERIALIZERS.register("advanced_coke_oven_fuel", AdvancedCokeOvenFuelSerializer::new);
        BoilerLiquidRecipe.SERIALIZER = RECIPE_SERIALIZERS.register("boiler_liquid", BoilerLiquidRecipeSerializer::new);
        BoilerSolidRecipe.SERIALIZER = RECIPE_SERIALIZERS.register("boiler_solid", BoilerSolidRecipeSerializer::new);
        BoilerTankRecipe.SERIALIZER = RECIPE_SERIALIZERS.register("boiler", BoilerTankRecipeSerializer::new);
        GasTurbineRecipe.SERIALIZER = RECIPE_SERIALIZERS.register("gas_turbine", GasTurbineRecipeSerializer::new);
        DistillerRecipe.SERIALIZER = RECIPE_SERIALIZERS.register("distiller", DistillerRecipeSerializer::new);
        SolarMelterRecipe.SERIALIZER = RECIPE_SERIALIZERS.register("solar_melter", SolarMelterRecipeSerializer::new);
        SolarTowerRecipe.SERIALIZER = RECIPE_SERIALIZERS.register("solar_tower", SolarTowerRecipeSerializer::new);
        SteamTurbineRecipe.SERIALIZER = RECIPE_SERIALIZERS.register("steam_turbine", SteamTurbineRecipeSerializer::new);
    }

    private static <T extends Recipe<?>> TypeWithClass<T> register(String name, Class<T> type) { return new TypeWithClass<>(REGISTER.register(name, () -> new RecipeType<>() {}), type); }

    public static void init(IEventBus modEventBus) { REGISTER.register(modEventBus); RECIPE_SERIALIZERS.register(modEventBus); }
}
