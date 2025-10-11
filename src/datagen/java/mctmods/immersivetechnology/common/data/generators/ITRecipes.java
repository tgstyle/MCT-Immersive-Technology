package mctmods.immersivetechnology.common.data.generators;

import blusunrize.immersiveengineering.api.IETags;
import blusunrize.immersiveengineering.api.EnumMetals;
import blusunrize.immersiveengineering.api.crafting.FluidTagInput;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.api.crafting.builders.MixerRecipeBuilder;
import blusunrize.immersiveengineering.common.register.IEBlocks;
import blusunrize.immersiveengineering.common.register.IEItems;
import mctmods.immersivetechnology.common.blocks.multiblocks.recipe.builder.*;
import mctmods.immersivetechnology.core.lib.ITLib;
import mctmods.immersivetechnology.core.registration.ITBlocks;
import mctmods.immersivetechnology.core.registration.ITFluids;
import mctmods.immersivetechnology.core.registration.ITItems;
import mctmods.immersivetechnology.core.registration.ITTags;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.Tags;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.function.Consumer;

public class ITRecipes extends RecipeProvider {
    private final HashMap<String, Integer> PATH_COUNT = new HashMap<>();

    public ITRecipes(PackOutput pOutput) { super(pOutput); }

    @Override
    protected void buildRecipes(@NotNull Consumer<FinishedRecipe> consumer) {
        multiblockRecipes();
        itemRecipes(consumer);
        recipesBoilerTank(consumer);
        recipesBoilerLiquid(consumer);
        recipesBoilerSolid(consumer);
        recipesCoolingTower(consumer);
        recipesDistiller(consumer);
        recipesMixer(consumer);
        recipesTurbine(consumer);
        recipesSolarMelter(consumer);
        recipesSolarTower(consumer);
    }

    private void multiblockRecipes() { ITLib.IT_LOGGER.info("Starting Multiblock Recipe Registration"); }

    private void itemRecipes(@Nonnull Consumer<FinishedRecipe> consumer) {
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ITBlocks.Stone.REINFORCED_COKE_BRICK.get().asItem(), 1).define('P', IETags.getTagsFor(EnumMetals.STEEL).plate).define('C', IEBlocks.StoneDecoration.COKEBRICK.get()).pattern("P").pattern("C").unlockedBy("has_steel_plate", has(IETags.getTagsFor(EnumMetals.STEEL).plate)).save(consumer, toResourceLocation("reinforced_coke_brick"));
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ITBlocks.Stone.REINFORCED_COKE_BRICK.get().asItem(), 1).define('S', ITBlocks.Stone.SLAB_REINFORCED_COKE_BRICK.get()).pattern("S").pattern("S").unlockedBy("has_slab_reinforced_coke_brick", has(ITBlocks.Stone.SLAB_REINFORCED_COKE_BRICK.get())).save(consumer, toResourceLocation("reinforced_coke_brick_slab_back"));
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ITBlocks.Stone.SLAB_REINFORCED_COKE_BRICK.get().asItem(), 6).define('B', ITBlocks.Stone.REINFORCED_COKE_BRICK.get()).pattern("BBB").unlockedBy("has_reinforced_coke_brick", has(ITBlocks.Stone.REINFORCED_COKE_BRICK.get())).save(consumer, toResourceLocation("reinforced_coke_brick_slab"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ITBlocks.MetalDevices.TRASH_ITEM.get().asItem(), 1).define('P', IETags.getTagsFor(EnumMetals.IRON).plate).define('C', Tags.Items.CHESTS_WOODEN).define('S', IETags.getItemTag(IETags.getTagsFor(EnumMetals.IRON).sheetmetal)).pattern("PPP").pattern("PCP").pattern(" S ").unlockedBy("has_iron_plate", has(IETags.getTagsFor(EnumMetals.IRON).plate)).save(consumer, toResourceLocation("trash_item"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ITBlocks.MetalDevices.TRASH_FLUID.get().asItem(), 1).define('P', IETags.getTagsFor(EnumMetals.IRON).plate).define('B', IEBlocks.MetalDevices.FLUID_PUMP.get()).define('S', IETags.getItemTag(IETags.getTagsFor(EnumMetals.IRON).sheetmetal)).pattern("PPP").pattern("PBP").pattern(" S ").unlockedBy("has_iron_plate", has(IETags.getTagsFor(EnumMetals.IRON).plate)).save(consumer, toResourceLocation("trash_fluid"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ITBlocks.MetalDevices.TRASH_ENERGY.get().asItem(), 1).define('P', IETags.getTagsFor(EnumMetals.IRON).plate).define('C', IEBlocks.MetalDecoration.HV_COIL.get()).define('S', IETags.getItemTag(IETags.getTagsFor(EnumMetals.IRON).sheetmetal)).pattern("PPP").pattern("PCP").pattern(" S ").unlockedBy("has_iron_plate", has(IETags.getTagsFor(EnumMetals.IRON).plate)).save(consumer, toResourceLocation("trash_energy"));
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ITBlocks.MetalDevices.BARREL_OPEN.get().asItem(), 1).define('S', Ingredient.of(IEBlocks.TO_SLAB.get(IEBlocks.Metals.SHEETMETAL.get(EnumMetals.IRON).getId()).get())).define('B', Ingredient.of(IEBlocks.Metals.SHEETMETAL.get(EnumMetals.IRON).get())).pattern("S S").pattern("B B").pattern("BBB").unlockedBy("has_slab_sheetmetal_iron", has(IEBlocks.TO_SLAB.get(IEBlocks.Metals.SHEETMETAL.get(EnumMetals.IRON).getId()).get())).save(consumer, toResourceLocation("barrel_open"));
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ITBlocks.MetalDevices.BARREL_STEEL.get().asItem(), 1).define('S', Ingredient.of(IEBlocks.TO_SLAB.get(IEBlocks.Metals.SHEETMETAL.get(EnumMetals.STEEL).getId()).get())).define('B', Ingredient.of(IEBlocks.Metals.SHEETMETAL.get(EnumMetals.STEEL).get())).pattern("SSS").pattern("B B").pattern("BBB").unlockedBy("has_slab_sheetmetal_steel", has(IEBlocks.TO_SLAB.get(IEBlocks.Metals.SHEETMETAL.get(EnumMetals.STEEL).getId()).get())).save(consumer, toResourceLocation("barrel_steel"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ITBlocks.MetalDevices.VALVE_FLUID.get().asItem(), 2).define('L', IETags.getTagsFor(EnumMetals.IRON).plate).define('R', IEBlocks.Connectors.CONNECTOR_REDSTONE.get()).define('P', IEBlocks.MetalDevices.FLUID_PIPE.get()).define('I', IEItems.Ingredients.COMPONENT_IRON.get()).define('C', IEItems.Ingredients.CIRCUIT_BOARD.get()).pattern("LRL").pattern("PIP").pattern("LCL").unlockedBy("has_iron_plate", has(IETags.getTagsFor(EnumMetals.IRON).plate)).save(consumer, toResourceLocation("valve_fluid"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ITBlocks.MetalDevices.VALVE_LOAD.get().asItem(), 2).define('L', IETags.getTagsFor(EnumMetals.IRON).plate).define('R', IEBlocks.Connectors.CONNECTOR_REDSTONE.get()).define('P', IEBlocks.Connectors.getEnergyConnector("HV", false).get()).define('I', IEItems.Ingredients.COMPONENT_IRON.get()).define('C', IEItems.Ingredients.CIRCUIT_BOARD.get()).pattern("LRL").pattern("PIP").pattern("LCL").unlockedBy("has_iron_plate", has(IETags.getTagsFor(EnumMetals.IRON).plate)).save(consumer, toResourceLocation("valve_load"));
    }

    private void recipesBoilerTank(@Nonnull Consumer<FinishedRecipe> out) {
        BoilerTankRecipeBuilder.builder(FluidTags.WATER, 250).addOutput(ITFluids.STEAM.getStill(), 450).setTime(10).build(out, toResourceLocation("boiler_tank/water"));
        BoilerTankRecipeBuilder.builder(ITTags.fluidDistilledWater, 250).addOutput(ITFluids.STEAM.getStill(), 500).setTime(10).build(out, toResourceLocation("boiler_tank/distilled_water"));
    }

    private void recipesBoilerLiquid(@Nonnull Consumer<FinishedRecipe> out) {
        BoilerLiquidRecipeBuilder.builder().addInput(IETags.fluidBiodiesel, 10).setTime(10).setHeatPerTick(0.1).build(out, toResourceLocation("boiler_liquid/biodiesel"));
    }

    private void recipesBoilerSolid(@Nonnull Consumer<FinishedRecipe> out) {
        BoilerSolidRecipeBuilder.builder().addInput(ItemTags.COALS, 1).setHeatPerTick(0.1).build(out, toResourceLocation("boiler_solid/coal"));
        BoilerSolidRecipeBuilder.builder().addInput(IETags.coalCoke, 1).setHeatPerTick(0.1).build(out, toResourceLocation("boiler_solid/coal_coke"));
    }

    private void recipesCoolingTower(@Nonnull Consumer<FinishedRecipe> out) {
        CoolingTowerRecipeBuilder.builder().addInput(FluidTags.WATER, 1000).addInput(ITTags.fluidExhaustSteam, 900).addOutput(Fluids.WATER, 500).addOutput(Fluids.WATER, 500).addOutput(Fluids.WATER, 500).setTime(3).build(out, toResourceLocation("cooling_tower/water"));
    }

    private void recipesDistiller(@Nonnull Consumer<FinishedRecipe> out) {
        ItemStack salt = new ItemStack(ITItems.SALT.get(), 1);
        DistillerRecipeBuilder.builder(new FluidTagInput(FluidTags.WATER, 1000), new FluidStack(ITFluids.DISTILLED_WATER.getStill(), 500), 20, 10000).addItemOutput(salt, 0.5f).build(out, toResourceLocation("distiller/water"));
    }

    private void recipesMixer(@Nonnull Consumer<FinishedRecipe> out) {
        MixerRecipeBuilder.builder(ITFluids.SALT_SLURRY.getStill(), 1000).addFluidTag(FluidTags.WATER, 1000).addInput(new IngredientWithSize(ITTags.saltForge, 4)).setEnergy(3200).build(out, toResourceLocation("mixer/salt_slurry"));
    }

    private void recipesTurbine(@Nonnull Consumer<FinishedRecipe> out) {
        SteamTurbineRecipeBuilder.builder().addInput(ITTags.fluidSteam, 100).addOutput(ITFluids.EXHAUST_STEAM.getStill(), 100).setTime(1).build(out, toResourceLocation("steam_turbine/steam"));
        SteamTurbineRecipeBuilder.builder().addInput(ITTags.fluidSteamForge, 100).addOutput(ITFluids.EXHAUST_STEAM.getStill(), 100).setTime(1).build(out, toResourceLocation("steam_turbine/steam_forge"));
        GasTurbineRecipeBuilder.builder().addInput(IETags.fluidBiodiesel, 160).addOutput(ITFluids.FLUE_GAS.getStill(), 1000).setTime(10).build(out, toResourceLocation("gas_turbine/biodiesel"));
    }

    private void recipesSolarMelter(@Nonnull Consumer<FinishedRecipe> out) {
        SolarMelterRecipeBuilder.builder().addInput(ITTags.fluidHeatedSaltSlurry, 1000).addOutput(ITFluids.MOLTEN_SALT.getStill(), 500).setTime(20).setRequiredTemp(1000.0).build(out, toResourceLocation("solar_melter/heated_salt"));
    }

    private void recipesSolarTower(@Nonnull Consumer<FinishedRecipe> out) {
        SolarTowerRecipeBuilder.builder().addInput(FluidTags.WATER, 250).addOutput(ITFluids.STEAM.getStill(), 450).setTime(20).setRequiredTemp(100.0).build(out, toResourceLocation("solar_tower/water"));
        SolarTowerRecipeBuilder.builder().addInput(ITTags.fluidDistilledWater, 250).addOutput(ITFluids.STEAM.getStill(), 500).setTime(20).setRequiredTemp(100.0).build(out, toResourceLocation("solar_tower/distilled_water"));
        SolarTowerRecipeBuilder.builder().addInput(ITTags.fluidSaltSlurry, 1000).addOutput(ITFluids.HEATED_SALT.getStill(), 500).setTime(20).setRequiredTemp(400.0).build(out, toResourceLocation("solar_tower/salt_slurry"));
    }

    private ResourceLocation toResourceLocation(String resourceLocation) {
        if (!resourceLocation.contains("/")) { resourceLocation = "crafting/" + resourceLocation; }
        if (PATH_COUNT.containsKey(resourceLocation)) {
            int count = PATH_COUNT.get(resourceLocation) + 1;
            PATH_COUNT.put(resourceLocation, count);
            return ResourceLocation.fromNamespaceAndPath(ITLib.MODID, resourceLocation + count);
        }
        PATH_COUNT.put(resourceLocation, 1);
        return ResourceLocation.fromNamespaceAndPath(ITLib.MODID, resourceLocation);
    }
}
