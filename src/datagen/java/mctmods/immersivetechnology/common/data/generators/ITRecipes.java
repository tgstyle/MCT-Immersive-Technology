package mctmods.immersivetechnology.common.data.generators;

import blusunrize.immersiveengineering.api.IETags;
import blusunrize.immersiveengineering.api.crafting.FluidTagInput;
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
        recipesAdvancedCoke(consumer);
        recipesAdvancedCokeFuel(consumer);
        recipesBoiler(consumer);
        recipesBoilerFuel(consumer);
        recipesDistiller(consumer);
        recipesTurbine(consumer);
        recipesSolarMelter(consumer);
        recipesSolarTower(consumer);
    }

    private void itemRecipes() {}

    private void multiblockRecipes() { ITLib.IT_LOGGER.info("Starting Multiblock Recipe Registration"); }

    private void recipesAdvancedCoke(@Nonnull Consumer<FinishedRecipe> out) {
        AdvancedCokeOvenRecipeBuilder.builder(IETags.coalCoke, 1).addInput(Items.COAL).setOil(FluidType.BUCKET_VOLUME / 2).setTime(600).build(out, toResourceLocation("advcokeoven/coke"));
        AdvancedCokeOvenRecipeBuilder.builder(IETags.getItemTag(IETags.coalCokeBlock), 1).addInput(Blocks.COAL_BLOCK).setOil(FluidType.BUCKET_VOLUME * 5).setTime(9 * 600).build(out, toResourceLocation("advcokeoven/coke_block"));
        AdvancedCokeOvenRecipeBuilder.builder(Items.CHARCOAL).addInput(ItemTags.LOGS).setOil(FluidType.BUCKET_VOLUME / 4).setTime(600).build(out, toResourceLocation("advcokeoven/charcoal"));
    }

    private void recipesAdvancedCokeFuel(@Nonnull Consumer<FinishedRecipe> out) {
        AdvancedCokeOvenFuelBuilder.builder(Ingredient.of(IETags.coalCoke)).setTime(1200).build(out, toResourceLocation("advcokeoven_fuel/coal_coke"));
        AdvancedCokeOvenFuelBuilder.builder(Ingredient.of(Items.CHARCOAL)).setTime(600).build(out, toResourceLocation("advcokeoven_fuel/charcoal"));
    }

    private void recipesBoiler(@Nonnull Consumer<FinishedRecipe> out) {
        BoilerRecipeBuilder.builder(ITFluids.STEAM.getStill(), 450).addInput(FluidTags.WATER, 250).setTime(10).build(out, toResourceLocation("boiler/water"));
        BoilerRecipeBuilder.builder(ITFluids.STEAM.getStill(), 500).addInput(ITTags.fluidDistilledWater, 250).setTime(10).build(out, toResourceLocation("boiler/distilled_water"));
    }

    private void recipesBoilerFuel(@Nonnull Consumer<FinishedRecipe> out) { BoilerFuelRecipeBuilder.builder().addInput(IETags.fluidBiodiesel, 10).setTime(10).setHeatPerTick(0.1).build(out, toResourceLocation("boiler_fuel/biodiesel")); }

    private void recipesDistiller(@Nonnull Consumer<FinishedRecipe> out) {
        ItemStack salt = new ItemStack(ITItems.SALT.get(), 1);
        DistillerRecipeBuilder.builder(new FluidTagInput(FluidTags.WATER, 1000), 10000, 20, new FluidStack(ITFluids.DISTILLED_WATER.getStill().getSource(), 500)).addItemOutput(salt, 0.5f).build(out, toResourceLocation("distiller/water"));
    }

    private void recipesTurbine(@Nonnull Consumer<FinishedRecipe> out) {
        SteamTurbineRecipeBuilder.builder().addInput(ITTags.fluidSteam, 100).addOutput(ITFluids.STEAM_EXHAUST.getStill(), 100).setTime(1).build(out, toResourceLocation("steamturbine/steam"));
        SteamTurbineRecipeBuilder.builder().addInput(ITTags.fluidSteamForge, 100).addOutput(ITFluids.STEAM_EXHAUST.getStill(), 100).setTime(1).build(out, toResourceLocation("steamturbine/steam_forge"));
        GasTurbineRecipeBuilder.builder().addInput(IETags.fluidBiodiesel, 160).addOutput(ITFluids.FLUE_GAS.getStill(), 1000).setTime(10).build(out, toResourceLocation("gas_turbine/biodiesel"));
    }

    private void recipesSolarMelter(@Nonnull Consumer<FinishedRecipe> out) { }

    private void recipesSolarTower(@Nonnull Consumer<FinishedRecipe> out) { }

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
