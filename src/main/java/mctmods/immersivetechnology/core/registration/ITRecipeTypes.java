package mctmods.immersivetechnology.core.registration;

import blusunrize.immersiveengineering.api.crafting.IERecipeTypes.TypeWithClass;
import mctmods.immersivetechnology.common.blocks.multiblocks.recipe.*;
import mctmods.immersivetechnology.common.blocks.multiblocks.recipe.serializer.*;
import mctmods.immersivetechnology.core.lib.ITLib;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ITRecipeTypes {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create( ForgeRegistries.RECIPE_SERIALIZERS, ITLib.MODID );
    private static final DeferredRegister<RecipeType<?>> REGISTER = DeferredRegister.create(Registries.RECIPE_TYPE, ITLib.MODID);

    public static final TypeWithClass<AdvancedCokeOvenRecipe> ADVANCED_COKE_OVEN = register("coke_oven_advanced", AdvancedCokeOvenRecipe.class);
    public static final TypeWithClass<BoilerRecipe> BOILER = register("boiler", BoilerRecipe.class);
    public static final TypeWithClass<SteamTurbineRecipe> STEAM_TURBINE = register("steam_turbine", SteamTurbineRecipe.class);
    public static final TypeWithClass<GasTurbineRecipe> GAS_TURBINE = register("gas_turbine", GasTurbineRecipe.class);
    public static final TypeWithClass<SolarTowerRecipe> SOLAR_TOWER = register("solar_tower", SolarTowerRecipe.class);
    public static final TypeWithClass<? extends Recipe<?>> ADV_COKE_OVEN_FUEL = register("coke_oven_advanced_fuel", AdvancedCokeOvenFuel.class);

    static {
        AdvancedCokeOvenRecipe.SERIALIZER = RECIPE_SERIALIZERS.register("coke_oven_advanced", AdvancedCokeOvenRecipeSerializer::new);
        BoilerRecipe.SERIALIZER = RECIPE_SERIALIZERS.register("boiler", BoilerRecipeSerializer::new);
        SolarTowerRecipe.SERIALIZER = RECIPE_SERIALIZERS.register("solar_tower", SolarTowerRecipeSerializer::new);
        SteamTurbineRecipe.SERIALIZER = RECIPE_SERIALIZERS.register("steam_turbine", SteamTurbineRecipeSerializer::new);
        GasTurbineRecipe.SERIALIZER = RECIPE_SERIALIZERS.register("gas_turbine", GasTurbineRecipeSerializer::new);
    }

    private static <T extends Recipe<?>> TypeWithClass<T> register(String name, Class<T> type) {
        RegistryObject<RecipeType<T>> regObj = REGISTER.register(name, () -> new RecipeType<>() { });
        return new TypeWithClass<>(regObj, type);
    }

    public static void init() {
        REGISTER.register(FMLJavaModLoadingContext.get().getModEventBus());
        RECIPE_SERIALIZERS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
