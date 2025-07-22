package mctmods.immersivetechnology.common.data.generators;

import blusunrize.immersiveengineering.api.IETags;
import blusunrize.immersiveengineering.api.crafting.FluidTagInput;
import mctmods.immersivetechnology.common.blocks.multiblocks.recipe.builder.AdvancedCokeOvenRecipeBuilder;
import mctmods.immersivetechnology.common.blocks.multiblocks.recipe.builder.BoilerRecipeBuilder;
import mctmods.immersivetechnology.common.blocks.multiblocks.recipe.builder.GasTurbineRecipeBuilder;
import mctmods.immersivetechnology.common.blocks.multiblocks.recipe.builder.SteamTurbineRecipeBuilder;
import mctmods.immersivetechnology.core.lib.ITLib;
import mctmods.immersivetechnology.core.registration.ITFluids;
import mctmods.immersivetechnology.core.registration.ITTags;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.fluids.FluidType;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.function.Consumer;

public class ITRecipes extends RecipeProvider
{
    private HashMap<String, Integer> PATH_COUNT = new HashMap<>();

    public ITRecipes(PackOutput pOutput)
    {
        super(pOutput);
    }

    @Override
    protected void buildRecipes(@NotNull Consumer<FinishedRecipe> consumer)
    {
        multiblockRecipes(consumer);
        itemRecipes(consumer);
        recipesCoke(consumer);
        recipesBoiler(consumer);
        recipesTurbine(consumer);
    }

    private void itemRecipes(Consumer<FinishedRecipe> consumer)
    {

    }

    private void multiblockRecipes(Consumer<FinishedRecipe> consumer)
    {
        ITLib.IT_LOGGER.info("Starting Multiblock Recipe Registration");
    }

    private void recipesBoiler(@Nonnull Consumer<FinishedRecipe> out)
    {
        BoilerRecipeBuilder.builder(ITFluids.STEAM.getStill(), 100)
                .addInput(new FluidTagInput(FluidTags.WATER, 95), "input1")
                .addInput(new FluidTagInput(IETags.fluidBiodiesel, 50), "input0")
                .setTime(75)
                .setHeatPerTick(3)
                .build(out, toRL("boiler/biodiesel"));
        BoilerRecipeBuilder.builder(ITFluids.STEAM.getStill(), 100)
                .addInput(new FluidTagInput(FluidTags.WATER, 95), "input1")
                .addInput(new FluidTagInput(IETags.fluidCreosote, 50), "input0")
                .setTime(25)
                .setHeatPerTick(3)
                .build(out, toRL("boiler/creosote"));
    }

    private void recipesTurbine(@Nonnull Consumer<FinishedRecipe> out)
    {
        SteamTurbineRecipeBuilder.builder()
                .addInput(ITTags.fluidSteam, 100)
                .addOutput(ITFluids.STEAM_EXHAUST.getStill(), 100)
                .setTime(1)
                .build(out, toRL("steamturbine/steam"));
        SteamTurbineRecipeBuilder.builder()
                .addInput(ITTags.fluidSteamForge, 100)
                .addOutput(ITFluids.STEAM_EXHAUST.getStill(), 100)
                .setTime(1)
                .build(out, toRL("steamturbine/steam_forge"));
        GasTurbineRecipeBuilder.builder()
                .addInput(IETags.fluidBiodiesel, 160)
                .addOutput(ITFluids.FLUE_GAS.getStill(), 1000)
                .setTime(10)
                .build(out, toRL("gas_turbine/biodiesel"));
    }

    private void recipesCoke(@Nonnull Consumer<FinishedRecipe> out)
    {
        AdvancedCokeOvenRecipeBuilder.builder(IETags.coalCoke, 1)
                .addInput(Items.COAL)
                .setOil(FluidType.BUCKET_VOLUME/2)
                .setTime(600)
                .build(out, toRL("cokeovenadv/coke"));
        AdvancedCokeOvenRecipeBuilder.builder(IETags.getItemTag(IETags.coalCokeBlock), 1)
                .addInput(Blocks.COAL_BLOCK)
                .setOil(FluidType.BUCKET_VOLUME*5)
                .setTime(9*600)
                .build(out, toRL("cokeovenadv/coke_block"));
        AdvancedCokeOvenRecipeBuilder.builder(Items.CHARCOAL)
                .addInput(ItemTags.LOGS)
                .setOil(FluidType.BUCKET_VOLUME/4)
                .setTime(600)
                .build(out, toRL("cokeovenadv/charcoal"));
    }

    private ResourceLocation toRL(String s)
    {
        if(!s.contains("/"))
            s = "crafting/"+s;
        if(PATH_COUNT.containsKey(s))
        {
            int count = PATH_COUNT.get(s)+1;
            PATH_COUNT.put(s, count);
            return new ResourceLocation(ITLib.MODID, s+count);
        }
        PATH_COUNT.put(s, 1);
        return new ResourceLocation(ITLib.MODID, s);
    }
}