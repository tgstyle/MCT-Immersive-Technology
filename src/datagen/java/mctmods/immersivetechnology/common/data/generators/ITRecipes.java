package mctmods.immersivetechnology.common.data.generators;

import blusunrize.immersiveengineering.api.IETags;
import blusunrize.immersiveengineering.api.EnumMetals;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.common.register.IEBlocks;
import blusunrize.immersiveengineering.common.register.IEItems;
import blusunrize.immersiveengineering.data.recipes.builder.MixerRecipeBuilder;
import mctmods.immersivetechnology.common.multiblocks.metal.recipe.builder.*;
import mctmods.immersivetechnology.common.multiblocks.stone.recipe.builder.CoolingTowerRecipeBuilder;
import mctmods.immersivetechnology.core.lib.ITLib;
import mctmods.immersivetechnology.core.registration.ITBlocks;
import mctmods.immersivetechnology.core.registration.ITFluids;
import mctmods.immersivetechnology.core.registration.ITItems;
import mctmods.immersivetechnology.core.registration.ITTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.conditions.ModLoadedCondition;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

public class ITRecipes extends RecipeProvider {

    private final HashMap<String, Integer> PATH_COUNT = new HashMap<>();

    public ITRecipes(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override protected void buildRecipes(@NotNull RecipeOutput output) {
        itemRecipes(output);
        recipesBoilerLiquid(output);
        recipesBoilerSolid(output);
        recipesBoilerTank(output);
        recipesDistiller(output);
        recipesHeatExchanger(output);
        recipesMixer(output);
        recipesSolarTower(output);
        recipesRadiator(output);
        recipesMelting(output);
        recipesElectrolyticCrucibleBattery(output);
        recipesGasTurbine(output);
        recipesSteamTurbine(output);
        recipesCoolingTower(output);
    }

    private void itemRecipes(RecipeOutput output) {
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ITBlocks.Stone.REINFORCED_COKE_BRICK.get().asItem(), 1).define('P', IETags.getTagsFor(EnumMetals.STEEL).plate).define('C', IEBlocks.StoneDecoration.COKEBRICK.get()).pattern("P").pattern("C").unlockedBy("has_steel_plate", has(IETags.getTagsFor(EnumMetals.STEEL).plate)).save(output, toResourceLocation("reinforced_coke_brick"));
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ITBlocks.Stone.REINFORCED_COKE_BRICK.get().asItem(), 1).define('S', ITBlocks.Stone.SLAB_REINFORCED_COKE_BRICK.get()).pattern("S").pattern("S").unlockedBy("has_slab_reinforced_coke_brick", has(ITBlocks.Stone.SLAB_REINFORCED_COKE_BRICK.get())).save(output, toResourceLocation("reinforced_coke_brick_slab_back"));
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ITBlocks.Stone.SLAB_REINFORCED_COKE_BRICK.get().asItem(), 6).define('B', ITBlocks.Stone.REINFORCED_COKE_BRICK.get()).pattern("BBB").unlockedBy("has_reinforced_coke_brick", has(ITBlocks.Stone.REINFORCED_COKE_BRICK.get())).save(output, toResourceLocation("reinforced_coke_brick_slab"));
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ITBlocks.Metal.TECHNOLOGY_ENGINEERING.get().asItem(), 4).define('C', IETags.getItemTag(IETags.getTagsFor(EnumMetals.COPPER).sheetmetal)).define('S', Ingredient.of(IEItems.Ingredients.COMPONENT_STEEL.get())).define('E', IETags.getTagsFor(EnumMetals.ELECTRUM).ingot).pattern("CSC").pattern("SES").pattern("CSC").unlockedBy("has_copper_sheetmetal", has(IETags.getItemTag(IETags.getTagsFor(EnumMetals.COPPER).sheetmetal))).save(output, toResourceLocation("technology_engineering"));
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ITBlocks.Metal.BARREL_OPEN.get().asItem(), 1).define('S', Ingredient.of(IEBlocks.TO_SLAB.get(IEBlocks.Metals.SHEETMETAL.get(EnumMetals.IRON).getId()).get())).define('B', Ingredient.of(IEBlocks.Metals.SHEETMETAL.get(EnumMetals.IRON).get())).pattern("S S").pattern("B B").pattern("BBB").unlockedBy("has_slab_sheetmetal_iron", has(IEBlocks.TO_SLAB.get(IEBlocks.Metals.SHEETMETAL.get(EnumMetals.IRON).getId()).get())).save(output, toResourceLocation("barrel_open"));
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ITBlocks.Metal.BARREL_STEEL.get().asItem(), 1).define('S', Ingredient.of(IEBlocks.TO_SLAB.get(IEBlocks.Metals.SHEETMETAL.get(EnumMetals.STEEL).getId()).get())).define('B', Ingredient.of(IEBlocks.Metals.SHEETMETAL.get(EnumMetals.STEEL).get())).pattern("SSS").pattern("B B").pattern("BBB").unlockedBy("has_slab_sheetmetal_steel", has(IEBlocks.TO_SLAB.get(IEBlocks.Metals.SHEETMETAL.get(EnumMetals.STEEL).getId()).get())).save(output, toResourceLocation("barrel_steel"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ITBlocks.Metal.TRASH_ITEM.get().asItem(), 1).define('P', IETags.getTagsFor(EnumMetals.IRON).plate).define('C', Tags.Items.CHESTS).define('S', IETags.getItemTag(IETags.getTagsFor(EnumMetals.IRON).sheetmetal)).pattern("PPP").pattern("PCP").pattern(" S ").unlockedBy("has_iron_plate", has(IETags.getTagsFor(EnumMetals.IRON).plate)).save(output, toResourceLocation("trash_item"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ITBlocks.Metal.TRASH_FLUID.get().asItem(), 1).define('P', IETags.getTagsFor(EnumMetals.IRON).plate).define('B', IEBlocks.MetalDevices.FLUID_PUMP.get()).define('S', IETags.getItemTag(IETags.getTagsFor(EnumMetals.IRON).sheetmetal)).pattern("PPP").pattern("PBP").pattern(" S ").unlockedBy("has_iron_plate", has(IETags.getTagsFor(EnumMetals.IRON).plate)).save(output, toResourceLocation("trash_fluid"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ITBlocks.Metal.TRASH_ENERGY.get().asItem(), 1).define('P', IETags.getTagsFor(EnumMetals.IRON).plate).define('C', IEBlocks.MetalDecoration.HV_COIL.get()).define('S', IETags.getItemTag(IETags.getTagsFor(EnumMetals.IRON).sheetmetal)).pattern("PPP").pattern("PCP").pattern(" S ").unlockedBy("has_iron_plate", has(IETags.getTagsFor(EnumMetals.IRON).plate)).save(output, toResourceLocation("trash_energy"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ITBlocks.Metal.VALVE_FLUID.get().asItem(), 2).define('L', IETags.getTagsFor(EnumMetals.IRON).plate).define('R', IEBlocks.Connectors.CONNECTOR_REDSTONE.get()).define('P', IEBlocks.MetalDevices.FLUID_PIPE.get()).define('I', IEItems.Ingredients.COMPONENT_IRON.get()).define('C', IEItems.Ingredients.CIRCUIT_BOARD.get()).pattern("LRL").pattern("PIP").pattern("LCL").unlockedBy("has_iron_plate", has(IETags.getTagsFor(EnumMetals.IRON).plate)).save(output, toResourceLocation("valve_fluid"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ITBlocks.Metal.VALVE_LIMITER.get().asItem(), 2).define('L', IETags.getTagsFor(EnumMetals.IRON).plate).define('R', IEBlocks.Connectors.CONNECTOR_REDSTONE.get()).define('P', BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath("immersiveengineering", "conveyor_basic"))).define('I', IEItems.Ingredients.COMPONENT_IRON.get()).define('C', IEItems.Ingredients.CIRCUIT_BOARD.get()).pattern("LRL").pattern("PIP").pattern("LCL").unlockedBy("has_iron_plate", has(IETags.getTagsFor(EnumMetals.IRON).plate)).save(output, toResourceLocation("valve_limiter"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ITBlocks.Metal.VALVE_LOAD.get().asItem(), 2).define('L', IETags.getTagsFor(EnumMetals.IRON).plate).define('R', IEBlocks.Connectors.CONNECTOR_REDSTONE.get()).define('P', IEBlocks.Connectors.getEnergyConnector("HV", false).get()).define('I', IEItems.Ingredients.COMPONENT_IRON.get()).define('C', IEItems.Ingredients.CIRCUIT_BOARD.get()).pattern("LRL").pattern("PIP").pattern("LCL").unlockedBy("has_iron_plate", has(IETags.getTagsFor(EnumMetals.IRON).plate)).save(output, toResourceLocation("valve_load"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ITBlocks.Metal.ADVANCED_COKE_OVEN_BASEHEATER.get().asItem(), 1).define('S', IETags.getItemTag(IETags.getTagsFor(EnumMetals.IRON).sheetmetal)).define('H', IEBlocks.MetalDevices.FURNACE_HEATER.get()).define('R', Items.REDSTONE).pattern("SSS").pattern("HRH").pattern("SSS").unlockedBy("has_furnace_heater", has(IEBlocks.MetalDevices.FURNACE_HEATER.get())).save(output, toResourceLocation("advanced_coke_oven_baseheater"));
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ITItems.FORMATION_TOOL.get(), 1).define('I', IETags.getTagsFor(EnumMetals.IRON).ingot).define('E', IETags.getTagsFor(EnumMetals.ELECTRUM).ingot).pattern(" I ").pattern(" EI").pattern("I  ").unlockedBy("has_iron_ingot", has(IETags.getTagsFor(EnumMetals.IRON).ingot)).save(output, toResourceLocation("formation_tool"));
    }

    private void recipesBoilerLiquid(RecipeOutput output) {
        BoilerLiquidRecipeBuilder.builder().addInput(IETags.fluidBiodiesel, 10).setTime(10).setHeatPerTick(0.1).setTargetHeat(600.0).save(output, toResourceLocation("boiler_liquid/biodiesel"));
        BoilerLiquidRecipeBuilder.builder().addInput(FluidTags.create(ResourceLocation.fromNamespaceAndPath("forge", "gasoline")), 50).setTime(10).setHeatPerTick(0.1).setTargetHeat(600.0).save(output.withConditions(new ModLoadedCondition("immersivepetroleum")), toResourceLocation("boiler_liquid/gasoline"));
        BoilerLiquidRecipeBuilder.builder().addInput(FluidTags.create(ResourceLocation.fromNamespaceAndPath("forge", "diesel")), 14).setTime(10).setHeatPerTick(0.1).setTargetHeat(600.0).save(output.withConditions(new ModLoadedCondition("immersivepetroleum")), toResourceLocation("boiler_liquid/diesel"));
        BoilerLiquidRecipeBuilder.builder().addInput(FluidTags.create(ResourceLocation.fromNamespaceAndPath("forge", "kerosene")), 9).setTime(10).setHeatPerTick(0.1).setTargetHeat(600.0).save(output.withConditions(new ModLoadedCondition("immersivepetroleum")), toResourceLocation("boiler_liquid/kerosene"));
    }

    private void recipesBoilerSolid(RecipeOutput output) {
        BoilerSolidRecipeBuilder.builder().addInput(ItemTags.COALS, 1).setHeatPerTick(0.1).setTargetHeat(600.0).save(output, toResourceLocation("boiler_solid/coal"));
        BoilerSolidRecipeBuilder.builder().addInput(IETags.coalCoke, 1).setHeatPerTick(0.1).setTargetHeat(600.0).save(output, toResourceLocation("boiler_solid/coal_coke"));
    }

    private void recipesBoilerTank(RecipeOutput output) {
        BoilerTankRecipeBuilder.builder().addInput(FluidTags.WATER, 250).addOutput(ITFluids.STEAM.getStill(), 450).setTime(10).setRequiredHeat(600.0).save(output, toResourceLocation("boiler_tank/water"));
        BoilerTankRecipeBuilder.builder().addInput(ITTags.fluidDistilledWater, 250).addOutput(ITFluids.STEAM.getStill(), 500).setTime(10).setRequiredHeat(600.0).save(output, toResourceLocation("boiler_tank/distilled_water"));
    }

    private void recipesDistiller(RecipeOutput output) {
        ItemStack salt = new ItemStack(ITItems.SALT.get(), 1);
        DistillerRecipeBuilder.builder().addInput(FluidTags.WATER, 1000).addOutput(new FluidStack(ITFluids.DISTILLED_WATER.getStill(), 500)).setTime(20).setEnergy(10000).addItemOutput(salt, 0.5f).save(output, toResourceLocation("distiller/water"));
    }

    private void recipesHeatExchanger(RecipeOutput output) {
        HeatExchangerRecipeBuilder.builder().addInput0(FluidTags.WATER, 250).addInput1(ITTags.fluidFlueGas, 1000).addOutput0(new FluidStack(ITFluids.STEAM.getStill(), 450)).setEnergy(640).setTime(10).save(output, toResourceLocation("heat_exchanger/water_fluegas"));
        HeatExchangerRecipeBuilder.builder().addInput0(ITTags.fluidDistilledWater, 250).addInput1(ITTags.fluidFlueGas, 1000).addOutput0(new FluidStack(ITFluids.STEAM.getStill(), 500)).setEnergy(640).setTime(10).save(output, toResourceLocation("heat_exchanger/distwater_fluegas"));
        HeatExchangerRecipeBuilder.builder().addInput0(FluidTags.WATER, 250).addInput1(ITTags.fluidMoltenSalt, 80).addOutput0(new FluidStack(ITFluids.STEAM.getStill(), 450)).addOutput1(new FluidStack(ITFluids.HEATED_SALT.getStill(), 80)).setEnergy(640).setTime(10).save(output, toResourceLocation("heat_exchanger/water_moltensalt"));
        HeatExchangerRecipeBuilder.builder().addInput0(ITTags.fluidDistilledWater, 250).addInput1(ITTags.fluidMoltenSalt, 80).addOutput0(new FluidStack(ITFluids.STEAM.getStill(), 500)).addOutput1(new FluidStack(ITFluids.HEATED_SALT.getStill(), 80)).setEnergy(640).setTime(10).save(output, toResourceLocation("heat_exchanger/distwater_moltensalt"));
        HeatExchangerRecipeBuilder.builder().addInput0(ITTags.fluidExhaustSteam, 500).addInput1(FluidTags.WATER, 4500).addOutput0(new FluidStack(ITFluids.DISTILLED_WATER.getStill(), 250)).addOutput1(new FluidStack(ITFluids.HOT_WATER.getStill(), 4500)).setEnergy(160).setTime(5).save(output, toResourceLocation("heat_exchanger/exhauststeam_water"));
    }

    private void recipesMixer(RecipeOutput output) {
        MixerRecipeBuilder.builder().output(ITFluids.SALT_SLURRY.getStill(), 1000).fluidInput(FluidTags.WATER, 1000).input(new IngredientWithSize(ITTags.saltForge, 4)).setEnergy(3200).build(output, toResourceLocation("mixer/salt_slurry"));
        MixerRecipeBuilder.builder().output(ITFluids.GRAVEL_SLURRY.getStill(), 1000).fluidInput(FluidTags.WATER, 1000).input(new IngredientWithSize(Tags.Items.GRAVELS, 4)).setEnergy(3200).build(output, toResourceLocation("mixer/gravel_slurry"));
    }

    private void recipesSolarTower(RecipeOutput output) {
        SolarTowerRecipeBuilder.builder().addInput(FluidTags.WATER, 250).addOutput(new FluidStack(ITFluids.STEAM.getStill(), 450)).setTime(10).setRequiredTemp(600.0).save(output, toResourceLocation("solar_tower/water"));
        SolarTowerRecipeBuilder.builder().addInput(ITTags.fluidDistilledWater, 250).addOutput(new FluidStack(ITFluids.STEAM.getStill(), 500)).setTime(10).setRequiredTemp(600.0).save(output, toResourceLocation("solar_tower/distilled_water"));
        SolarTowerRecipeBuilder.builder().addInput(ITTags.fluidSaltSlurry, 1000).addOutput(new FluidStack(ITFluids.HEATED_SALT.getStill(), 500)).setTime(10).setRequiredTemp(400.0).save(output, toResourceLocation("solar_tower/salt_slurry"));
        SolarTowerRecipeBuilder.builder().addInput(ITTags.fluidGravelSlurry, 1000).addOutput(new FluidStack(ITFluids.HEATED_GRAVEL.getStill(), 500)).setTime(10).setRequiredTemp(400.0).save(output, toResourceLocation("solar_tower/gravel_slurry"));
    }

    private void recipesRadiator(RecipeOutput output) {
        RadiatorRecipeBuilder.builder().addInput(ITTags.fluidExhaustSteam, 500).addOutput(new FluidStack(ITFluids.DISTILLED_WATER.getStill(), 250)).setTime(80).save(output, toResourceLocation("radiator/exhaust_steam"));
        RadiatorRecipeBuilder.builder().addInput(ITTags.fluidHotWater, 1000).addOutput(new FluidStack(Fluids.WATER, 800)).setTime(20).save(output, toResourceLocation("radiator/hot_water"));
    }

    private void recipesMelting(RecipeOutput output) {
        MeltingRecipeBuilder.builder().addInput(ITTags.fluidHeatedSaltSlurry, 1000).addOutput(new FluidStack(ITFluids.MOLTEN_SALT.getStill(), 500)).setTime(20).setRequiredTemp(1000.0).save(output, toResourceLocation("melting/heated_salt"));
        MeltingRecipeBuilder.builder().addInput(ITTags.fluidHeatedGravelSlurry, 1000).addOutput(new FluidStack(Fluids.LAVA, 500)).setTime(20).setRequiredTemp(1000.0).save(output, toResourceLocation("melting/heated_gravel_slurry"));
    }

    private void recipesElectrolyticCrucibleBattery(RecipeOutput output) {
        ElectrolyticCrucibleBatteryRecipeBuilder.builder().addInput(ITTags.fluidSaltSlurry, 1000).addOutput0(new FluidStack(ITFluids.CHLORINE.getStill(), 1000)).addOutput1(new FluidStack(ITFluids.HYDROGEN.getStill(), 3000)).addOutput2(new FluidStack(ITFluids.HEATED_SALT.getStill(), 700)).addItemOutput(ItemStack.EMPTY).setEnergy(614400).setTime(300).save(output, toResourceLocation("electrolytic_crucible_battery/hydrogen"));
    }

    private void recipesGasTurbine(RecipeOutput output) {
        GasTurbineRecipeBuilder.builder().addInput(IETags.fluidBiodiesel, 160).addOutput(ITFluids.FLUE_GAS.getStill(), 1000).setTime(10).setTorque(1.0f).save(output, toResourceLocation("gas_turbine/biodiesel"));
        GasTurbineRecipeBuilder.builder().addInput(ITTags.fluidHydrogen, 100).addOutput(ITFluids.FLUE_GAS.getStill(), 1000).setTime(10).setTorque(1.0f).save(output, toResourceLocation("gas_turbine/hydrogen"));
        GasTurbineRecipeBuilder.builder().addInput(FluidTags.create(ResourceLocation.fromNamespaceAndPath("forge", "gasoline")), 800).addOutput(ITFluids.FLUE_GAS.getStill(), 1000).setTime(10).setTorque(1.0f).save(output.withConditions(new ModLoadedCondition("immersivepetroleum")), toResourceLocation("gas_turbine/gasoline"));
        GasTurbineRecipeBuilder.builder().addInput(FluidTags.create(ResourceLocation.fromNamespaceAndPath("forge", "diesel")), 114).addOutput(ITFluids.FLUE_GAS.getStill(), 1000).setTime(10).setTorque(1.0f).save(output.withConditions(new ModLoadedCondition("immersivepetroleum")), toResourceLocation("gas_turbine/diesel"));
        GasTurbineRecipeBuilder.builder().addInput(FluidTags.create(ResourceLocation.fromNamespaceAndPath("forge", "kerosene")), 150).addOutput(ITFluids.FLUE_GAS.getStill(), 1000).setTime(10).setTorque(1.0f).save(output.withConditions(new ModLoadedCondition("immersivepetroleum")), toResourceLocation("gas_turbine/kerosene"));
    }

    private void recipesSteamTurbine(RecipeOutput output) {
        SteamTurbineRecipeBuilder.builder().addInput(ITTags.fluidSteam, 100).addOutput(ITFluids.EXHAUST_STEAM.getStill(), 100).setTime(1).setTorque(1.0f).save(output, toResourceLocation("steam_turbine/steam"));
    }

    private void recipesCoolingTower(RecipeOutput output) {
        CoolingTowerRecipeBuilder.builder().addInput(ITTags.fluidHotWater, 8100).addInput(FluidTags.WATER, 900).addOutput(Fluids.WATER, 2925).addOutput(Fluids.WATER, 2925).addOutput(Fluids.WATER, 2925).setTime(3).save(output, toResourceLocation("cooling_tower/hot_water"));
    }

    private ResourceLocation toResourceLocation(String resourceLocation) {
        if (!resourceLocation.contains("/")) { resourceLocation = "crafting/" + resourceLocation; }
        if (PATH_COUNT.containsKey(resourceLocation)) {
            int count = PATH_COUNT.get(resourceLocation) + 1;
            PATH_COUNT.put(resourceLocation, count);
            return ITLib.rl(resourceLocation + count);
        }
        PATH_COUNT.put(resourceLocation, 1);
        return ITLib.rl(resourceLocation);
    }
}
