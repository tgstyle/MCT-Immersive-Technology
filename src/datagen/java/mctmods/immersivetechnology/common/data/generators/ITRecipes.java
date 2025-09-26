package mctmods.immersivetechnology.common.data.generators;

import blusunrize.immersiveengineering.api.IETags;
import blusunrize.immersiveengineering.api.crafting.FluidTagInput;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.api.crafting.builders.MixerRecipeBuilder;
import mctmods.immersivetechnology.common.blocks.multiblocks.recipe.builder.*;
import mctmods.immersivetechnology.core.lib.ITLib;
import mctmods.immersivetechnology.core.registration.ITFluids;
import mctmods.immersivetechnology.core.registration.ITItems;
import mctmods.immersivetechnology.core.registration.ITTags;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
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
        itemRecipes();
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

    private void itemRecipes() {}

    private void multiblockRecipes() { ITLib.IT_LOGGER.info("Starting Multiblock Recipe Registration"); }

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
        CoolingTowerRecipeBuilder.builder().addInput(FluidTags.WATER, 2925).addInput(ITTags.fluidExhaustSteam, 8100).addOutput(Fluids.WATER, 2925).addOutput(Fluids.WATER, 2925).addOutput(Fluids.WATER, 2925).setTime(3).build(out, toResourceLocation("cooling_tower/water"));
    }

    private void recipesDistiller(@Nonnull Consumer<FinishedRecipe> out) {
        ItemStack salt = new ItemStack(ITItems.SALT.get(), 1);
        DistillerRecipeBuilder.builder(new FluidTagInput(FluidTags.WATER, 1000), new FluidStack(ITFluids.DISTILLED_WATER.getStill(), 500), 20, 10000).addItemOutput(salt, 0.5f).build(out, toResourceLocation("distiller/water"));
    }

    private void recipesMixer(@Nonnull Consumer<FinishedRecipe> out) {
        MixerRecipeBuilder.builder(ITFluids.SALT_SLURRY.getStill(), 1000).addFluidTag(FluidTags.WATER, 1000).addInput(new IngredientWithSize(ITTags.salt, 4)).setEnergy(3200).build(out, toResourceLocation("mixer/salt_slurry"));
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
