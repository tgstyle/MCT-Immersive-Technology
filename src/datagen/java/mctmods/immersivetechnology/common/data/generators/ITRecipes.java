package mctmods.immersivetechnology.common.data.generators;

import blusunrize.immersiveengineering.api.IETags;
import blusunrize.immersiveengineering.api.crafting.FluidTagInput;
import blusunrize.immersiveengineering.api.crafting.builders.CokeOvenRecipeBuilder;
import com.igteam.immersivegeology.common.item.IGGenericItem;
import com.igteam.immersivegeology.core.lib.IGLib;
import com.igteam.immersivegeology.core.material.data.enums.MineralEnum;
import com.igteam.immersivegeology.core.material.helper.flags.BlockCategoryFlags;
import com.igteam.immersivegeology.core.material.helper.flags.ItemCategoryFlags;
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
        recipesCoke(consumer);
        recipesAdvCokeFuel(consumer);
        recipesBoiler(consumer);
        recipesBoilerFuel(consumer);
        recipesTurbine(consumer);
        recipesDistiller(consumer);
        registerCokingRecipes(consumer, MineralEnum.Bituminous);
    }

    private void itemRecipes() {}

    private void multiblockRecipes() { ITLib.IT_LOGGER.info("Starting Multiblock Recipe Registration"); }

    private void recipesBoiler(@Nonnull Consumer<FinishedRecipe> out) {
        BoilerRecipeBuilder.builder(ITFluids.STEAM.getStill(), 450).addInput(FluidTags.WATER, 250).setTime(10).build(out, toRL("boiler/water"));
        BoilerRecipeBuilder.builder(ITFluids.STEAM.getStill(), 500).addInput(ITTags.fluidDistilledWater, 250).setTime(10).build(out, toRL("boiler/distilled_water"));
    }

    private void recipesBoilerFuel(@Nonnull Consumer<FinishedRecipe> out) { BoilerFuelRecipeBuilder.builder().addInput(IETags.fluidBiodiesel, 10).setTime(10).setHeatPerTick(0.1).build(out, toRL("boiler_fuel/biodiesel")); }

    private void recipesTurbine(@Nonnull Consumer<FinishedRecipe> out) {
        SteamTurbineRecipeBuilder.builder().addInput(ITTags.fluidSteam, 100).addOutput(ITFluids.STEAM_EXHAUST.getStill(), 100).setTime(1).build(out, toRL("steamturbine/steam"));
        SteamTurbineRecipeBuilder.builder().addInput(ITTags.fluidSteamForge, 100).addOutput(ITFluids.STEAM_EXHAUST.getStill(), 100).setTime(1).build(out, toRL("steamturbine/steam_forge"));
        GasTurbineRecipeBuilder.builder().addInput(IETags.fluidBiodiesel, 160).addOutput(ITFluids.FLUE_GAS.getStill(), 1000).setTime(10).build(out, toRL("gas_turbine/biodiesel"));
    }

    private void recipesCoke(@Nonnull Consumer<FinishedRecipe> out) {
        AdvancedCokeOvenRecipeBuilder.builder(IETags.coalCoke, 1).addInput(Items.COAL).setOil(FluidType.BUCKET_VOLUME / 2).setTime(600).build(out, toRL("advcokeoven/coke"));
        AdvancedCokeOvenRecipeBuilder.builder(IETags.coalCoke, 1).addInput(MineralEnum.Lignite.getItem(ItemCategoryFlags.INGOT)).setOil(FluidType.BUCKET_VOLUME / 2).setTime(600).build(out, toRL("advcokeoven/coke_lignite"));
        AdvancedCokeOvenRecipeBuilder.builder(IETags.getItemTag(IETags.coalCokeBlock), 1).addInput(Blocks.COAL_BLOCK).setOil(FluidType.BUCKET_VOLUME * 5).setTime(9 * 600).build(out, toRL("advcokeoven/coke_block"));
        AdvancedCokeOvenRecipeBuilder.builder(Items.CHARCOAL).addInput(ItemTags.LOGS).setOil(FluidType.BUCKET_VOLUME / 4).setTime(600).build(out, toRL("advcokeoven/charcoal"));
    }

    private void recipesAdvCokeFuel(@Nonnull Consumer<FinishedRecipe> out) {
        AdvancedCokeOvenFuelBuilder.builder(Ingredient.of(IETags.coalCoke)).setTime(1200).build(out, toRL("advcokeoven_fuel/coal_coke"));
        AdvancedCokeOvenFuelBuilder.builder(Ingredient.of(Items.CHARCOAL)).setTime(600).build(out, toRL("advcokeoven_fuel/charcoal"));
    }

    private void recipesDistiller(@Nonnull Consumer<FinishedRecipe> out) {
        ItemStack salt = new ItemStack(ITItems.SALT.get(), 1);
        DistillerRecipeBuilder.builder(new FluidTagInput(FluidTags.WATER, 1000), 10000, 20, new FluidStack(ITFluids.DISTILLED_WATER.getStill().getSource(), 500)).addItemOutput(salt, 0.5f).build(out, toRL("distiller/water"));
    }

    private void registerCokingRecipes(Consumer<FinishedRecipe> consumer, MineralEnum mineral) {
        String mineralName = mineral.getName();
        AdvancedCokeOvenRecipeBuilder.builder(IETags.coalCoke, 1)
                .addInput(mineral.getItem(ItemCategoryFlags.NORMAL_ORE))
                .setOil(FluidType.BUCKET_VOLUME / 2)
                .setTime(1800)
                .build(consumer, toRL( "coking/normal_" + mineralName + "_to_coke"));

        AdvancedCokeOvenRecipeBuilder.builder(IETags.coalCoke, 1)
                .addInput(mineral.getBlock(BlockCategoryFlags.STORAGE_BLOCK))
                .setOil(FluidType.BUCKET_VOLUME * 5)
                .setTime(16200)
                .build(consumer, toRL( "coking/normal_block_" + mineralName + "_to_coke"));
    }

    private ResourceLocation toRL(String s) {
        if (!s.contains("/")) { s = "crafting/" + s; }
        if (PATH_COUNT.containsKey(s)) {
            int count = PATH_COUNT.get(s) + 1;
            PATH_COUNT.put(s, count);
            return ResourceLocation.fromNamespaceAndPath(ITLib.MODID, s + count);
        }
        PATH_COUNT.put(s, 1);
        return ResourceLocation.fromNamespaceAndPath(ITLib.MODID, s);
    }
}
