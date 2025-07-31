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

    public static final TypeWithClass<AdvancedCokeOvenRecipe> ADVANCED_COKE_OVEN = register("coke_oven_advanced", AdvancedCokeOvenRecipe.class);
    public static final TypeWithClass<BoilerRecipe> BOILER = register("boiler", BoilerRecipe.class);
    public static final TypeWithClass<BoilerFuelRecipe> BOILER_FUEL = register("boiler_fuel", BoilerFuelRecipe.class);
    public static final TypeWithClass<SteamTurbineRecipe> STEAM_TURBINE = register("steam_turbine", SteamTurbineRecipe.class);
    public static final TypeWithClass<GasTurbineRecipe> GAS_TURBINE = register("gas_turbine", GasTurbineRecipe.class);
    public static final TypeWithClass<SolarTowerRecipe> SOLAR_TOWER = register("solar_tower", SolarTowerRecipe.class);
    public static final TypeWithClass<DistillerRecipe> DISTILLER = register("distiller", DistillerRecipe.class);
    public static final TypeWithClass<AdvancedCokeOvenFuel> ADV_COKE_OVEN_FUEL = register("coke_oven_advanced_fuel", AdvancedCokeOvenFuel.class);

    static {
        AdvancedCokeOvenRecipe.SERIALIZER = RECIPE_SERIALIZERS.register("coke_oven_advanced", AdvancedCokeOvenRecipeSerializer::new);
        BoilerRecipe.SERIALIZER = RECIPE_SERIALIZERS.register("boiler", BoilerRecipeSerializer::new);
        BoilerFuelRecipe.SERIALIZER = RECIPE_SERIALIZERS.register("boiler_fuel", BoilerFuelRecipeSerializer::new);
        SolarTowerRecipe.SERIALIZER = RECIPE_SERIALIZERS.register("solar_tower", SolarTowerRecipeSerializer::new);
        DistillerRecipe.SERIALIZER = RECIPE_SERIALIZERS.register("distiller", DistillerRecipeSerializer::new);
        SteamTurbineRecipe.SERIALIZER = RECIPE_SERIALIZERS.register("steam_turbine", SteamTurbineRecipeSerializer::new);
        GasTurbineRecipe.SERIALIZER = RECIPE_SERIALIZERS.register("gas_turbine", GasTurbineRecipeSerializer::new);
        AdvancedCokeOvenFuel.SERIALIZER = RECIPE_SERIALIZERS.register("coke_oven_advanced_fuel", AdvancedCokeOvenFuelSerializer::new);
    }

    private static <T extends Recipe<?>> TypeWithClass<T> register(String name, Class<T> type) { return new TypeWithClass<>(REGISTER.register(name, () -> new RecipeType<>() {}), type); }

    public static void init(IEventBus modEventBus) { REGISTER.register(modEventBus); RECIPE_SERIALIZERS.register(modEventBus); }
}
